package com.campusfix.campusfixbackend.user.service;

import com.campusfix.campusfixbackend.building.entity.Building;
import com.campusfix.campusfixbackend.building.repository.BuildingRepository;
import com.campusfix.campusfixbackend.campus.entity.Campus;
import com.campusfix.campusfixbackend.campus.repository.CampusRepository;
import com.campusfix.campusfixbackend.common.JobType;
import com.campusfix.campusfixbackend.common.Role;
import com.campusfix.campusfixbackend.exception.ForbiddenException;
import com.campusfix.campusfixbackend.exception.ResourceNotFoundException;
import com.campusfix.campusfixbackend.user.dto.UpdateProfileRequest;
import com.campusfix.campusfixbackend.user.dto.UserListResponse;
import com.campusfix.campusfixbackend.user.dto.UserResponse;
import com.campusfix.campusfixbackend.user.entity.User;
import com.campusfix.campusfixbackend.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final BuildingRepository buildingRepository;
    private final CampusRepository campusRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public Optional<User> findByFirebaseUid(String firebaseUid) {
        return userRepository.findByFirebaseUid(firebaseUid);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public User createUser(String firebaseUid, String email, String name, Role role, UUID campusId, UUID buildingId, Boolean invited, JobType jobType) {
        log.info("Creating user: email={}, name={}, role={}, campusId={}, jobType={}", email, name, role, campusId, jobType);

        User user = new User();
        user.setFirebaseUid(firebaseUid);
        user.setEmail(email);
        user.setName(name);
        user.setRole(role);
        user.setJobType(role == Role.STAFF ? jobType : null);
        user.setCampusId(campusId);
        user.setBuildingId(buildingId);
        user.setIsActive(true);
        user.setInvited(invited != null ? invited : false);

        User savedUser = userRepository.save(user);
        log.info("User saved. ID before flush: {}", savedUser.getId());

        // Flush to ensure data is written to database
        entityManager.flush();
        log.info("Flushed to database");

        // Refresh to ensure all database-generated fields (id, timestamps) are loaded
        entityManager.refresh(savedUser);
        log.info("Refreshed from database. ID after refresh: {}, name: {}, createdAt: {}",
                 savedUser.getId(), savedUser.getName(), savedUser.getCreatedAt());

        return savedUser;
    }

    public UserResponse toUserResponse(User user) {
        log.info("Converting User to UserResponse: id={}, email={}, name={}, role={}, campusId={}, buildingId={}, invited={}",
                 user.getId(), user.getEmail(), user.getName(), user.getRole(),
                 user.getCampusId(), user.getBuildingId(), user.getInvited());

        // Resolve building name
        String buildingName = null;
        if (user.getBuildingId() != null) {
            buildingName = buildingRepository.findById(user.getBuildingId())
                    .map(Building::getName)
                    .orElse(null);
        }

        // Resolve campus name
        String campusName = null;
        if (user.getCampusId() != null) {
            campusName = campusRepository.findById(user.getCampusId())
                    .map(Campus::getName)
                    .orElse(null);
        }

        UserResponse response = UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole().name())
                .jobType(user.getJobType() != null ? user.getJobType().name() : null)
                .campusId(user.getCampusId())
                .campusName(campusName)
                .buildingId(user.getBuildingId())
                .buildingName(buildingName)
                .invited(user.getInvited())
                .isActive(user.getIsActive())
                .build();

        log.info("UserResponse built: id={}, email={}, name={}, role={}",
                 response.getId(), response.getEmail(), response.getName(), response.getRole());

        return response;
    }

    public User getUserByFirebaseUid(String firebaseUid) {
        return userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new BadCredentialsException("User not found"));
    }

    // ==================== Profile ====================

    @Transactional(readOnly = true)
    public UserResponse getMyProfile(String firebaseUid) {
        User user = getUserByFirebaseUid(firebaseUid);
        return toUserResponse(user);
    }

    @Transactional
    public UserResponse updateMyProfile(String firebaseUid, UpdateProfileRequest request) {
        User user = getUserByFirebaseUid(firebaseUid);
        user.setName(request.getName());
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        // Allow students to update their building
        if (request.getBuildingId() != null) {
            if (user.getRole() != Role.STUDENT) {
                throw new ForbiddenException("Only students can update their building from profile");
            }
            // Validate building belongs to the student's campus
            buildingRepository.findByIdAndCampusId(request.getBuildingId(), user.getCampusId())
                    .orElseThrow(() -> new ResourceNotFoundException("Building not found in your campus"));
            user.setBuildingId(request.getBuildingId());
        }

        user = userRepository.save(user);
        log.info("Profile updated for user: id={}", user.getId());
        return toUserResponse(user);
    }

    // ==================== Campus Users ====================

    @Transactional(readOnly = true)
    public UserListResponse getCampusUsers(String firebaseUid, String role) {
        User caller = getUserByFirebaseUid(firebaseUid);

        if (caller.getRole() != Role.CAMPUS_ADMIN) {
            throw new ForbiddenException("Only campus admins can view campus users");
        }

        List<User> users;
        if (role != null && !role.isBlank()) {
            try {
                Role filterRole = Role.valueOf(role.toUpperCase());
                users = userRepository.findByCampusIdAndRole(caller.getCampusId(), filterRole);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid role filter: " + role);
            }
        } else {
            users = userRepository.findByCampusId(caller.getCampusId());
        }

        List<UserResponse> userResponses = users.stream()
                .map(this::toUserResponse)
                .toList();

        return UserListResponse.builder()
                .users(userResponses)
                .message("Users fetched successfully")
                .build();
    }

    // ==================== Deactivate User ====================

    @Transactional
    public UserResponse deactivateUser(UUID userId, String callerFirebaseUid) {
        User caller = getUserByFirebaseUid(callerFirebaseUid);

        if (caller.getRole() != Role.CAMPUS_ADMIN) {
            throw new ForbiddenException("Only campus admins can deactivate users");
        }

        User targetUser = userRepository.findByIdAndCampusId(userId, caller.getCampusId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found in your campus"));

        if (targetUser.getId().equals(caller.getId())) {
            throw new IllegalArgumentException("Cannot deactivate your own account");
        }

        targetUser.setIsActive(false);
        targetUser = userRepository.save(targetUser);
        log.info("Deactivated user: id={}, by campusAdmin: id={}", userId, caller.getId());

        return toUserResponse(targetUser);
    }
}
