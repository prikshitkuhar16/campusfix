package com.campusfix.campusfixbackend.campus.controller;

import com.campusfix.campusfixbackend.campus.dto.CreateCampusRequest;
import com.campusfix.campusfixbackend.campus.dto.CreateCampusResponse;
import com.campusfix.campusfixbackend.campus.service.CampusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/campus")
@RequiredArgsConstructor
public class CampusController {

// ...existing code...
    private final CampusService campusService;

    @PostMapping("/create")
    public ResponseEntity<CreateCampusResponse> createCampus(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateCampusRequest request) {
        String firebaseUid = jwt.getSubject();
        String email = jwt.getClaimAsString("email");
        CreateCampusResponse response = campusService.createCampusWithAdmin(request, firebaseUid, email);
        return ResponseEntity.ok(response);
    }
}
