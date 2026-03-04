package com.campusfix.campusfixbackend.campus.service;

import com.campusfix.campusfixbackend.auth.entity.OtpVerification;
import com.campusfix.campusfixbackend.common.OtpPurpose;
import com.campusfix.campusfixbackend.common.Role;
import com.campusfix.campusfixbackend.auth.repository.OtpVerificationRepository;
import com.campusfix.campusfixbackend.campus.entity.Campus;
import com.campusfix.campusfixbackend.campus.dto.CreateCampusRequest;
import com.campusfix.campusfixbackend.campus.dto.CreateCampusResponse;
import com.campusfix.campusfixbackend.campus.repository.CampusRepository;
import com.campusfix.campusfixbackend.user.entity.User;
import com.campusfix.campusfixbackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CampusService {

    private final CampusRepository campusRepository;
    private final OtpVerificationRepository otpVerificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public CreateCampusResponse createCampusWithAdmin(CreateCampusRequest request, String firebaseUid, String email) {
        // ...existing code (unchanged)...
        String campusDomain = extractDomain(email);

        // 1. Validate OTP is verified for this email
        OtpVerification otpVerification = otpVerificationRepository
                .findTopByEmailAndPurposeOrderByCreatedAtDesc(email, OtpPurpose.CREATE_CAMPUS)
                .orElseThrow(() -> new IllegalArgumentException("OTP verification not found. Please verify your email first."));

        if (!otpVerification.getVerified()) {
            throw new IllegalArgumentException("OTP not verified. Please verify your email first.");
        }

        if (otpVerification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("OTP has expired. Please request a new one.");
        }

        // 2. Ensure campus domain is not already registered
        if (campusRepository.existsByDomain(campusDomain)) {
            throw new IllegalArgumentException("A campus with domain '" + campusDomain + "' already exists");
        }

        // 3. Ensure user does not already exist
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email is already registered");
        }

        // 4. Create campus
        Campus campus = Campus.builder()
                .name(request.getCampusName())
                .domain(campusDomain)
                .address(request.getCampusAddress())
                .description(request.getDescription())
                .isActive(true)
                .build();

        campus = campusRepository.save(campus);
        log.info("Created campus: id={}, name={}, domain={}", campus.getId(), campus.getName(), campusDomain);

        // 5. Create campus admin user
        User admin = User.builder()
                .firebaseUid(firebaseUid)
                .email(email)
                .name(request.getName())
                .role(Role.CAMPUS_ADMIN)
                .campusId(campus.getId())
                .isActive(true)
                .invited(false)
                .build();

        admin = userRepository.save(admin);
        log.info("Created campus admin: id={}, email={}, campusId={}", admin.getId(), email, campus.getId());

        return CreateCampusResponse.builder()
                .campusId(campus.getId())
                .adminId(admin.getId())
                .message("Campus and admin account created successfully")
                .build();
    }

    private String extractDomain(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex == -1 || atIndex == email.length() - 1) {
            throw new IllegalArgumentException("Invalid email format");
        }
        return email.substring(atIndex + 1);
    }
}
