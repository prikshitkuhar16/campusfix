package com.campusfix.campusfixbackend.auth.repository;

import com.campusfix.campusfixbackend.auth.entity.OtpVerification;
import com.campusfix.campusfixbackend.common.OtpPurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, UUID> {

    Optional<OtpVerification> findTopByEmailAndPurposeOrderByCreatedAtDesc(
        String email,
        OtpPurpose purpose
    );

    void deleteByExpiresAtBefore(LocalDateTime dateTime);
}
