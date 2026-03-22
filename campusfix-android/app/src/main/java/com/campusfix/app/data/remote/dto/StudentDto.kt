package com.campusfix.app.data.remote.dto

// ── Student Profile ──

data class StudentProfileResponse(
    val id: String,
    val name: String?,
    val email: String,
    val role: String,
    val phoneNumber: String? = null,
    val buildingId: String? = null,
    val buildingName: String? = null,
    val campusId: String?,
    val campusName: String?
)

// ── Create Complaint ──

data class CreateComplaintRequest(
    val room: String,
    val jobType: String,
    val complaint: String,
    val availableAnytime: Boolean,
    val availableFrom: String? = null,
    val availableTo: String? = null
)

data class CreateComplaintResponse(
    val id: String,
    val complaint: String,
    val room: String?,
    val location: String?,
    val status: String,
    val availableAnytime: Boolean? = null,
    val availableFrom: String? = null,
    val availableTo: String? = null,
    val createdAt: String?,
    val message: String?
)

// ── Verify Resolution ──

data class VerifyResolutionResponse(
    val id: String,
    val complaint: String,
    val studentName: String?,
    val studentEmail: String?,
    val room: String?,
    val location: String?,
    val status: String,
    val assignedStaffId: String?,
    val assignedStaffName: String?,
    val availableAnytime: Boolean? = null,
    val availableFrom: String? = null,
    val availableTo: String? = null,
    val createdAt: String?,
    val updatedAt: String?
)

