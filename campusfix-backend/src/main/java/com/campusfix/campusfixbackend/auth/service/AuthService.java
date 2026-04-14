package com.campusfix.campusfixbackend.auth.service;

import com.campusfix.campusfixbackend.auth.entity.InviteToken;
import com.campusfix.campusfixbackend.auth.entity.OtpVerification;
import com.campusfix.campusfixbackend.campus.entity.Campus;
import com.campusfix.campusfixbackend.campus.repository.CampusRepository;
import com.campusfix.campusfixbackend.common.OtpPurpose;
import com.campusfix.campusfixbackend.common.Role;
import com.campusfix.campusfixbackend.user.dto.UserResponse;
import com.campusfix.campusfixbackend.user.entity.User;
import com.campusfix.campusfixbackend.auth.dto.*;
import com.campusfix.campusfixbackend.auth.repository.InviteTokenRepository;
import com.campusfix.campusfixbackend.auth.repository.OtpVerificationRepository;
import com.campusfix.campusfixbackend.exception.ForbiddenException;
import com.campusfix.campusfixbackend.user.service.UserService;
import com.campusfix.campusfixbackend.common.EmailService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserService userService;
    private final OtpVerificationRepository otpVerificationRepository;
    private final InviteTokenRepository inviteTokenRepository;
    private final CampusRepository campusRepository;
    private final TokenGenerator tokenGenerator;
    private final EmailService emailService;

    public ResolveUserResponse resolveUser(String firebaseUid) {
        User user = userService.getUserByFirebaseUid(firebaseUid);

        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new ForbiddenException("Account is inactive. Please contact support.");
        }

        UserResponse userResponse = userService.toUserResponse(user);

        return ResolveUserResponse.builder()
                .user(userResponse)
                .message("User resolved successfully")
                .build();
    }

    public CheckDomainResponse checkDomain(CheckDomainRequest request) {
        String domain = extractDomain(request.getOfficialEmail());
        boolean exists = campusRepository.existsByDomain(domain);

        return CheckDomainResponse.builder()
                .exists(exists)
                .message(exists
                        ? "Campus with domain '" + domain + "' exists"
                        : "No campus found for domain '" + domain + "'")
                .build();
    }

    @Transactional
    public OtpResponse sendSignupOtp(SendOtpRequest request) {
        if (userService.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        String otp = tokenGenerator.generateOtp();

        OtpVerification otpVerification = OtpVerification.builder()
                .email(request.getEmail())
                .otpCode(otp)
                .purpose(OtpPurpose.SIGNUP)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .verified(false)
                .build();

        otpVerificationRepository.save(otpVerification);

//        emailService.sendStudentLoginOtp(request.getEmail(), otp);
        log.info("Generated signup OTP for {}: {}", request.getEmail(), otp);

        return OtpResponse.builder()
                .message("OTP sent successfully")
                .verified(false)
                .build();
    }

    @Transactional
    public OtpResponse verifySignupOtp(VerifyOtpRequest request) {
        OtpVerification otpVerification = otpVerificationRepository
                .findTopByEmailAndPurposeOrderByCreatedAtDesc(request.getEmail(), OtpPurpose.SIGNUP)
                .orElseThrow(() -> new IllegalArgumentException("OTP not found"));

        if (otpVerification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("OTP expired");
        }

        if (otpVerification.getVerified()) {
            throw new IllegalArgumentException("OTP already used");
        }

        if (!otpVerification.getOtpCode().equals(request.getOtp())) {
            throw new IllegalArgumentException("Invalid OTP");
        }

        otpVerification.setVerified(true);
        otpVerificationRepository.save(otpVerification);

        return OtpResponse.builder()
                .message("OTP verified successfully")
                .verified(true)
                .build();
    }

    @Transactional
    public CreateStudentResponse createStudent(CreateStudentRequest request) {
        log.info("Starting student creation process");

        FirebaseToken decodedToken = verifyFirebaseToken(request.getIdToken());
        String email = decodedToken.getEmail();
        String firebaseUid = decodedToken.getUid();

        log.info("Creating student for email: {}, firebaseUid: {}", email, firebaseUid);

        // Verify OTP
        OtpVerification otpVerification = otpVerificationRepository
                .findTopByEmailAndPurposeOrderByCreatedAtDesc(email, OtpPurpose.SIGNUP)
                .orElseThrow(() -> new IllegalArgumentException("OTP verification not found"));

        if (!otpVerification.getVerified()) {
            throw new IllegalArgumentException("OTP not verified");
        }

        if (userService.existsByEmail(email)) {
            throw new IllegalArgumentException("User already exists");
        }

        // Extract campus domain from email automatically
        String emailDomain = email.substring(email.indexOf('@') + 1);
        log.info("Extracted email domain: {}", emailDomain);

        // Validate campus exists and is active
        Campus campus = campusRepository.findByDomain(emailDomain)
                .orElseThrow(() -> new IllegalArgumentException("No campus found for domain: " + emailDomain + ". Please contact your campus administrator."));

        if (!campus.getIsActive()) {
            throw new IllegalArgumentException("Campus is not active");
        }

        log.info("Found campus: {} (id: {})", campus.getName(), campus.getId());

        // Create student with campus
        User user = userService.createUser(firebaseUid, email, request.getName(), Role.STUDENT, campus.getId(), null, false, null);

        log.info("User created in database. User id: {}, email: {}, name: {}, campusId: {}",
                 user.getId(), user.getEmail(), user.getName(), user.getCampusId());

        UserResponse userResponse = userService.toUserResponse(user);

        log.info("UserResponse created: id={}, email={}, role={}, campusId={}, buildingId={}, invited={}",
                 userResponse.getId(), userResponse.getEmail(), userResponse.getRole(),
                 userResponse.getCampusId(), userResponse.getBuildingId(), userResponse.getInvited());

        CreateStudentResponse response = CreateStudentResponse.builder()
                .user(userResponse)
                .message("Student account created successfully")
                .build();

        log.info("Returning CreateStudentResponse with user and message");

        return response;
    }

    @Transactional
    public OtpResponse sendCampusOtp(SendOtpRequest request) {
        String otp = tokenGenerator.generateOtp();

        OtpVerification otpVerification = OtpVerification.builder()
                .email(request.getEmail())
                .otpCode(otp)
                .purpose(OtpPurpose.CREATE_CAMPUS)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .verified(false)
                .build();

        otpVerificationRepository.save(otpVerification);

//        emailService.sendCampusCreationOtp(request.getEmail(), otp);
        log.info("Generated campus creation OTP for {}: {}", request.getEmail(), otp);

        return OtpResponse.builder()
                .message("OTP sent successfully")
                .verified(false)
                .build();
    }

    @Transactional
    public OtpResponse verifyCampusOtp(VerifyOtpRequest request) {
        OtpVerification otpVerification = otpVerificationRepository
                .findTopByEmailAndPurposeOrderByCreatedAtDesc(request.getEmail(), OtpPurpose.CREATE_CAMPUS)
                .orElseThrow(() -> new IllegalArgumentException("OTP not found"));

        if (otpVerification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("OTP expired");
        }

        if (otpVerification.getVerified()) {
            throw new IllegalArgumentException("OTP already used");
        }

        if (!otpVerification.getOtpCode().equals(request.getOtp())) {
            throw new IllegalArgumentException("Invalid OTP");
        }

        otpVerification.setVerified(true);
        otpVerificationRepository.save(otpVerification);

        return OtpResponse.builder()
                .message("OTP verified successfully")
                .verified(true)
                .build();
    }

    @Transactional(readOnly = true)
    public InviteVerificationResponse verifyInvite(VerifyInviteRequest request) {
        InviteToken inviteToken = inviteTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Invalid invite token"));

        if (inviteToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Invite token expired");
        }

        if (inviteToken.getUsed()) {
            throw new IllegalArgumentException("Invite token already used");
        }

        return InviteVerificationResponse.builder()
                .email(inviteToken.getEmail())
                .role(inviteToken.getRole().name())
                .jobType(inviteToken.getJobType() != null ? inviteToken.getJobType().name() : null)
                .campusId(inviteToken.getCampusId())
                .buildingId(inviteToken.getBuildingId())
                .build();
    }

    @Transactional
    public CompleteInviteResponse completeInvite(CompleteInviteRequest request, String firebaseUid, String email) {
        log.info("Completing invite for email: {}, firebaseUid: {}", email, firebaseUid);

        // 1. Find unused invite for this email
        InviteToken inviteToken = inviteTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Invalid invite token"));

        if (inviteToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Invite token has expired");
        }

        if (inviteToken.getUsed()) {
            throw new IllegalArgumentException("Invite token already used");
        }

        // 2. Validate email matches invite
        if (!email.equals(inviteToken.getEmail())) {
            throw new IllegalArgumentException("Email does not match invite");
        }

        // 3. Ensure user does not already exist
        if (userService.existsByEmail(email)) {
            throw new IllegalArgumentException("User already exists");
        }

        // 4. Create user from invite data (role comes from invite, NOT from frontend)
        User user = userService.createUser(
                firebaseUid,
                email,
                request.getName(),
                inviteToken.getRole(),
                inviteToken.getCampusId(),
                inviteToken.getBuildingId(),
                true,
                inviteToken.getJobType()
        );

        // 5. Mark invite as used
        inviteToken.setUsed(true);
        inviteTokenRepository.save(inviteToken);

        log.info("Invite completed: userId={}, email={}, role={}", user.getId(), email, inviteToken.getRole());

        UserResponse userResponse = userService.toUserResponse(user);

        return CompleteInviteResponse.builder()
                .user(userResponse)
                .message("Account created successfully")
                .build();
    }

    private FirebaseToken verifyFirebaseToken(String idToken) {
        try {
            return FirebaseAuth.getInstance().verifyIdToken(idToken);
        } catch (FirebaseAuthException e) {
            throw new BadCredentialsException("Invalid Firebase token", e);
        }
    }

    private String extractDomain(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex == -1 || atIndex == email.length() - 1) {
            throw new IllegalArgumentException("Invalid email format");
        }
        return email.substring(atIndex + 1);
    }
}
