package com.campusfix.campusfixbackend.admin.service;

import com.campusfix.campusfixbackend.admin.dto.*;
import com.campusfix.campusfixbackend.auth.entity.InviteToken;
import com.campusfix.campusfixbackend.auth.repository.InviteTokenRepository;
import com.campusfix.campusfixbackend.auth.service.TokenGenerator;
import com.campusfix.campusfixbackend.building.entity.Building;
import com.campusfix.campusfixbackend.building.repository.BuildingRepository;
import com.campusfix.campusfixbackend.common.EmailService;
import com.campusfix.campusfixbackend.common.JobType;
import com.campusfix.campusfixbackend.common.Role;
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
public class AdminService {

    private final InviteTokenRepository inviteTokenRepository;
    private final BuildingRepository buildingRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final TokenGenerator tokenGenerator;
    private final EmailService emailService;

    // ==================== POST /admin/invite-building-admin ====================
    // Only CAMPUS_ADMIN can invite a new building admin.

    @Transactional
    public InviteResponse inviteBuildingAdmin(InviteBuildingAdminRequest request, String callerFirebaseUid) {
        User caller = userService.getUserByFirebaseUid(callerFirebaseUid);

        if (caller.getRole() != Role.CAMPUS_ADMIN) {
            throw new ForbiddenException("Only campus admins can invite building admins");
        }

        Building building = buildingRepository.findById(request.getBuildingId())
                .orElseThrow(() -> new ResourceNotFoundException("Building not found"));

        if (!building.getCampusId().equals(caller.getCampusId())) {
            throw new ResourceNotFoundException("Building not found in your campus");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email is already registered");
        }

        String token = tokenGenerator.generateInviteToken();

        InviteToken invite = InviteToken.builder()
                .email(request.getEmail())
                .role(Role.BUILDING_ADMIN)
                .campusId(caller.getCampusId())
                .buildingId(request.getBuildingId())
                .token(token)
                .expiresAt(LocalDateTime.now().plusHours(48))
                .used(false)
                .build();

        inviteTokenRepository.save(invite);

        log.info("Building admin invite created: email={}, campusId={}, buildingId={}, token=https://campusfix.app/invite?token={}",
                request.getEmail(), caller.getCampusId(), request.getBuildingId(), token);

//        emailService.sendInviteEmail(request.getEmail(), token, Role.BUILDING_ADMIN.name());

        return InviteResponse.builder()
                .inviteToken(token)
                .message("Invite sent successfully to " + request.getEmail())
                .build();
    }

    // ==================== POST /admin/invite-staff ====================
    // CAMPUS_ADMIN can invite staff to any building in their campus.
    // BUILDING_ADMIN can invite staff to their own building only.

