package com.campusfix.app.data.remote.api

import com.campusfix.app.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface StudentApiService {

    // ── Complaints ──

    @POST("/complaints")
    suspend fun createComplaint(
        @Header("Authorization") authorization: String,
        @Body request: CreateComplaintRequest
    ): Response<CreateComplaintResponse>

    @GET("/student/complaints")
    suspend fun getMyComplaints(
        @Header("Authorization") authorization: String
    ): Response<ComplaintListResponse>

    @GET("/complaints/{id}")
    suspend fun getComplaintById(
        @Header("Authorization") authorization: String,
        @Path("id") complaintId: String
    ): Response<ComplaintDetailResponse>

    @PATCH("/complaints/{id}/verify")
    suspend fun verifyResolution(
        @Header("Authorization") authorization: String,
        @Path("id") complaintId: String
    ): Response<ComplaintDetailResponse>

    @PATCH("/complaints/{id}/status")
    suspend fun updateComplaintStatus(
        @Header("Authorization") authorization: String,
        @Path("id") complaintId: String,
        @Body request: UpdateStatusRequest
    ): Response<ComplaintDetailResponse>

    // ── Buildings ──

    @GET("/buildings")
    suspend fun getBuildings(
        @Header("Authorization") authorization: String
    ): Response<BuildingListResponse>

    // ── Profile ──

    @GET("/users/me")
    suspend fun getProfile(
        @Header("Authorization") authorization: String
    ): Response<StudentProfileResponse>

    @PUT("/users/me")
    suspend fun updateProfile(
        @Header("Authorization") authorization: String,
        @Body request: UpdateProfileRequest
    ): Response<StudentProfileResponse>
}
