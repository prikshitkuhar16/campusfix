package com.campusfix.campusfixbackend.admin.service;

import com.campusfix.campusfixbackend.admin.dto.StaffListResponse;
import com.campusfix.campusfixbackend.admin.dto.StaffResponse;
import com.campusfix.campusfixbackend.common.Role;
import com.campusfix.campusfixbackend.exception.ForbiddenException;
import com.campusfix.campusfixbackend.exception.ResourceNotFoundException;
import com.campusfix.campusfixbackend.user.entity.User;
import com.campusfix.campusfixbackend.user.repository.UserRepository;
import com.campusfix.campusfixbackend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BuildingAdminService {

    private final UserRepository userRepository;
    private final UserService userService;

    // ==================== Get Staff List ====================

    @Transactional(readOnly = true)
    public StaffListResponse getBuildingStaff(String firebaseUid) {
        User caller = userService.getUserByFirebaseUid(firebaseUid);
        validateBuildingAdmin(caller);

        List<User> staffMembers = userRepository.findByBuildingIdAndRoleOrderByCreatedAtDesc(
                caller.getBuildingId(), Role.STAFF);

        List<StaffResponse> staffResponses = staffMembers.stream()
                .map(this::toStaffResponse)
                .toList();

        return StaffListResponse.builder()
                .staff(staffResponses)
                .message("Staff fetched successfully")
                .build();
    }

    // ==================== Deactivate Staff ====================

    @Transactional
    public StaffResponse deactivateStaff(UUID staffId, String firebaseUid) {
        User caller = userService.getUserByFirebaseUid(firebaseUid);
        validateBuildingAdmin(caller);

        // Validate staff belongs to building
        User staff = userRepository.findByIdAndBuildingId(staffId, caller.getBuildingId())
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found in your building"));

        if (staff.getRole() != Role.STAFF) {
            throw new ResourceNotFoundException("Staff not found in your building");
        }

        // Cannot deactivate self
        if (staff.getId().equals(caller.getId())) {
            throw new IllegalArgumentException("Cannot deactivate your own account");
        }

        staff.setIsActive(false);
        staff = userRepository.save(staff);
        log.info("Staff deactivated: staffId={}, by buildingAdmin={}", staffId, caller.getId());

        return toStaffResponse(staff);
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

    private StaffResponse toStaffResponse(User user) {
        return StaffResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .jobType(user.getJobType() != null ? user.getJobType().name() : null)
                .isActive(user.getIsActive())
                .build();
    }
}

