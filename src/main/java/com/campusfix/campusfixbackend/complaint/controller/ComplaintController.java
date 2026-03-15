package com.campusfix.campusfixbackend.complaint.controller;

import com.campusfix.campusfixbackend.complaint.dto.*;
import com.campusfix.campusfixbackend.complaint.service.ComplaintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintService complaintService;

    // ==================== Shared Endpoints ====================

    @GetMapping("/complaints")
    public ResponseEntity<ComplaintListResponse> getComplaints(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) String status) {
        String firebaseUid = jwt.getSubject();
        ComplaintListResponse response = complaintService.getComplaintsByRole(firebaseUid, status);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/complaints/{complaintId}")
    public ResponseEntity<ComplaintResponse> getComplaintDetail(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID complaintId) {
        String firebaseUid = jwt.getSubject();
        ComplaintResponse response = complaintService.getComplaintDetailByRole(firebaseUid, complaintId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/complaints/{complaintId}/status")
    public ResponseEntity<ComplaintResponse> updateComplaintStatusByRole(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID complaintId,
            @Valid @RequestBody UpdateComplaintStatusRequest request) {
        String firebaseUid = jwt.getSubject();
        ComplaintResponse response = complaintService.updateComplaintStatusByRole(firebaseUid, complaintId, request);
        return ResponseEntity.ok(response);
    }

    // ==================== Student Endpoints ====================

    @PostMapping("/complaints")
    public ResponseEntity<ComplaintResponse> createComplaint(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateComplaintRequest request) {
        String firebaseUid = jwt.getSubject();
        ComplaintResponse response = complaintService.createComplaint(firebaseUid, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/student/complaints")
    public ResponseEntity<ComplaintListResponse> getStudentComplaints(
            @AuthenticationPrincipal Jwt jwt) {
        String firebaseUid = jwt.getSubject();
        ComplaintListResponse response = complaintService.getStudentComplaints(firebaseUid);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/complaints/{complaintId}/verify")
    public ResponseEntity<ComplaintResponse> verifyComplaint(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID complaintId) {
        String firebaseUid = jwt.getSubject();
        ComplaintResponse response = complaintService.verifyComplaint(firebaseUid, complaintId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/complaints/{complaintId}/assign")
    public ResponseEntity<ComplaintResponse> assignComplaintByAdmin(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID complaintId,
            @Valid @RequestBody AssignComplaintRequest request) {
        String firebaseUid = jwt.getSubject();
        ComplaintResponse response = complaintService.assignComplaint(firebaseUid, complaintId, request);
        return ResponseEntity.ok(response);
    }

    // ==================== Staff Endpoints ====================

    @GetMapping("/staff/complaints")
    public ResponseEntity<ComplaintListResponse> getStaffComplaints(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) String status) {
        String firebaseUid = jwt.getSubject();
        ComplaintListResponse response = complaintService.getStaffComplaints(firebaseUid, status);
        return ResponseEntity.ok(response);
    }
}