    @Transactional
    public InviteResponse inviteStaff(InviteStaffRequest request, String callerFirebaseUid) {
        User caller = userService.getUserByFirebaseUid(callerFirebaseUid);

        if (caller.getRole() != Role.CAMPUS_ADMIN && caller.getRole() != Role.BUILDING_ADMIN) {
            throw new ForbiddenException("Only campus admins or building admins can invite staff");
        }

        // Parse and validate job type
        JobType jobType;
        try {
            jobType = JobType.valueOf(request.getJobType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid job type: " + request.getJobType());
        }

        // Determine buildingId based on caller role — never trust frontend for building admin
        UUID effectiveBuildingId;

        if (caller.getRole() == Role.BUILDING_ADMIN) {
            if (caller.getBuildingId() == null) {
                throw new ForbiddenException("Building admin is not assigned to any building");
            }
            effectiveBuildingId = caller.getBuildingId();
        } else {
            // Campus admin must provide buildingId
            if (request.getBuildingId() == null) {
                throw new IllegalArgumentException("Building ID is required for campus admin staff invites");
            }
            Building building = buildingRepository.findById(request.getBuildingId())
                    .orElseThrow(() -> new ResourceNotFoundException("Building not found"));
            if (!building.getCampusId().equals(caller.getCampusId())) {
                throw new ResourceNotFoundException("Building not found in your campus");
            }
            effectiveBuildingId = request.getBuildingId();
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email is already registered");
        }

        String token = tokenGenerator.generateInviteToken();

        InviteToken invite = InviteToken.builder()
                .email(request.getEmail())
                .role(Role.STAFF)
                .jobType(jobType)
                .campusId(caller.getCampusId())
                .buildingId(effectiveBuildingId)
                .token(token)
                .expiresAt(LocalDateTime.now().plusHours(48))
                .used(false)
                .build();

        inviteTokenRepository.save(invite);

        log.info("Staff invite created: email={}, jobType={}, buildingId={}, callerRole={}, token=https://campusfix.app/invite?token={}",
                request.getEmail(), jobType, effectiveBuildingId, caller.getRole(), token);

//        emailService.sendInviteEmail(request.getEmail(), token, Role.STAFF.name());

        return InviteResponse.builder()
                .inviteToken(token)
                .message("Invite sent successfully to " + request.getEmail())
                .build();
    }

    // ==================== GET /admin/invites ====================

    @Transactional(readOnly = true)
    public InviteListResponse getInvites(String callerFirebaseUid) {
        User caller = userService.getUserByFirebaseUid(callerFirebaseUid);
        if (caller.getRole() != Role.CAMPUS_ADMIN) {
            throw new ForbiddenException("Only campus admins can view invites");
        }

        List<InviteToken> pendingInvites = inviteTokenRepository
                .findByCampusIdAndRoleAndUsedFalseOrderByCreatedAtDesc(caller.getCampusId(), Role.BUILDING_ADMIN);

        List<InviteDto> inviteDtos = pendingInvites.stream().map(invite -> {
            String buildingName = null;
            if (invite.getBuildingId() != null) {
                buildingName = buildingRepository.findById(invite.getBuildingId())
                        .map(Building::getName)
                        .orElse(null);
            }

            String status = invite.getUsed() ? "USED" :
                    (invite.getExpiresAt().isBefore(LocalDateTime.now()) ? "EXPIRED" : "PENDING");

            return InviteDto.builder()
                    .id(invite.getId().toString())
                    .email(invite.getEmail())
                    .role(invite.getRole().name())
                    .buildingId(invite.getBuildingId() != null ? invite.getBuildingId().toString() : null)
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

    // ==================== DELETE /admin/invites/{inviteId} ====================

    @Transactional
    public void revokeInvite(String inviteId, String callerFirebaseUid) {
        User caller = userService.getUserByFirebaseUid(callerFirebaseUid);
        if (caller.getRole() != Role.CAMPUS_ADMIN) {
            throw new ForbiddenException("Only campus admins can revoke invites");
        }

        UUID inviteUuid;
        try {
            inviteUuid = UUID.fromString(inviteId);
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException("Invite not found");
        }

        InviteToken invite = inviteTokenRepository.findByIdAndCampusId(inviteUuid, caller.getCampusId())
                .orElseThrow(() -> new ResourceNotFoundException("Invite not found in your campus"));

        if (invite.getUsed()) {
            throw new IllegalArgumentException("Cannot revoke an already used invite");
        }

        inviteTokenRepository.delete(invite);
        log.info("Revoked invite: id={}, email={}, campusId={}", inviteId, invite.getEmail(), caller.getCampusId());
    }

    // ==================== Deactivate Building Admin ====================

    @Transactional
    public BuildingAdminResponse deactivateBuildingAdmin(UUID adminId, String callerFirebaseUid) {
        User caller = userService.getUserByFirebaseUid(callerFirebaseUid);

        if (caller.getRole() != Role.CAMPUS_ADMIN) {
            throw new ForbiddenException("Only campus admins can manage building admins");
        }

        User target = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (target.getRole() != Role.BUILDING_ADMIN) {
            throw new ResourceNotFoundException("User is not a building admin");
        }

        // Verify same campus
        if (!target.getCampusId().equals(caller.getCampusId())) {
            throw new ResourceNotFoundException("Building admin not found in your campus");
        }

        target.setIsActive(false);
        target = userRepository.save(target);
        log.info("Building Admin deactivated: adminId={}, by campusAdmin={}", adminId, caller.getId());

        return toBuildingAdminResponse(target);
    }

    // ==================== Activate Building Admin ====================

    @Transactional
    public BuildingAdminResponse activateBuildingAdmin(UUID adminId, String callerFirebaseUid) {
        User caller = userService.getUserByFirebaseUid(callerFirebaseUid);

        if (caller.getRole() != Role.CAMPUS_ADMIN) {
            throw new ForbiddenException("Only campus admins can manage building admins");
        }

        User target = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (target.getRole() != Role.BUILDING_ADMIN) {
            throw new ResourceNotFoundException("User is not a building admin");
        }

        // Verify same campus
        if (!target.getCampusId().equals(caller.getCampusId())) {
            throw new ResourceNotFoundException("Building admin not found in your campus");
        }

        if (Boolean.TRUE.equals(target.getIsActive())) {
             return toBuildingAdminResponse(target);
        }

        target.setIsActive(true);
        target = userRepository.save(target);
        log.info("Building Admin activated: adminId={}, by campusAdmin={}", adminId, caller.getId());

        return toBuildingAdminResponse(target);
    }

    private BuildingAdminResponse toBuildingAdminResponse(User user) {
        return BuildingAdminResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .isActive(user.getIsActive())
                .build();
    }
}
