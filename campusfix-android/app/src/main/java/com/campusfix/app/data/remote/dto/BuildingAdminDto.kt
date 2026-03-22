package com.campusfix.app.data.remote.dto

// ── Complaints ──

data class ComplaintDto(
    val id: String,
    val complaint: String,
    val studentName: String?,
    val studentEmail: String?,
    val room: String?,
    val location: String?,
    val status: String,
    val jobType: String?,
    val assignedStaffId: String?,
    val assignedStaffName: String?,
    val availableAnytime: Boolean? = null,
    val availableFrom: String? = null,
    val availableTo: String? = null,
    val createdAt: String?,
    val updatedAt: String?
)

data class ComplaintListResponse(
    val complaints: List<ComplaintDto>
)

data class ComplaintStudentInfo(
    val name: String?,
    val email: String?,
    val phoneNumber: String?
)

data class ComplaintAssignedStaffInfo(
    val id: String?,
    val name: String?,
    val phoneNumber: String?
)

data class ComplaintDetailResponse(
    val id: String,
    val complaint: String,
    val student: ComplaintStudentInfo?,
    val room: String?,
    val location: String?,
    val status: String,
    val jobType: String?,
    val assignedStaff: ComplaintAssignedStaffInfo?,
    val availableAnytime: Boolean? = null,
    val availableFrom: String? = null,
    val availableTo: String? = null,
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
    val phoneNumber: String?,
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

data class UpdateJobTypeRequest(
    val jobType: String
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
    val phoneNumber: String? = null,
    val buildingId: String?,
    val buildingName: String?,
    val campusId: String?,
    val campusName: String?
)
