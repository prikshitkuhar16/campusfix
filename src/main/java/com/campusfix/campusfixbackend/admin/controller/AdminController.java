package com.campusfix.campusfixbackend.admin.controller;

import com.campusfix.campusfixbackend.admin.dto.InviteBuildingAdminRequest;
import com.campusfix.campusfixbackend.admin.dto.InviteBuildingAdminResponse;
import com.campusfix.campusfixbackend.admin.dto.InviteListResponse;
import com.campusfix.campusfixbackend.admin.dto.InviteRequest;
import com.campusfix.campusfixbackend.admin.dto.InviteResponse;
import com.campusfix.campusfixbackend.admin.service.AdminService;
import com.campusfix.campusfixbackend.auth.dto.InviteUserRequest;
import com.campusfix.campusfixbackend.auth.dto.InviteUserResponse;
import com.campusfix.campusfixbackend.common.MessageResponse;
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

    @PostMapping("/invites")
    public ResponseEntity<InviteResponse> createInvite(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody InviteRequest request) {
        String firebaseUid = jwt.getSubject();
        InviteResponse response = adminService.createInvite(request, firebaseUid);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/invites")
    public ResponseEntity<InviteListResponse> getInvites(@AuthenticationPrincipal Jwt jwt) {
        String firebaseUid = jwt.getSubject();
        InviteListResponse response = adminService.getInvites(firebaseUid);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/invites/{inviteId}")
    public ResponseEntity<MessageResponse> revokeInvite(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String inviteId) {
        String firebaseUid = jwt.getSubject();
        adminService.revokeInvite(inviteId, firebaseUid);
        return ResponseEntity.ok(MessageResponse.builder()
                .message("Invite revoked successfully")
                .build());
    }

    @PostMapping("/invite-user")
    public ResponseEntity<InviteUserResponse> inviteUser(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody InviteUserRequest request) {
        String firebaseUid = jwt.getSubject();
        InviteUserResponse response = adminService.inviteUser(request, firebaseUid);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/invite-building-admin")
    public ResponseEntity<InviteBuildingAdminResponse> inviteBuildingAdmin(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody InviteBuildingAdminRequest request) {
        String firebaseUid = jwt.getSubject();
        InviteBuildingAdminResponse response = adminService.inviteBuildingAdmin(request, firebaseUid);
        return ResponseEntity.ok(response);
    }
}
