package com.campusfix.campusfixbackend.auth.repository;

import com.campusfix.campusfixbackend.auth.entity.InviteToken;
import com.campusfix.campusfixbackend.common.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InviteTokenRepository extends JpaRepository<InviteToken, UUID> {

    Optional<InviteToken> findByToken(String token);

    Optional<InviteToken> findByEmailAndUsedFalse(String email);

    List<InviteToken> findByCampusIdAndUsedFalseOrderByCreatedAtDesc(UUID campusId);

    List<InviteToken> findByCampusIdAndRoleAndUsedFalseOrderByCreatedAtDesc(UUID campusId, Role role);

    Optional<InviteToken> findByIdAndCampusId(UUID id, UUID campusId);

    List<InviteToken> findByBuildingIdAndUsedFalseOrderByCreatedAtDesc(UUID buildingId);

    List<InviteToken> findByBuildingIdAndRoleAndUsedFalseOrderByCreatedAtDesc(UUID buildingId, Role role);

    Optional<InviteToken> findByIdAndBuildingId(UUID id, UUID buildingId);
}
