package com.campusfix.campusfixbackend.admin.service;

import com.campusfix.campusfixbackend.auth.dto.InviteUserRequest;
import com.campusfix.campusfixbackend.auth.dto.InviteUserResponse;
import com.campusfix.campusfixbackend.auth.entity.InviteToken;
import com.campusfix.campusfixbackend.auth.repository.InviteTokenRepository;
import com.campusfix.campusfixbackend.auth.service.TokenGenerator;
import com.campusfix.campusfixbackend.building.entity.Building;
import com.campusfix.campusfixbackend.building.repository.BuildingRepository;
import com.campusfix.campusfixbackend.common.JobType;
import com.campusfix.campusfixbackend.common.Role;
import com.campusfix.campusfixbackend.user.entity.User;
import com.campusfix.campusfixbackend.user.repository.UserRepository;
import com.campusfix.campusfixbackend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    private static final Set<String> INVITABLE_ROLES = Set.of("STAFF", "BUILDING_ADMIN");

    @Transactional
    public InviteUserResponse inviteUser(InviteUserRequest request, String callerFirebaseUid) {
        // 1. Validate caller is a CAMPUS_ADMIN
        User caller = userService.getUserByFirebaseUid(callerFirebaseUid);

        if (caller.getRole() != Role.CAMPUS_ADMIN) {
            throw new IllegalArgumentException("Only campus admins can invite users");
        }

        // 2. Validate requested role
        String requestedRole = request.getRole().toUpperCase();
        if (!INVITABLE_ROLES.contains(requestedRole)) {
            throw new IllegalArgumentException("Can only invite STAFF or BUILDING_ADMIN roles");
        }
        Role role = Role.valueOf(requestedRole);

        // 3. Validate building belongs to caller's campus
        Building building = buildingRepository.findById(request.getBuildingId())
                .orElseThrow(() -> new IllegalArgumentException("Building not found"));

        if (!building.getCampusId().equals(caller.getCampusId())) {
            throw new IllegalArgumentException("Building does not belong to your campus");
        }

        // 4. Check if email is already registered
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        // 5. Parse jobType if provided (only relevant for STAFF)
        JobType jobType = null;
        if (role == Role.STAFF && request.getJobType() != null && !request.getJobType().isBlank()) {
            try {
                jobType = JobType.valueOf(request.getJobType().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid job type: " + request.getJobType());
            }
        }

        // 6. Generate invite token and save
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

        // Invite link: https://campusfix.app/invite?token=...
        return InviteUserResponse.builder()
                .inviteToken(token)
                .message("Invite sent successfully to " + request.getEmail())
                .build();
    }
}
