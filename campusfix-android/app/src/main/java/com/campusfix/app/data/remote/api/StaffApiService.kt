package com.campusfix.app.data.remote.api

import com.campusfix.app.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface StaffApiService {

    // ── Complaints ──

    @GET("/staff/complaints")
    suspend fun getStaffComplaints(
        @Header("Authorization") authorization: String
    ): Response<ComplaintListResponse>

    @GET("/staff/complaints")
    suspend fun getStaffComplaintsByStatus(
        @Header("Authorization") authorization: String,
        @Query("status") status: String
    ): Response<ComplaintListResponse>

    @GET("/complaints/{id}")
    suspend fun getComplaintById(
        @Header("Authorization") authorization: String,
        @Path("id") complaintId: String
    ): Response<ComplaintDetailResponse>

    @PATCH("/complaints/{id}/status")
    suspend fun updateComplaintStatus(
        @Header("Authorization") authorization: String,
        @Path("id") complaintId: String,
        @Body request: UpdateStatusRequest
    ): Response<ComplaintDetailResponse>

    // ── Profile ──

    @GET("/users/me")
    suspend fun getProfile(
        @Header("Authorization") authorization: String
    ): Response<StaffProfileResponse>

    @PUT("/users/me")
    suspend fun updateProfile(
        @Header("Authorization") authorization: String,
        @Body request: UpdateProfileRequest
    ): Response<StaffProfileResponse>
}

