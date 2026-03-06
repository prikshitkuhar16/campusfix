package com.campusfix.app.data.remote.dto

data class StaffProfileResponse(
    val id: String,
    val name: String?,
    val email: String,
    val role: String,
    val jobType: String?,
    val buildingId: String?,
    val buildingName: String?,
    val campusId: String?,
    val campusName: String?
)

