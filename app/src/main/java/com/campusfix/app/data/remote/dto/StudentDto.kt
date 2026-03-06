package com.campusfix.app.data.remote.dto

// ── Student Profile ──

data class StudentProfileResponse(
    val id: String,
    val name: String?,
    val email: String,
    val role: String,
    val campusId: String?,
    val campusName: String?
)

// ── Create Complaint ──

data class CreateComplaintRequest(
    val title: String,
    val description: String,
    val buildingId: String,
    val room: String,
    val jobType: String
)

data class CreateComplaintResponse(
    val id: String,
    val title: String,
    val description: String?,
    val room: String?,
    val location: String?,
    val status: String,
    val createdAt: String?,
    val message: String?
)

// ── Verify Resolution ──

data class VerifyResolutionResponse(
    val id: String,
    val title: String,
    val description: String?,
    val studentName: String?,
    val studentEmail: String?,
    val room: String?,
    val location: String?,
    val status: String,
    val assignedStaffId: String?,
    val assignedStaffName: String?,
    val createdAt: String?,
    val updatedAt: String?
)

