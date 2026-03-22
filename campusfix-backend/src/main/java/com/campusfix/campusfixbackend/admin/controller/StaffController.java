package com.campusfix.campusfixbackend.admin.controller;

import com.campusfix.campusfixbackend.admin.dto.InviteListResponse;
import com.campusfix.campusfixbackend.admin.dto.StaffListResponse;
import com.campusfix.campusfixbackend.admin.dto.StaffResponse;
import com.campusfix.campusfixbackend.admin.dto.UpdateStaffJobTypeRequest;
import com.campusfix.campusfixbackend.admin.service.BuildingAdminService;
import com.campusfix.campusfixbackend.common.MessageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/staff")
@RequiredArgsConstructor
public class StaffController {

    private final BuildingAdminService buildingAdminService;

    @GetMapping
    public ResponseEntity<StaffListResponse> getStaff(@AuthenticationPrincipal Jwt jwt) {
        String firebaseUid = jwt.getSubject();
        StaffListResponse response = buildingAdminService.getBuildingStaff(firebaseUid);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/invites")
    public ResponseEntity<InviteListResponse> getStaffInvites(@AuthenticationPrincipal Jwt jwt) {
        String firebaseUid = jwt.getSubject();
        InviteListResponse response = buildingAdminService.getStaffInvites(firebaseUid);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/invites/{inviteId}")
    public ResponseEntity<MessageResponse> revokeStaffInvite(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String inviteId) {
        String firebaseUid = jwt.getSubject();
        buildingAdminService.revokeStaffInvite(inviteId, firebaseUid);
        return ResponseEntity.ok(MessageResponse.builder()
                .message("Invite revoked successfully")
                .build());
    }

    @PatchMapping("/{staffId}/deactivate")
    public ResponseEntity<StaffResponse> deactivateStaff(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID staffId) {
        String firebaseUid = jwt.getSubject();
        StaffResponse response = buildingAdminService.deactivateStaff(staffId, firebaseUid);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{staffId}/activate")
    public ResponseEntity<StaffResponse> activateStaff(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID staffId) {
        String firebaseUid = jwt.getSubject();
        StaffResponse response = buildingAdminService.activateStaff(staffId, firebaseUid);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{staffId}/job-type")
    public ResponseEntity<StaffResponse> updateStaffJobType(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID staffId,
            @Valid @RequestBody UpdateStaffJobTypeRequest request) {
        String firebaseUid = jwt.getSubject();
        StaffResponse response = buildingAdminService.updateStaffJobType(staffId, request.getJobType(), firebaseUid);
        return ResponseEntity.ok(response);
    }
}
