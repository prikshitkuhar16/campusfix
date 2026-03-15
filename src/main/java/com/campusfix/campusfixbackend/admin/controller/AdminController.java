package com.campusfix.campusfixbackend.admin.controller;

import com.campusfix.campusfixbackend.admin.dto.BuildingAdminResponse;
import com.campusfix.campusfixbackend.admin.dto.InviteBuildingAdminRequest;
import com.campusfix.campusfixbackend.admin.dto.InviteListResponse;
import com.campusfix.campusfixbackend.admin.dto.InviteResponse;
import com.campusfix.campusfixbackend.admin.dto.InviteStaffRequest;
import com.campusfix.campusfixbackend.admin.service.AdminService;
import com.campusfix.campusfixbackend.common.MessageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // ==================== Invite Endpoints ====================

    @PostMapping("/invite-building-admin")
    public ResponseEntity<InviteResponse> inviteBuildingAdmin(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody InviteBuildingAdminRequest request) {
        String firebaseUid = jwt.getSubject();
        InviteResponse response = adminService.inviteBuildingAdmin(request, firebaseUid);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/invite-staff")
    public ResponseEntity<InviteResponse> inviteStaff(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody InviteStaffRequest request) {
        String firebaseUid = jwt.getSubject();
        InviteResponse response = adminService.inviteStaff(request, firebaseUid);
        return ResponseEntity.ok(response);
    }

    // ==================== Invite Management ====================

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

    // ==================== Building Admin Management ====================

    @PatchMapping("/building-admins/{adminId}/deactivate")
    public ResponseEntity<BuildingAdminResponse> deactivateBuildingAdmin(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID adminId) {
        String firebaseUid = jwt.getSubject();
        BuildingAdminResponse response = adminService.deactivateBuildingAdmin(adminId, firebaseUid);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/building-admins/{adminId}/activate")
    public ResponseEntity<BuildingAdminResponse> activateBuildingAdmin(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID adminId) {
        String firebaseUid = jwt.getSubject();
        BuildingAdminResponse response = adminService.activateBuildingAdmin(adminId, firebaseUid);
        return ResponseEntity.ok(response);
    }
}
