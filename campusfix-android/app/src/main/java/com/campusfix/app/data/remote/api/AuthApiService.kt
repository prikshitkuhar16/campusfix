package com.campusfix.app.data.remote.api

import com.campusfix.app.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApiService {

    @POST("/auth/resolve")
    suspend fun resolveUserRole(
        @Header("Authorization") authorization: String
    ): Response<LoginResponse>

    @POST("/auth/send-signup-otp")
    suspend fun sendSignupOtp(
        @Body request: SendOtpRequest
    ): Response<SendOtpResponse>

    @POST("/auth/verify-signup-otp")
    suspend fun verifySignupOtp(
        @Body request: VerifyOtpRequest
    ): Response<VerifyOtpResponse>

    @POST("/auth/create-student")
    suspend fun createStudent(
        @Body request: CreateStudentRequest
    ): Response<CreateStudentResponse>

    @POST("/auth/send-campus-otp")
    suspend fun sendCampusOtp(
        @Body request: SendOtpRequest
    ): Response<SendOtpResponse>

    @POST("/auth/verify-campus-otp")
    suspend fun verifyCampusOtp(
        @Body request: VerifyOtpRequest
    ): Response<VerifyOtpResponse>

    @POST("/auth/check-domain")
    suspend fun checkDomain(
        @Body request: CheckDomainRequest
    ): Response<CheckDomainResponse>

    @POST("/campus/create")
    suspend fun createCampus(
        @Header("Authorization") authorization: String,
        @Body request: CreateCampusRequest
    ): Response<CreateCampusResponse>

    @POST("/auth/verify-invite")
    suspend fun verifyInvite(
        @Body request: VerifyInviteRequest
    ): Response<VerifyInviteResponse>

    @POST("/auth/set-password")
    suspend fun setPassword(
        @Body request: SetPasswordRequest
    ): Response<SetPasswordResponse>

    @POST("/auth/complete-invite")
    suspend fun completeInvite(
        @Header("Authorization") authorization: String,
        @Body request: CompleteInviteRequest
    ): Response<CompleteInviteResponse>

}

