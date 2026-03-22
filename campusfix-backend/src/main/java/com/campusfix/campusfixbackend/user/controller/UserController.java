package com.campusfix.campusfixbackend.user.controller;

import com.campusfix.campusfixbackend.user.dto.UpdateProfileRequest;
import com.campusfix.campusfixbackend.user.dto.UserListResponse;
import com.campusfix.campusfixbackend.user.dto.UserResponse;
import com.campusfix.campusfixbackend.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile(@AuthenticationPrincipal Jwt jwt) {
        String firebaseUid = jwt.getSubject();
        UserResponse response = userService.getMyProfile(firebaseUid);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateMyProfile(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateProfileRequest request) {
        String firebaseUid = jwt.getSubject();
        UserResponse response = userService.updateMyProfile(firebaseUid, request);
        return ResponseEntity.ok(response);
    }

// ...existing code...
    @GetMapping
    public ResponseEntity<UserListResponse> getUsers(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) String role) {
        String firebaseUid = jwt.getSubject();
        UserListResponse response = userService.getCampusUsers(firebaseUid, role);
        return ResponseEntity.ok(response);
    }
}

