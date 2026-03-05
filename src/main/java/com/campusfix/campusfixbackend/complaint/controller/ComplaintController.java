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
@RequestMapping("/complaints")
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintService complaintService;

    @GetMapping
    public ResponseEntity<ComplaintListResponse> getComplaints(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) String status) {
        String firebaseUid = jwt.getSubject();
        ComplaintListResponse response = complaintService.getBuildingComplaints(firebaseUid, status);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{complaintId}")
    public ResponseEntity<ComplaintResponse> getComplaintDetail(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID complaintId) {
        String firebaseUid = jwt.getSubject();
        ComplaintResponse response = complaintService.getComplaintDetail(firebaseUid, complaintId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{complaintId}/assign")
    public ResponseEntity<ComplaintResponse> assignComplaint(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID complaintId,
            @Valid @RequestBody AssignComplaintRequest request) {
        String firebaseUid = jwt.getSubject();
        ComplaintResponse response = complaintService.assignComplaint(firebaseUid, complaintId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{complaintId}/status")
    public ResponseEntity<ComplaintResponse> updateComplaintStatus(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID complaintId,
            @Valid @RequestBody UpdateComplaintStatusRequest request) {
        String firebaseUid = jwt.getSubject();
        ComplaintResponse response = complaintService.updateComplaintStatus(firebaseUid, complaintId, request);
        return ResponseEntity.ok(response);
    }
}

