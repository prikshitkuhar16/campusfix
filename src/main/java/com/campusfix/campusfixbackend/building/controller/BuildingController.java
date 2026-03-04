package com.campusfix.campusfixbackend.building.controller;

import com.campusfix.campusfixbackend.building.dto.*;
import com.campusfix.campusfixbackend.building.service.BuildingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/buildings")
@RequiredArgsConstructor
public class BuildingController {

    private final BuildingService buildingService;

    @GetMapping
    public ResponseEntity<BuildingListResponse> getBuildings(@AuthenticationPrincipal Jwt jwt) {
        String firebaseUid = jwt.getSubject();
        BuildingListResponse response = buildingService.getBuildings(firebaseUid);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<CreateBuildingResponse> createBuilding(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateBuildingRequest request) {
        String firebaseUid = jwt.getSubject();
        CreateBuildingResponse response = buildingService.createBuilding(request, firebaseUid);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{buildingId}")
    public ResponseEntity<BuildingResponse> getBuildingById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID buildingId) {
        String firebaseUid = jwt.getSubject();
        BuildingResponse response = buildingService.getBuildingById(buildingId, firebaseUid);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{buildingId}")
    public ResponseEntity<BuildingResponse> updateBuilding(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID buildingId,
            @Valid @RequestBody UpdateBuildingRequest request) {
        String firebaseUid = jwt.getSubject();
        BuildingResponse response = buildingService.updateBuilding(buildingId, request, firebaseUid);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{buildingId}")
    public ResponseEntity<Void> deleteBuilding(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID buildingId) {
        String firebaseUid = jwt.getSubject();
        buildingService.deleteBuilding(buildingId, firebaseUid);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{buildingId}/admin")
    public ResponseEntity<BuildingDetailResponse> assignBuildingAdmin(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID buildingId,
            @Valid @RequestBody AssignBuildingAdminRequest request) {
        String firebaseUid = jwt.getSubject();
        BuildingDetailResponse response = buildingService.assignBuildingAdmin(buildingId, request, firebaseUid);
        return ResponseEntity.ok(response);
    }
}
