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
            case STUDENT -> getStudentComplaints(firebaseUid, status);
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

    // ==================== Universal Status Update (PATCH /complaints/{id}/status) ====================

    @Transactional
    public ComplaintResponse updateComplaintStatusByRole(String firebaseUid, UUID complaintId, UpdateComplaintStatusRequest request) {
        User caller = userService.getUserByFirebaseUid(firebaseUid);

        // Parse requested status
        ComplaintStatus newStatus;
        try {
            newStatus = ComplaintStatus.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + request.getStatus());
        }

        return switch (caller.getRole()) {
            case STAFF -> handleStaffStatusUpdate(caller, complaintId, newStatus);
            case STUDENT -> handleStudentStatusUpdate(caller, complaintId, newStatus);
            case BUILDING_ADMIN -> handleBuildingAdminStatusUpdate(caller, complaintId, newStatus);
            default -> throw new ForbiddenException("Your role does not have permission to update complaint status");
        };
    }

    private ComplaintResponse handleStaffStatusUpdate(User staff, UUID complaintId, ComplaintStatus newStatus) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found"));

        // Validate building isolation
        if (!staff.getBuildingId().equals(complaint.getBuildingId())) {
            throw new ForbiddenException("This complaint is not in your building");
        }

        ComplaintStatus currentStatus = complaint.getStatus();

        // Staff can do: CREATED → ASSIGNED, ASSIGNED → RESOLVED
        if (currentStatus == ComplaintStatus.CREATED && newStatus == ComplaintStatus.ASSIGNED) {
            // Validate job_type matches
            if (staff.getJobType() != complaint.getJobType()) {
                throw new ForbiddenException("Your job type does not match this complaint's job type");
            }

            // Atomic pick to prevent race conditions
            int updatedRows = complaintRepository.atomicPickComplaint(
                    complaintId, staff.getId(), ComplaintStatus.ASSIGNED, ComplaintStatus.CREATED);

            if (updatedRows == 0) {
                throw new ConflictException("Complaint is no longer available. It may have been picked by another staff member.");
            }

            saveStatusHistory(complaintId, ComplaintStatus.CREATED, ComplaintStatus.ASSIGNED, staff.getId());

            log.info("Staff started complaint via status update: complaintId={}, staffId={}", complaintId, staff.getId());

            // Refresh to return updated state
            complaint = complaintRepository.findById(complaintId)
                    .orElseThrow(() -> new ResourceNotFoundException("Complaint not found"));

            return toComplaintResponse(complaint);

        } else if (currentStatus == ComplaintStatus.ASSIGNED && newStatus == ComplaintStatus.RESOLVED) {
            // Only the assigned staff or the one who started work can resolve
            if (!staff.getId().equals(complaint.getAssignedTo())) {
                throw new ForbiddenException("Only the assigned staff can resolve this complaint");
            }

            complaint.setStatus(ComplaintStatus.RESOLVED);
            complaint = complaintRepository.save(complaint);

            saveStatusHistory(complaint.getId(), ComplaintStatus.ASSIGNED, ComplaintStatus.RESOLVED, staff.getId());

            log.info("Staff resolved complaint via status update: complaintId={}, staffId={}", complaintId, staff.getId());

            return toComplaintResponse(complaint);

        } else {
            throw new ConflictException("Invalid status transition for STAFF: " + currentStatus + " → " + newStatus);
        }
    }

    private ComplaintResponse handleStudentStatusUpdate(User student, UUID complaintId, ComplaintStatus newStatus) {
        // Student can only do: RESOLVED → VERIFIED, or RESOLVED → ASSIGNED (if not satisfied)
        Complaint complaint = complaintRepository.findByIdAndCreatedBy(complaintId, student.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found or does not belong to you"));

        ComplaintStatus currentStatus = complaint.getStatus();

        if (currentStatus == ComplaintStatus.RESOLVED) {
            if (newStatus == ComplaintStatus.VERIFIED) {
                complaint.setStatus(ComplaintStatus.VERIFIED);
                complaint = complaintRepository.save(complaint);

                saveStatusHistory(complaint.getId(), ComplaintStatus.RESOLVED, ComplaintStatus.VERIFIED, student.getId());
                log.info("Student verified complaint via status update: complaintId={}, studentId={}", complaintId, student.getId());
                return toComplaintResponse(complaint);

            } else if (newStatus == ComplaintStatus.ASSIGNED) {
                complaint.setStatus(ComplaintStatus.ASSIGNED);
                complaint = complaintRepository.save(complaint);

                saveStatusHistory(complaint.getId(), ComplaintStatus.RESOLVED, ComplaintStatus.ASSIGNED, student.getId());
                log.info("Student rejected resolution and moved back to ASSIGNED: complaintId={}, studentId={}", complaintId, student.getId());
                return toComplaintResponse(complaint);
            }
        }

        throw new ConflictException("Invalid status transition for STUDENT: " + currentStatus + " → " + newStatus);
    }

    private ComplaintResponse handleBuildingAdminStatusUpdate(User admin, UUID complaintId, ComplaintStatus newStatus) {
        validateBuildingAdmin(admin);

        Complaint complaint = complaintRepository.findByIdAndBuildingId(complaintId, admin.getBuildingId())
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found in your building"));

        ComplaintStatus currentStatus = complaint.getStatus();

        // Building admin can do: CREATED → ASSIGNED, ASSIGNED → RESOLVED
        // Building admin CANNOT set VERIFIED
        if (newStatus == ComplaintStatus.VERIFIED) {
            throw new ForbiddenException("Building admins cannot verify complaints. Only the student who created the complaint can verify.");
        }

        if (currentStatus == ComplaintStatus.CREATED && newStatus == ComplaintStatus.ASSIGNED) {
            complaint.setStatus(ComplaintStatus.ASSIGNED);
            complaint = complaintRepository.save(complaint);

            saveStatusHistory(complaint.getId(), ComplaintStatus.CREATED, ComplaintStatus.ASSIGNED, admin.getId());

            log.info("Building admin moved complaint to ASSIGNED: complaintId={}, adminId={}", complaintId, admin.getId());

            return toComplaintResponse(complaint);

        } else if (currentStatus == ComplaintStatus.ASSIGNED && newStatus == ComplaintStatus.RESOLVED) {
            complaint.setStatus(ComplaintStatus.RESOLVED);
            complaint = complaintRepository.save(complaint);

            saveStatusHistory(complaint.getId(), ComplaintStatus.ASSIGNED, ComplaintStatus.RESOLVED, admin.getId());

            log.info("Building admin resolved complaint: complaintId={}, adminId={}", complaintId, admin.getId());

            return toComplaintResponse(complaint);

        } else if (currentStatus == ComplaintStatus.RESOLVED && newStatus == ComplaintStatus.ASSIGNED) {
            complaint.setStatus(ComplaintStatus.ASSIGNED);
            complaint = complaintRepository.save(complaint);

            saveStatusHistory(complaint.getId(), ComplaintStatus.RESOLVED, ComplaintStatus.ASSIGNED, admin.getId());

            log.info("Building admin moved complaint back to ASSIGNED: complaintId={}, adminId={}", complaintId, admin.getId());

            return toComplaintResponse(complaint);

        } else {
            throw new ConflictException("Invalid status transition for BUILDING_ADMIN: " + currentStatus + " → " + newStatus);
        }
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
        if (complaint.getStatus() == ComplaintStatus.VERIFIED) {
            throw new ConflictException("Cannot assign complaint with status: " + complaint.getStatus());
        }

        // Only allow assignment when CREATED, ASSIGNED, or RESOLVED
        if (complaint.getStatus() != ComplaintStatus.CREATED && 
            complaint.getStatus() != ComplaintStatus.ASSIGNED && 
            complaint.getStatus() != ComplaintStatus.RESOLVED) {
            throw new ConflictException("Complaint can only be assigned when status is CREATED, ASSIGNED, or RESOLVED. Current status: " + complaint.getStatus());
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

        if (previousStatus == ComplaintStatus.CREATED || previousStatus == ComplaintStatus.RESOLVED) {
            // New assignment or Reopen: CREATED/RESOLVED → ASSIGNED
            complaint.setStatus(ComplaintStatus.ASSIGNED);
        }
        // If ASSIGNED: reassign only (status stays ASSIGNED)

        complaint = complaintRepository.save(complaint);

        // Save status history
        saveStatusHistory(complaint.getId(), previousStatus, complaint.getStatus(), caller.getId());

        log.info("Complaint assigned: complaintId={}, staffId={}, {} -> {}, by buildingAdmin={}",
                complaintId, staff.getId(), previousStatus, complaint.getStatus(), caller.getId());

        return toComplaintResponse(complaint);
    }

// ...existing code...
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

        // Use student's building — do not trust buildingId from frontend
        if (student.getBuildingId() == null) {
            throw new IllegalArgumentException("You must set your building in your profile before creating a complaint");
        }

        UUID buildingId = student.getBuildingId();

        // Validate availability timings
        if (!request.isAvailableAnytime()) {
            if (request.getAvailableFrom() == null || request.getAvailableTo() == null) {
                throw new IllegalArgumentException("Available from and to times are required when not available anytime");
            }
            if (request.getAvailableFrom().isAfter(request.getAvailableTo())) {
                throw new IllegalArgumentException("Available from time must be before available to time");
            }
        }

        Complaint complaint = Complaint.builder()
                .complaint(request.getComplaint())
                .room(request.getRoom())
                .jobType(jobType)
                .status(ComplaintStatus.CREATED)
                .campusId(student.getCampusId())
                .buildingId(buildingId)
                .createdBy(student.getId())
                .assignedTo(null)
                .availableAnytime(request.isAvailableAnytime())
                .availableFrom(request.isAvailableAnytime() ? null : request.getAvailableFrom())
                .availableTo(request.isAvailableAnytime() ? null : request.getAvailableTo())
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
        return getStudentComplaints(firebaseUid, null);
    }

    @Transactional(readOnly = true)
    public ComplaintListResponse getStudentComplaints(String firebaseUid, String status) {
        User student = userService.getUserByFirebaseUid(firebaseUid);
        validateStudent(student);

        List<Complaint> complaints;

        if (status != null && !status.isBlank()) {
            try {
                ComplaintStatus filterStatus = ComplaintStatus.valueOf(status.toUpperCase());
                complaints = complaintRepository.findByCreatedByAndStatusOrderByCreatedAtDesc(student.getId(), filterStatus);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid status filter: " + status);
            }
        } else {
            complaints = complaintRepository.findByCreatedByOrderByCreatedAtDesc(student.getId());
        }

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
                    // ASSIGNED / RESOLVED / VERIFIED — show only complaints assigned to this staff
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

// ...existing code...
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

    // ==================== Helper Methods ====================

    private void validateStudent(User user) {
// ...existing code...
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
// ...existing code...
        if (user.getBuildingId() == null) {
            throw new ForbiddenException("Building admin is not assigned to any building");
        }
    }

    private void saveStatusHistory(UUID complaintId, ComplaintStatus previousStatus, ComplaintStatus newStatus, UUID changedBy) {
// ...existing code...
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
        // Fetch student details
        User student = userRepository.findById(complaint.getCreatedBy())
                .orElse(null);

        ComplaintUser studentResponse = null;
        if (student != null) {
            studentResponse = ComplaintUser.builder()
                    .name(student.getName())
                    .phoneNumber(student.getPhoneNumber())
                    .build();
        }

        // Fetch assigned staff details
        User assignedStaff;
        ComplaintUser assignedStaffResponse = null;

        if (complaint.getAssignedTo() != null) {
            assignedStaff = userRepository.findById(complaint.getAssignedTo())
                    .orElse(null);

            if (assignedStaff != null) {
                assignedStaffResponse = ComplaintUser.builder()
                        .name(assignedStaff.getName())
                        .phoneNumber(assignedStaff.getPhoneNumber())
                        .build();
            }
        }

        // Fetch building name
        String buildingName = buildingRepository.findById(complaint.getBuildingId())
                .map(Building::getName)
                .orElse(null);

        // Fetch assignedAt from status history (assignment happens when status moves to ASSIGNED)
        LocalDateTime assignedAt = complaintStatusHistoryRepository
                .findFirstByComplaintIdAndNewStatusOrderByChangedAtDesc(complaint.getId(), ComplaintStatus.ASSIGNED)
                .map(ComplaintStatusHistory::getChangedAt)
                .orElse(null);

        return ComplaintResponse.builder()
                .id(complaint.getId())
                .complaint(complaint.getComplaint())
                .room(complaint.getRoom())
                .jobType(complaint.getJobType() != null ? complaint.getJobType().name() : null)
                .status(complaint.getStatus().name())
                .student(studentResponse)
                .location(buildingName)
                .buildingId(complaint.getBuildingId())
                .buildingName(buildingName)
                .assignedStaff(assignedStaffResponse)
                .assignedStaffId(complaint.getAssignedTo())
                .availableAnytime(complaint.isAvailableAnytime())
                .availableFrom(complaint.getAvailableFrom())
                .availableTo(complaint.getAvailableTo())
                .createdAt(complaint.getCreatedAt())
                .updatedAt(complaint.getUpdatedAt())
                .assignedAt(assignedAt)
                .build();
    }
}

