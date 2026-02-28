package com.campusfix.campusfixbackend.admin.controller;

import com.campusfix.campusfixbackend.admin.service.AdminService;
import com.campusfix.campusfixbackend.auth.dto.InviteUserRequest;
import com.campusfix.campusfixbackend.auth.dto.InviteUserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/invite-user")
    public ResponseEntity<InviteUserResponse> inviteUser(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody InviteUserRequest request) {
        String firebaseUid = jwt.getSubject();
        InviteUserResponse response = adminService.inviteUser(request, firebaseUid);
        return ResponseEntity.ok(response);
    }
}
