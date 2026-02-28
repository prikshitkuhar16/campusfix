package com.campusfix.campusfixbackend.auth.controller;

import com.campusfix.campusfixbackend.auth.dto.*;
import com.campusfix.campusfixbackend.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/check-domain")
    public ResponseEntity<CheckDomainResponse> checkDomain(@Valid @RequestBody CheckDomainRequest request) {
        CheckDomainResponse response = authService.checkDomain(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resolve")
    public ResponseEntity<ResolveUserResponse> resolveUser(@AuthenticationPrincipal Jwt jwt) {
        String firebaseUid = jwt.getSubject();
        ResolveUserResponse response = authService.resolveUser(firebaseUid);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/send-signup-otp")
    public ResponseEntity<OtpResponse> sendSignupOtp(@Valid @RequestBody SendOtpRequest request) {
        OtpResponse response = authService.sendSignupOtp(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-signup-otp")
    public ResponseEntity<OtpResponse> verifySignupOtp(@Valid @RequestBody VerifyOtpRequest request) {
        OtpResponse response = authService.verifySignupOtp(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/create-student")
    public ResponseEntity<CreateStudentResponse> createStudent(@Valid @RequestBody CreateStudentRequest request) {
        log.info("Received createStudent request");
        CreateStudentResponse response = authService.createStudent(request);
        log.info("Returning response: {}", response);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/send-campus-otp")
    public ResponseEntity<OtpResponse> sendCampusOtp(@Valid @RequestBody SendOtpRequest request) {
        OtpResponse response = authService.sendCampusOtp(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-campus-otp")
    public ResponseEntity<OtpResponse> verifyCampusOtp(@Valid @RequestBody VerifyOtpRequest request) {
        OtpResponse response = authService.verifyCampusOtp(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-invite")
    public ResponseEntity<InviteVerificationResponse> verifyInvite(@Valid @RequestBody VerifyInviteRequest request) {
        InviteVerificationResponse response = authService.verifyInvite(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/complete-invite")
    public ResponseEntity<CompleteInviteResponse> completeInvite(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CompleteInviteRequest request) {
        String firebaseUid = jwt.getSubject();
        String email = jwt.getClaimAsString("email");
        CompleteInviteResponse response = authService.completeInvite(request, firebaseUid, email);
        return ResponseEntity.ok(response);
    }
}
