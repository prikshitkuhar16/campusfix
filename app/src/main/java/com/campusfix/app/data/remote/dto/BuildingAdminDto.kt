package com.campusfix.app.data.remote.dto

// ── Complaints ──

data class ComplaintDto(
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

data class ComplaintListResponse(
    val complaints: List<ComplaintDto>
)

data class ComplaintDetailResponse(
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

data class AssignStaffRequest(
    val staffId: String
)

data class UpdateStatusRequest(
    val status: String
)

// ── Staff (Building Admin context) ──

data class StaffDto(
    val id: String,
    val name: String?,
    val email: String,
    val jobType: String?,
    val isActive: Boolean
)

data class StaffListResponse(
    val staff: List<StaffDto>
)

data class InviteStaffRequest(
    val email: String,
    val jobType: String,
    val buildingId: String? = null
)

data class InviteStaffResponse(
    val inviteToken: String?,
    val message: String
)

data class DeactivateStaffResponse(
    val message: String
)

// ── Building Admin Profile ──

data class BuildingAdminProfileResponse(
    val id: String,
    val name: String?,
    val email: String,
    val role: String,
    val buildingId: String?,
    val buildingName: String?,
    val campusId: String?,
    val campusName: String?
)

