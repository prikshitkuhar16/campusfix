package com.campusfix.campusfixbackend.user.service;

import com.campusfix.campusfixbackend.common.JobType;
import com.campusfix.campusfixbackend.common.Role;
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

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

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

        UserResponse response = UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .jobType(user.getJobType() != null ? user.getJobType().name() : null)
                .campusId(user.getCampusId())
                .buildingId(user.getBuildingId())
                .invited(user.getInvited())
                .build();

        log.info("UserResponse built: id={}, email={}, name={}, role={}",
                 response.getId(), response.getEmail(), response.getName(), response.getRole());

        return response;
    }

    public User getUserByFirebaseUid(String firebaseUid) {
        return userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new BadCredentialsException("User not found"));
    }
}
