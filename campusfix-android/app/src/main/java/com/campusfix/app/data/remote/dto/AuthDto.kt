package com.campusfix.app.data.remote.dto

// ── Login / Resolve ──

data class LoginRequest(
    val idToken: String
)

data class LoginResponse(
    val user: UserDto,
    val message: String
)

data class UserDto(
    val id: String,
    val email: String,
    val name: String?,
    val role: String,
    val campusId: String?,
    val buildingIds: List<String>?,
    val isOnboarded: Boolean
)

// ── OTP ──

data class SendOtpRequest(
    val email: String
)

data class SendOtpResponse(
    val message: String
)

data class VerifyOtpRequest(
    val email: String,
    val otp: String
)

data class VerifyOtpResponse(
    val message: String,
    val verified: Boolean
)

// ── Student creation ──

data class CreateStudentRequest(
    val idToken: String,
    val name: String,
    val email: String,
    val password: String
)

data class CreateStudentResponse(
    val user: UserDto,
    val message: String
)

// ── Invite ──

data class VerifyInviteRequest(
    val token: String
)

data class VerifyInviteResponse(
    val user: UserDto?,
    val email: String?,
    val role: String?,
    val message: String?
)

data class SetPasswordRequest(
    val token: String,
    val password: String
)

data class SetPasswordResponse(
    val message: String
)

data class CompleteInviteRequest(
    val token: String,
    val name: String
)

data class CompleteInviteResponse(
    val user: UserDto,
    val message: String
)
