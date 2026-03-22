package com.campusfix.campusfixbackend.admin.service;

import com.campusfix.campusfixbackend.admin.dto.InviteDto;
import com.campusfix.campusfixbackend.admin.dto.InviteListResponse;
import com.campusfix.campusfixbackend.admin.dto.StaffListResponse;
import com.campusfix.campusfixbackend.admin.dto.StaffResponse;
import com.campusfix.campusfixbackend.auth.entity.InviteToken;
import com.campusfix.campusfixbackend.auth.repository.InviteTokenRepository;
import com.campusfix.campusfixbackend.building.entity.Building;
import com.campusfix.campusfixbackend.building.repository.BuildingRepository;
import com.campusfix.campusfixbackend.common.JobType;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BuildingAdminService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final InviteTokenRepository inviteTokenRepository;
    private final BuildingRepository buildingRepository;

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

    // ==================== Get Staff Invites ====================

    @Transactional(readOnly = true)
    public InviteListResponse getStaffInvites(String firebaseUid) {
        User caller = userService.getUserByFirebaseUid(firebaseUid);
        validateBuildingAdmin(caller);

        List<InviteToken> pendingInvites = inviteTokenRepository
                .findByBuildingIdAndRoleAndUsedFalseOrderByCreatedAtDesc(caller.getBuildingId(), Role.STAFF);

        List<InviteDto> inviteDtos = pendingInvites.stream().map(invite -> {
            String buildingName = buildingRepository.findById(invite.getBuildingId())
                    .map(Building::getName)
                    .orElse(null);

            String status = invite.getUsed() ? "USED" :
                    (invite.getExpiresAt().isBefore(LocalDateTime.now()) ? "EXPIRED" : "PENDING");

            return InviteDto.builder()
                    .id(invite.getId().toString())
                    .email(invite.getEmail())
                    .role(invite.getRole().name())
                    .buildingId(invite.getBuildingId().toString())
                    .buildingName(buildingName)
                    .jobType(invite.getJobType() != null ? invite.getJobType().name() : null)
                    .status(status)
                    .createdAt(invite.getCreatedAt() != null ? invite.getCreatedAt().toString() : null)
                    .build();
        }).toList();

        return InviteListResponse.builder()
                .invites(inviteDtos)
                .build();
    }

    // ==================== Revoke Staff Invite ====================

    @Transactional
    public void revokeStaffInvite(String inviteId, String firebaseUid) {
        User caller = userService.getUserByFirebaseUid(firebaseUid);
        validateBuildingAdmin(caller);

        UUID inviteUuid;
        try {
            inviteUuid = UUID.fromString(inviteId);
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException("Invite not found");
        }

        InviteToken invite = inviteTokenRepository.findByIdAndBuildingId(inviteUuid, caller.getBuildingId())
                .orElseThrow(() -> new ResourceNotFoundException("Invite not found in your building"));

        if (invite.getUsed()) {
            throw new IllegalArgumentException("Cannot revoke an already used invite");
        }

        inviteTokenRepository.delete(invite);
        log.info("Revoked staff invite: id={}, email={}, buildingId={}", inviteId, invite.getEmail(), caller.getBuildingId());
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

    // ==================== Activate Staff ====================

    @Transactional
    public StaffResponse activateStaff(UUID staffId, String firebaseUid) {
        User caller = userService.getUserByFirebaseUid(firebaseUid);
        validateBuildingAdmin(caller);

        // Validate staff belongs to building
        User staff = userRepository.findByIdAndBuildingId(staffId, caller.getBuildingId())
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found in your building"));

        if (staff.getRole() != Role.STAFF) {
            throw new ResourceNotFoundException("Staff not found in your building");
        }

        // Check if already active
        if (Boolean.TRUE.equals(staff.getIsActive())) {
             return toStaffResponse(staff);
        }

        staff.setIsActive(true);
        staff = userRepository.save(staff);
        log.info("Staff activated: staffId={}, by buildingAdmin={}", staffId, caller.getId());

        return toStaffResponse(staff);
    }

    // ==================== Update Staff Job Type ====================

    @Transactional
    public StaffResponse updateStaffJobType(UUID staffId, JobType jobType, String firebaseUid) {
        User caller = userService.getUserByFirebaseUid(firebaseUid);
        validateBuildingAdmin(caller);

        // Validate staff belongs to building
        User staff = userRepository.findByIdAndBuildingId(staffId, caller.getBuildingId())
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found in your building"));

        if (staff.getRole() != Role.STAFF) {
            throw new ResourceNotFoundException("Staff not found in your building");
        }

        staff.setJobType(jobType);
        staff = userRepository.save(staff);
        log.info("Staff job type updated: staffId={}, jobType={}, by buildingAdmin={}", staffId, jobType, caller.getId());

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
                .phoneNumber(user.getPhoneNumber() != null ? user.getPhoneNumber() : null)
                .isActive(user.getIsActive())
                .build();
    }
}
