package com.campusfix.campusfixbackend.user.repository;

import com.campusfix.campusfixbackend.common.Role;
import com.campusfix.campusfixbackend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByFirebaseUid(String firebaseUid);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByCampusId(UUID campusId);

    List<User> findByCampusIdAndRole(UUID campusId, Role role);

    Optional<User> findByIdAndCampusId(UUID id, UUID campusId);

    Optional<User> findFirstByBuildingIdAndRole(UUID buildingId, Role role);

    List<User> findByBuildingIdAndRoleOrderByCreatedAtDesc(UUID buildingId, Role role);

    List<User> findByBuildingId(UUID buildingId);

    Optional<User> findByIdAndBuildingId(UUID id, UUID buildingId);
}
