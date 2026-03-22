package com.campusfix.app.data.remote.mapper

import com.campusfix.app.data.remote.dto.UserDto
import com.campusfix.app.domain.model.User
import com.campusfix.app.domain.model.UserRole

fun UserDto.toDomain(): User {
    return User(
        id = this.id,
        email = this.email,
        name = this.name ?: "",
        role = when (this.role) {
            "STUDENT" -> UserRole.STUDENT
            "STAFF" -> UserRole.STAFF
            "BUILDING_ADMIN" -> UserRole.BUILDING_ADMIN
            "CAMPUS_ADMIN" -> UserRole.CAMPUS_ADMIN
            else -> throw IllegalArgumentException("Unknown role: ${this.role}")
        },
        campusId = this.campusId,
        buildingIds = this.buildingIds,
        isOnboarded = this.isOnboarded
    )
}

