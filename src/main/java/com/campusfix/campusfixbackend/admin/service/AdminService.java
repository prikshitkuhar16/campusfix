package com.campusfix.campusfixbackend.admin.service;

import com.campusfix.campusfixbackend.admin.dto.*;
import com.campusfix.campusfixbackend.auth.dto.InviteUserRequest;
import com.campusfix.campusfixbackend.auth.dto.InviteUserResponse;
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
import java.util.Set;

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

    private static final Set<String> INVITABLE_ROLES = Set.of("STAFF", "BUILDING_ADMIN");

    // ==================== New POST /admin/invites endpoint ====================

    @Transactional
    public InviteResponse createInvite(InviteRequest request, String callerFirebaseUid) {
        // 1. Validate caller is CAMPUS_ADMIN
        User caller = userService.getUserByFirebaseUid(callerFirebaseUid);
        if (caller.getRole() != Role.CAMPUS_ADMIN) {
            throw new ForbiddenException("Only campus admins can send invites");
        }

        // 2. Validate requested role
        String requestedRole = request.getRole().toUpperCase();
        if (!INVITABLE_ROLES.contains(requestedRole)) {
            throw new IllegalArgumentException("Can only invite BUILDING_ADMIN or STAFF roles");
        }
        Role role = Role.valueOf(requestedRole);

        // 3. For BUILDING_ADMIN, buildingId is required and must belong to campus
        if (role == Role.BUILDING_ADMIN) {
            if (request.getBuildingId() == null) {
                throw new IllegalArgumentException("Building ID is required for BUILDING_ADMIN invites");
            }
            Building building = buildingRepository.findById(request.getBuildingId())
                    .orElseThrow(() -> new ResourceNotFoundException("Building not found"));
            if (!building.getCampusId().equals(caller.getCampusId())) {
                throw new ResourceNotFoundException("Building not found in your campus");
            }
        }

        // 4. For STAFF, buildingId is optional but if provided must belong to campus
        if (role == Role.STAFF && request.getBuildingId() != null) {
            Building building = buildingRepository.findById(request.getBuildingId())
                    .orElseThrow(() -> new ResourceNotFoundException("Building not found"));
            if (!building.getCampusId().equals(caller.getCampusId())) {
                throw new ResourceNotFoundException("Building not found in your campus");
            }
        }

        // 5. Check if email is already registered
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email is already registered");
        }

        // 6. Generate secure token and store
        String token = tokenGenerator.generateInviteToken();

        InviteToken invite = InviteToken.builder()
                .email(request.getEmail())
                .role(role)
                .campusId(caller.getCampusId())
                .buildingId(request.getBuildingId())
                .token(token)
                .expiresAt(LocalDateTime.now().plusHours(48))
                .used(false)
                .build();

        inviteTokenRepository.save(invite);

        log.info("Invite created: email={}, role={}, campusId={}, buildingId={}, token={}",
                request.getEmail(), role, caller.getCampusId(), request.getBuildingId(), token);

        // 7. Send email
        emailService.sendInviteEmail(request.getEmail(), token, role.name());

        return InviteResponse.builder()
                .inviteToken(token)
                .message("Invite sent successfully to " + request.getEmail())
                .build();
    }

    // ==================== Legacy endpoints (kept for backward compatibility) ====================

    @Transactional
    public InviteUserResponse inviteUser(InviteUserRequest request, String callerFirebaseUid) {
        User caller = userService.getUserByFirebaseUid(callerFirebaseUid);

        if (caller.getRole() != Role.CAMPUS_ADMIN) {
            throw new ForbiddenException("Only campus admins can invite users");
        }

        String requestedRole = request.getRole().toUpperCase();
        if (!INVITABLE_ROLES.contains(requestedRole)) {
            throw new IllegalArgumentException("Can only invite STAFF or BUILDING_ADMIN roles");
        }
        Role role = Role.valueOf(requestedRole);

        Building building = buildingRepository.findById(request.getBuildingId())
                .orElseThrow(() -> new ResourceNotFoundException("Building not found"));

        if (!building.getCampusId().equals(caller.getCampusId())) {
            throw new ResourceNotFoundException("Building does not belong to your campus");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email is already registered");
        }

        JobType jobType = null;
        if (role == Role.STAFF && request.getJobType() != null && !request.getJobType().isBlank()) {
            try {
                jobType = JobType.valueOf(request.getJobType().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid job type: " + request.getJobType());
            }
        }

        String token = tokenGenerator.generateInviteToken();

        InviteToken invite = InviteToken.builder()
                .email(request.getEmail())
                .role(role)
                .jobType(jobType)
                .campusId(caller.getCampusId())
                .buildingId(request.getBuildingId())
                .token(token)
                .expiresAt(LocalDateTime.now().plusHours(48))
                .used(false)
                .build();

        inviteTokenRepository.save(invite);

        log.info("Invite created: email={}, role={}, campusId={}, buildingId={}, token={}",
                request.getEmail(), role, caller.getCampusId(), request.getBuildingId(), token);

        emailService.sendInviteEmail(request.getEmail(), token, role.name());

        return InviteUserResponse.builder()
                .inviteToken(token)
                .message("Invite sent successfully to " + request.getEmail())
                .build();
    }

    @Transactional
    public InviteBuildingAdminResponse inviteBuildingAdmin(InviteBuildingAdminRequest request, String callerFirebaseUid) {
        User caller = userService.getUserByFirebaseUid(callerFirebaseUid);

        if (caller.getRole() != Role.CAMPUS_ADMIN) {
            throw new ForbiddenException("Only campus admins can invite building admins");
        }

        Building building = buildingRepository.findById(request.getBuildingId())
                .orElseThrow(() -> new ResourceNotFoundException("Building not found"));

        if (!building.getCampusId().equals(caller.getCampusId())) {
            throw new ResourceNotFoundException("Building does not belong to your campus");
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

        log.info("Building admin invite created: email={}, campusId={}, buildingId={}, token={}",
                request.getEmail(), caller.getCampusId(), request.getBuildingId(), token);

        emailService.sendInviteEmail(request.getEmail(), token, Role.BUILDING_ADMIN.name());

        return InviteBuildingAdminResponse.builder()
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
                .findByCampusIdAndUsedFalseOrderByCreatedAtDesc(caller.getCampusId());

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

        java.util.UUID inviteUuid;
        try {
            inviteUuid = java.util.UUID.fromString(inviteId);
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
}
