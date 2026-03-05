package com.campusfix.campusfixbackend.admin.controller;

import com.campusfix.campusfixbackend.admin.dto.StaffListResponse;
import com.campusfix.campusfixbackend.admin.dto.StaffResponse;
import com.campusfix.campusfixbackend.admin.service.BuildingAdminService;
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

    @PatchMapping("/{staffId}/deactivate")
    public ResponseEntity<StaffResponse> deactivateStaff(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID staffId) {
        String firebaseUid = jwt.getSubject();
        StaffResponse response = buildingAdminService.deactivateStaff(staffId, firebaseUid);
        return ResponseEntity.ok(response);
    }
}

