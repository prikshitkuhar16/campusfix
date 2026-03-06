package com.campusfix.campusfixbackend.complaint.service;

import com.campusfix.campusfixbackend.building.entity.Building;
import com.campusfix.campusfixbackend.building.repository.BuildingRepository;
import com.campusfix.campusfixbackend.common.ComplaintStatus;
import com.campusfix.campusfixbackend.common.JobType;
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

    // ==================== Role-Based Complaint Access ====================

    @Transactional(readOnly = true)
    public ComplaintListResponse getComplaintsByRole(String firebaseUid, String status) {
        User caller = userService.getUserByFirebaseUid(firebaseUid);

        return switch (caller.getRole()) {
            case BUILDING_ADMIN -> getBuildingComplaints(firebaseUid, status);
            case STAFF -> getStaffComplaints(firebaseUid, status);
            case STUDENT -> getStudentComplaints(firebaseUid);
            default -> throw new ForbiddenException("Your role does not have access to complaints");
        };
    }

    @Transactional(readOnly = true)
    public ComplaintResponse getComplaintDetailByRole(String firebaseUid, UUID complaintId) {
        User caller = userService.getUserByFirebaseUid(firebaseUid);

        return switch (caller.getRole()) {
            case BUILDING_ADMIN -> getComplaintDetail(firebaseUid, complaintId);
            case STAFF -> getStaffComplaintDetail(firebaseUid, complaintId);
            case STUDENT -> getStudentComplaintDetail(firebaseUid, complaintId);
            default -> throw new ForbiddenException("Your role does not have access to complaints");
        };
    }

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

        // Validate complaint belongs to admin's building
        Complaint complaint = complaintRepository.findByIdAndBuildingId(complaintId, caller.getBuildingId())
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found in your building"));

        // Reject if status is RESOLVED or VERIFIED
        if (complaint.getStatus() == ComplaintStatus.RESOLVED || complaint.getStatus() == ComplaintStatus.VERIFIED) {
            throw new ConflictException("Cannot assign complaint with status: " + complaint.getStatus());
        }

        // Only allow assignment when CREATED or IN_PROGRESS
        if (complaint.getStatus() != ComplaintStatus.CREATED && complaint.getStatus() != ComplaintStatus.IN_PROGRESS) {
            throw new ConflictException("Complaint can only be assigned when status is CREATED or IN_PROGRESS. Current status: " + complaint.getStatus());
        }

        // Validate staff belongs to same building
        User staff = userRepository.findByIdAndBuildingId(request.getStaffId(), caller.getBuildingId())
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found in your building"));

        if (staff.getRole() != Role.STAFF) {
            throw new IllegalArgumentException("User is not a staff member");
        }

        // Validate staff jobType matches complaint jobType
        if (complaint.getJobType() != null && staff.getJobType() != complaint.getJobType()) {
            throw new IllegalArgumentException("Staff job type (" + staff.getJobType() + ") does not match complaint job type (" + complaint.getJobType() + ")");
        }

        ComplaintStatus previousStatus = complaint.getStatus();
        complaint.setAssignedTo(staff.getId());

        if (previousStatus == ComplaintStatus.CREATED) {
            // New assignment: CREATED → IN_PROGRESS
            complaint.setStatus(ComplaintStatus.IN_PROGRESS);
        }
        // If IN_PROGRESS: reassign only (status stays IN_PROGRESS)

        complaint = complaintRepository.save(complaint);

        // Save status history
        saveStatusHistory(complaint.getId(), previousStatus, complaint.getStatus(), caller.getId());

        log.info("Complaint assigned: complaintId={}, staffId={}, {} -> {}, by buildingAdmin={}",
                complaintId, staff.getId(), previousStatus, complaint.getStatus(), caller.getId());

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

    // ==================== Student: Create Complaint ====================

    @Transactional
    public ComplaintResponse createComplaint(String firebaseUid, CreateComplaintRequest request) {
        User student = userService.getUserByFirebaseUid(firebaseUid);
        validateStudent(student);

        // Parse and validate job type
        JobType jobType;
        try {
            jobType = JobType.valueOf(request.getJobType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid job type: " + request.getJobType());
        }

        // Validate building belongs to student's campus
        Building building = buildingRepository.findByIdAndCampusId(request.getBuildingId(), student.getCampusId())
                .orElseThrow(() -> new ResourceNotFoundException("Building not found in your campus"));

        Complaint complaint = Complaint.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .room(request.getRoom())
                .jobType(jobType)
                .status(ComplaintStatus.CREATED)
                .campusId(student.getCampusId())
                .buildingId(building.getId())
                .createdBy(student.getId())
                .assignedTo(null)
                .build();

        complaint = complaintRepository.save(complaint);

        // Save status history
        saveStatusHistory(complaint.getId(), null, ComplaintStatus.CREATED, student.getId());

        log.info("Complaint created: complaintId={}, by student={}", complaint.getId(), student.getId());

        return toComplaintResponse(complaint);
    }

    // ==================== Student: Get My Complaints ====================

    @Transactional(readOnly = true)
    public ComplaintListResponse getStudentComplaints(String firebaseUid) {
        User student = userService.getUserByFirebaseUid(firebaseUid);
        validateStudent(student);

        List<Complaint> complaints = complaintRepository.findByCreatedByOrderByCreatedAtDesc(student.getId());

        List<ComplaintResponse> responses = complaints.stream()
                .map(this::toComplaintResponse)
                .toList();

        return ComplaintListResponse.builder()
                .complaints(responses)
                .message("Student complaints fetched successfully")
                .build();
    }

    // ==================== Student: Get Complaint Detail ====================

    @Transactional(readOnly = true)
    public ComplaintResponse getStudentComplaintDetail(String firebaseUid, UUID complaintId) {
        User student = userService.getUserByFirebaseUid(firebaseUid);
        validateStudent(student);

        Complaint complaint = complaintRepository.findByIdAndCreatedBy(complaintId, student.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found or does not belong to you"));

        return toComplaintResponse(complaint);
    }

    // ==================== Student: Verify Resolution ====================

    @Transactional
    public ComplaintResponse verifyComplaint(String firebaseUid, UUID complaintId) {
        User student = userService.getUserByFirebaseUid(firebaseUid);
        validateStudent(student);

        Complaint complaint = complaintRepository.findByIdAndCreatedBy(complaintId, student.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found or does not belong to you"));

        if (complaint.getStatus() != ComplaintStatus.RESOLVED) {
            throw new ConflictException("Complaint can only be verified when status is RESOLVED. Current status: " + complaint.getStatus());
        }

        ComplaintStatus previousStatus = complaint.getStatus();
        complaint.setStatus(ComplaintStatus.VERIFIED);
        complaint = complaintRepository.save(complaint);

        saveStatusHistory(complaint.getId(), previousStatus, ComplaintStatus.VERIFIED, student.getId());

        log.info("Complaint verified: complaintId={}, by student={}", complaintId, student.getId());

        return toComplaintResponse(complaint);
    }

    // ==================== Staff: Get Complaints (Auto-Assignment) ====================

    @Transactional(readOnly = true)
    public ComplaintListResponse getStaffComplaints(String firebaseUid, String status) {
        User staff = userService.getUserByFirebaseUid(firebaseUid);
        validateStaff(staff);

        List<Complaint> complaints;

        if (status != null && !status.isBlank()) {
            try {
                ComplaintStatus filterStatus = ComplaintStatus.valueOf(status.toUpperCase());

                if (filterStatus == ComplaintStatus.CREATED) {
                    // Show available complaints matching staff's building and job_type
                    complaints = complaintRepository.findByBuildingIdAndJobTypeAndStatusOrderByCreatedAtDesc(
                            staff.getBuildingId(), staff.getJobType(), ComplaintStatus.CREATED);
                } else {
                    // IN_PROGRESS / RESOLVED / VERIFIED — show only complaints assigned to this staff
                    complaints = complaintRepository.findByAssignedToAndStatusOrderByCreatedAtDesc(staff.getId(), filterStatus);
                }
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid status filter: " + status);
            }
        } else {
            // No filter: show available (CREATED matching building+jobType) + all assigned to staff
            List<Complaint> available = complaintRepository.findByBuildingIdAndJobTypeAndStatusOrderByCreatedAtDesc(
                    staff.getBuildingId(), staff.getJobType(), ComplaintStatus.CREATED);
            List<Complaint> assigned = complaintRepository.findByAssignedToOrderByCreatedAtDesc(staff.getId());

            complaints = new java.util.ArrayList<>(available);
            // Add assigned complaints that aren't already in the available list
            java.util.Set<UUID> availableIds = available.stream().map(Complaint::getId).collect(java.util.stream.Collectors.toSet());
            assigned.stream().filter(c -> !availableIds.contains(c.getId())).forEach(complaints::add);
        }

        List<ComplaintResponse> responses = complaints.stream()
                .map(this::toComplaintResponse)
                .toList();

        return ComplaintListResponse.builder()
                .complaints(responses)
                .message("Staff complaints fetched successfully")
                .build();
    }

    // ==================== Staff: Get Complaint Detail ====================

    @Transactional(readOnly = true)
    public ComplaintResponse getStaffComplaintDetail(String firebaseUid, UUID complaintId) {
        User staff = userService.getUserByFirebaseUid(firebaseUid);
        validateStaff(staff);

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found"));

        // Staff can view if: complaint is assigned to them, OR complaint is CREATED and matches their building+jobType
        boolean isAssignedToStaff = staff.getId().equals(complaint.getAssignedTo());
        boolean isAvailableForStaff = complaint.getStatus() == ComplaintStatus.CREATED
                && staff.getBuildingId().equals(complaint.getBuildingId())
                && staff.getJobType() == complaint.getJobType();

        if (!isAssignedToStaff && !isAvailableForStaff) {
            throw new ForbiddenException("You do not have access to this complaint");
        }

        return toComplaintResponse(complaint);
    }

    // ==================== Staff: Start (Pick) Complaint ====================

    @Transactional
    public ComplaintResponse startComplaint(String firebaseUid, UUID complaintId) {
        User staff = userService.getUserByFirebaseUid(firebaseUid);
        validateStaff(staff);

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found"));

        // Validate job_type matches
        if (staff.getJobType() != complaint.getJobType()) {
            throw new ForbiddenException("Your job type does not match this complaint's job type");
        }

        // Validate building matches
        if (!staff.getBuildingId().equals(complaint.getBuildingId())) {
            throw new ForbiddenException("This complaint is not in your building");
        }

        // Atomic update: only succeeds if status is still CREATED
        int updatedRows = complaintRepository.atomicPickComplaint(
                complaintId, staff.getId(), ComplaintStatus.IN_PROGRESS, ComplaintStatus.CREATED);

        if (updatedRows == 0) {
            throw new ConflictException("Complaint is no longer available. It may have been picked by another staff member.");
        }

        // Save status history
        saveStatusHistory(complaintId, ComplaintStatus.CREATED, ComplaintStatus.IN_PROGRESS, staff.getId());

        log.info("Staff started complaint: complaintId={}, staffId={}", complaintId, staff.getId());

        // Refresh to return updated state
        complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found"));

        return toComplaintResponse(complaint);
    }

    // ==================== Staff: Resolve Complaint ====================

    @Transactional
    public ComplaintResponse resolveComplaint(String firebaseUid, UUID complaintId) {
        User staff = userService.getUserByFirebaseUid(firebaseUid);
        validateStaff(staff);

        Complaint complaint = complaintRepository.findByIdAndAssignedTo(complaintId, staff.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found or not assigned to you"));

        if (complaint.getStatus() != ComplaintStatus.IN_PROGRESS) {
            throw new ConflictException("Complaint can only be resolved when status is IN_PROGRESS. Current status: " + complaint.getStatus());
        }

        ComplaintStatus previousStatus = complaint.getStatus();
        complaint.setStatus(ComplaintStatus.RESOLVED);
        complaint = complaintRepository.save(complaint);

        saveStatusHistory(complaint.getId(), previousStatus, ComplaintStatus.RESOLVED, staff.getId());

        log.info("Staff resolved complaint: complaintId={}, staffId={}", complaintId, staff.getId());

        return toComplaintResponse(complaint);
    }

    // ==================== Staff: Update Complaint Status (Legacy) ====================

    @Transactional
    public ComplaintResponse updateStaffComplaintStatus(String firebaseUid, UUID complaintId, UpdateComplaintStatusRequest request) {
        User caller = userService.getUserByFirebaseUid(firebaseUid);
        validateStaff(caller);

        Complaint complaint = complaintRepository.findByIdAndAssignedTo(complaintId, caller.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found or not assigned to you"));

        // Parse requested status
        ComplaintStatus newStatus;
        try {
            newStatus = ComplaintStatus.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + request.getStatus());
        }

        // Validate staff-allowed transitions: IN_PROGRESS -> RESOLVED
        validateStaffStatusTransition(complaint.getStatus(), newStatus);

        // Update status
        ComplaintStatus previousStatus = complaint.getStatus();
        complaint.setStatus(newStatus);
        complaint = complaintRepository.save(complaint);

        // Save status history
        saveStatusHistory(complaint.getId(), previousStatus, newStatus, caller.getId());

        log.info("Staff updated complaint status: complaintId={}, {} -> {}, by staff={}", complaintId, previousStatus, newStatus, caller.getId());

        return toComplaintResponse(complaint);
    }

    // ==================== Helper Methods ====================

    private void validateStudent(User user) {
        if (user.getRole() != Role.STUDENT) {
            throw new ForbiddenException("Only students can access this resource");
        }
    }

    private void validateStaff(User user) {
        if (user.getRole() != Role.STAFF) {
            throw new ForbiddenException("Only staff can access this resource");
        }
    }

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
            case CREATED -> target == ComplaintStatus.IN_PROGRESS;
            case ASSIGNED -> target == ComplaintStatus.IN_PROGRESS;
            case IN_PROGRESS -> target == ComplaintStatus.RESOLVED;
            case RESOLVED -> target == ComplaintStatus.VERIFIED;
            default -> false;
        };

        if (!valid) {
            throw new ConflictException("Invalid status transition: " + current + " → " + target);
        }
    }

    private void validateStaffStatusTransition(ComplaintStatus current, ComplaintStatus target) {
        boolean valid = switch (current) {
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

        // Fetch assignedAt from status history
        LocalDateTime assignedAt = complaintStatusHistoryRepository
                .findFirstByComplaintIdAndNewStatusOrderByChangedAtDesc(complaint.getId(), ComplaintStatus.ASSIGNED)
                .map(ComplaintStatusHistory::getChangedAt)
                .orElse(null);

        return ComplaintResponse.builder()
                .id(complaint.getId())
                .title(complaint.getTitle())
                .description(complaint.getDescription())
                .room(complaint.getRoom())
                .jobType(complaint.getJobType() != null ? complaint.getJobType().name() : null)
                .status(complaint.getStatus().name())
                .studentName(studentName)
                .location(buildingName)
                .buildingId(complaint.getBuildingId())
                .buildingName(buildingName)
                .assignedStaffId(complaint.getAssignedTo())
                .assignedStaffName(assignedStaffName)
                .createdAt(complaint.getCreatedAt())
                .updatedAt(complaint.getUpdatedAt())
                .assignedAt(assignedAt)
                .build();
    }
}

