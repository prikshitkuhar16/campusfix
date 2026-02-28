package com.campusfix.app.data.remote.dto

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

data class CheckDomainRequest(
    val officialEmail: String
)

data class CheckDomainResponse(
    val exists: Boolean,
    val message: String?
)

data class CreateCampusRequest(
    val name: String,
    val campusName: String,
    val campusAddress: String,
    val description: String
)

data class CreateCampusResponse(
    val campusId: String,
    val message: String
)

data class VerifyInviteRequest(
    val token: String
)

data class VerifyInviteResponse(
    val user: UserDto,
    val message: String
)

data class SetPasswordRequest(
    val token: String,
    val password: String
)

data class SetPasswordResponse(
    val message: String
)

data class CompleteInviteResponse(
    val user: UserDto,
    val message: String
)

