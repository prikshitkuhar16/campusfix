package com.campusfix.campusfixbackend.auth.repository;

import com.campusfix.campusfixbackend.auth.entity.InviteToken;
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

    Optional<InviteToken> findByIdAndCampusId(UUID id, UUID campusId);
}
