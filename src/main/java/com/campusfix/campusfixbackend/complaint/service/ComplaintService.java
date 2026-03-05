package com.campusfix.campusfixbackend.complaint.service;

import com.campusfix.campusfixbackend.building.entity.Building;
import com.campusfix.campusfixbackend.building.repository.BuildingRepository;
import com.campusfix.campusfixbackend.common.ComplaintStatus;
import com.campusfix.campusfixbackend.common.Role;
import com.campusfix.campusfixbackend.complaint.dto.*;
import com.campusfix.campusfixbackend.complaint.entity.Complaint;
import com.campusfix.campusfixbackend.complaint.entity.ComplaintStatusHistory;
import com.campusfix.campusfixbackend.complaint.repository.ComplaintRepository;
import com.campusfix.campusfixbackend.complaint.repository.ComplaintStatusHistoryRepository;
import com.campusfix.campusfixbackend.exception.ConflictException;
import com.campusfix.campusfixbackend.exception.ForbiddenException;
import com.campusfix.campusfixbackend.exception.ResourceNotFoundException;
import com.campusfix.campusfixbackend.user.entity.User;
import com.campusfix.campusfixbackend.user.repository.UserRepository;
import com.campusfix.campusfixbackend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintStatusHistoryRepository complaintStatusHistoryRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final BuildingRepository buildingRepository;

    // ==================== Get Complaints (Building Admin) ====================

    @Transactional(readOnly = true)
    public ComplaintListResponse getBuildingComplaints(String firebaseUid, String status) {
        User caller = userService.getUserByFirebaseUid(firebaseUid);
        validateBuildingAdmin(caller);

        UUID buildingId = caller.getBuildingId();
        List<Complaint> complaints;

        if (status != null && !status.isBlank()) {
            try {
                ComplaintStatus filterStatus = ComplaintStatus.valueOf(status.toUpperCase());
                complaints = complaintRepository.findByBuildingIdAndStatusOrderByCreatedAtDesc(buildingId, filterStatus);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid status filter: " + status);
            }
        } else {
            complaints = complaintRepository.findByBuildingIdOrderByCreatedAtDesc(buildingId);
        }

        List<ComplaintResponse> responses = complaints.stream()
                .map(this::toComplaintResponse)
                .toList();

        return ComplaintListResponse.builder()
                .complaints(responses)
                .message("Complaints fetched successfully")
                .build();
    }

    // ==================== Get Complaint Detail (Building Admin) ====================

    @Transactional(readOnly = true)
    public ComplaintResponse getComplaintDetail(String firebaseUid, UUID complaintId) {
        User caller = userService.getUserByFirebaseUid(firebaseUid);
        validateBuildingAdmin(caller);

        Complaint complaint = complaintRepository.findByIdAndBuildingId(complaintId, caller.getBuildingId())
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found in your building"));

        return toComplaintResponse(complaint);
    }

    // ==================== Assign Complaint to Staff ====================

    @Transactional
    public ComplaintResponse assignComplaint(String firebaseUid, UUID complaintId, AssignComplaintRequest request) {
        User caller = userService.getUserByFirebaseUid(firebaseUid);
        validateBuildingAdmin(caller);

        // Validate complaint belongs to building
        Complaint complaint = complaintRepository.findByIdAndBuildingId(complaintId, caller.getBuildingId())
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found in your building"));

        // Validate complaint status allows assignment
        if (complaint.getStatus() != ComplaintStatus.CREATED && complaint.getStatus() != ComplaintStatus.ASSIGNED) {
            throw new ConflictException("Complaint can only be assigned when status is CREATED or ASSIGNED. Current status: " + complaint.getStatus());
        }

        // Validate staff belongs to same building
        User staff = userRepository.findByIdAndBuildingId(request.getStaffId(), caller.getBuildingId())
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found in your building"));

        if (staff.getRole() != Role.STAFF) {
            throw new IllegalArgumentException("User is not a staff member");
        }

        // Record status change
        ComplaintStatus previousStatus = complaint.getStatus();
        complaint.setAssignedTo(staff.getId());
        complaint.setStatus(ComplaintStatus.ASSIGNED);
        complaint = complaintRepository.save(complaint);

        // Save status history
        saveStatusHistory(complaint.getId(), previousStatus, ComplaintStatus.ASSIGNED, caller.getId());

        log.info("Complaint assigned: complaintId={}, staffId={}, by buildingAdmin={}", complaintId, staff.getId(), caller.getId());

        return toComplaintResponse(complaint);
    }

    // ==================== Update Complaint Status ====================

    @Transactional
    public ComplaintResponse updateComplaintStatus(String firebaseUid, UUID complaintId, UpdateComplaintStatusRequest request) {
        User caller = userService.getUserByFirebaseUid(firebaseUid);
        validateBuildingAdmin(caller);

        // Validate complaint belongs to building
        Complaint complaint = complaintRepository.findByIdAndBuildingId(complaintId, caller.getBuildingId())
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found in your building"));

        // Parse requested status
        ComplaintStatus newStatus;
        try {
            newStatus = ComplaintStatus.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + request.getStatus());
        }

        // Validate status transition
        validateStatusTransition(complaint.getStatus(), newStatus);

        // Update status
        ComplaintStatus previousStatus = complaint.getStatus();
        complaint.setStatus(newStatus);
        complaint = complaintRepository.save(complaint);

        // Save status history
        saveStatusHistory(complaint.getId(), previousStatus, newStatus, caller.getId());

        log.info("Complaint status updated: complaintId={}, {} -> {}, by buildingAdmin={}", complaintId, previousStatus, newStatus, caller.getId());

        return toComplaintResponse(complaint);
    }

    // ==================== Helper Methods ====================

    private void validateBuildingAdmin(User user) {
        if (user.getRole() != Role.BUILDING_ADMIN) {
            throw new ForbiddenException("Only building admins can access this resource");
        }
        if (user.getBuildingId() == null) {
            throw new ForbiddenException("Building admin is not assigned to any building");
        }
    }

    private void validateStatusTransition(ComplaintStatus current, ComplaintStatus target) {
        boolean valid = switch (current) {
            case ASSIGNED -> target == ComplaintStatus.IN_PROGRESS;
            case IN_PROGRESS -> target == ComplaintStatus.RESOLVED;
            default -> false;
        };

        if (!valid) {
            throw new ConflictException("Invalid status transition: " + current + " → " + target);
        }
    }

    private void saveStatusHistory(UUID complaintId, ComplaintStatus previousStatus, ComplaintStatus newStatus, UUID changedBy) {
        ComplaintStatusHistory history = ComplaintStatusHistory.builder()
                .complaintId(complaintId)
                .previousStatus(previousStatus)
                .newStatus(newStatus)
                .changedBy(changedBy)
                .changedAt(LocalDateTime.now())
                .build();
        complaintStatusHistoryRepository.save(history);
    }

    private ComplaintResponse toComplaintResponse(Complaint complaint) {
        // Fetch student name
        String studentName = userRepository.findById(complaint.getCreatedBy())
                .map(User::getName)
                .orElse(null);

        // Fetch assigned staff name
        String assignedStaffName = null;
        if (complaint.getAssignedTo() != null) {
            assignedStaffName = userRepository.findById(complaint.getAssignedTo())
                    .map(User::getName)
                    .orElse(null);
        }

        // Fetch building name
        String buildingName = buildingRepository.findById(complaint.getBuildingId())
                .map(Building::getName)
                .orElse(null);

        return ComplaintResponse.builder()
                .id(complaint.getId())
                .title(complaint.getTitle())
                .description(complaint.getDescription())
                .status(complaint.getStatus().name())
                .studentName(studentName)
                .location(buildingName)
                .buildingId(complaint.getBuildingId())
                .buildingName(buildingName)
                .assignedStaffId(complaint.getAssignedTo())
                .assignedStaffName(assignedStaffName)
                .createdAt(complaint.getCreatedAt())
                .updatedAt(complaint.getUpdatedAt())
                .build();
    }
}

