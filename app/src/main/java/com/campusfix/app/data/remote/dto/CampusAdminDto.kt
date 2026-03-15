package com.campusfix.app.data.remote.dto

data class MessageResponse(val message: String)

data class BuildingDetailResponse(
    val id: String,
    val number: String,
    val name: String,
    val description: String?,
    val campusId: String,
    val adminId: String?,
    val adminName: String?,
    val adminEmail: String?
)

data class UpdateBuildingRequest(
    val number: String,
    val name: String,
    val description: String?
)

data class CampusUserDto(
    val id: String,
    val name: String?,
    val email: String,
    val role: String,
    val buildingId: String?,
    val buildingName: String?,
    val isActive: Boolean,
    val phoneNumber: String? = null
)

data class UserListResponse(val users: List<CampusUserDto>)

data class InviteUserRequest(
    val email: String,
    val role: String,
    val buildingId: String? = null
)

data class InviteUserResponse(val message: String)

// ── Invite Building Admin ──

data class InviteBuildingAdminRequest(
    val email: String,
    val buildingId: String
)

data class InviteBuildingAdminResponse(
    val inviteToken: String?,
    val message: String
)

data class BuildingAdminResponse(
    val id: String,
    val name: String?,
    val email: String,
    val phoneNumber: String?,
    val isActive: Boolean
)

// ── Invite Staff (Campus Admin context) ──

data class InviteStaffByCampusAdminRequest(
    val email: String,
    val jobType: String,
    val buildingId: String
)

data class InviteStaffByCampusAdminResponse(
    val inviteToken: String?,
    val message: String
)

data class ProfileResponse(
    val id: String,
    val name: String?,
    val email: String,
    val role: String,
    val phoneNumber: String? = null,
    val campusId: String?,
    val campusName: String?
)

data class UpdateProfileRequest(
    val name: String,
    val phoneNumber: String? = null,
    val buildingId: String? = null
)

// ── Invites ──

data class InviteDto(
    val id: String,
    val email: String,
    val role: String,
    val buildingId: String?,
    val buildingName: String?,
    val status: String,
    val createdAt: String?,
    val jobType: String? = null
)

data class InviteListResponse(val invites: List<InviteDto>)

// ── Assign Building Admin ──

data class AssignBuildingAdminRequest(val userId: String)
