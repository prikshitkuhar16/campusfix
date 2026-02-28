package com.campusfix.app.domain.model

data class User(
    val id: String,
    val email: String,
    val name: String,
    val role: UserRole,
    val campusId: String?,
    val buildingIds: List<String>?,
    val isOnboarded: Boolean
)

enum class UserRole {
    STUDENT,
    STAFF,
    BUILDING_ADMIN,
    CAMPUS_ADMIN
}

