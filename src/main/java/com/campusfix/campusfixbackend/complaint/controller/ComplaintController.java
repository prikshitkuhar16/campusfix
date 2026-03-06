package com.campusfix.campusfixbackend.complaint.controller;

import com.campusfix.campusfixbackend.complaint.dto.*;
import com.campusfix.campusfixbackend.complaint.service.ComplaintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintService complaintService;

    // ==================== Building Admin Endpoints ====================

    @GetMapping("/complaints")
    public ResponseEntity<ComplaintListResponse> getComplaints(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) String status) {
        String firebaseUid = jwt.getSubject();
        ComplaintListResponse response = complaintService.getBuildingComplaints(firebaseUid, status);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/complaints/{complaintId}")
    public ResponseEntity<ComplaintResponse> getComplaintDetail(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID complaintId) {
        String firebaseUid = jwt.getSubject();
        ComplaintResponse response = complaintService.getComplaintDetail(firebaseUid, complaintId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/complaints/{complaintId}/assign")
    public ResponseEntity<ComplaintResponse> assignComplaint(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID complaintId,
            @Valid @RequestBody AssignComplaintRequest request) {
        String firebaseUid = jwt.getSubject();
        ComplaintResponse response = complaintService.assignComplaint(firebaseUid, complaintId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/complaints/{complaintId}/status")
    public ResponseEntity<ComplaintResponse> updateComplaintStatus(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID complaintId,
            @Valid @RequestBody UpdateComplaintStatusRequest request) {
        String firebaseUid = jwt.getSubject();
        ComplaintResponse response = complaintService.updateComplaintStatus(firebaseUid, complaintId, request);
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

    @GetMapping("/staff/complaints/{complaintId}")
    public ResponseEntity<ComplaintResponse> getStaffComplaintDetail(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID complaintId) {
        String firebaseUid = jwt.getSubject();
        ComplaintResponse response = complaintService.getStaffComplaintDetail(firebaseUid, complaintId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/staff/complaints/{complaintId}/status")
    public ResponseEntity<ComplaintResponse> updateStaffComplaintStatus(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID complaintId,
            @Valid @RequestBody UpdateComplaintStatusRequest request) {
        String firebaseUid = jwt.getSubject();
        ComplaintResponse response = complaintService.updateStaffComplaintStatus(firebaseUid, complaintId, request);
        return ResponseEntity.ok(response);
    }
}

