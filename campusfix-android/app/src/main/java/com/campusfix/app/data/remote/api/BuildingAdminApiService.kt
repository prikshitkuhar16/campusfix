package com.campusfix.app.data.remote.api

import com.campusfix.app.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface BuildingAdminApiService {

    // ── Complaints ──

    @GET("/complaints")
    suspend fun getComplaints(
        @Header("Authorization") authorization: String
    ): Response<ComplaintListResponse>

    @GET("/complaints/{id}")
    suspend fun getComplaintById(
        @Header("Authorization") authorization: String,
        @Path("id") complaintId: String
    ): Response<ComplaintDetailResponse>

    @PATCH("/complaints/{id}/assign")
    suspend fun assignStaffToComplaint(
        @Header("Authorization") authorization: String,
        @Path("id") complaintId: String,
        @Body request: AssignStaffRequest
    ): Response<ComplaintDetailResponse>

    @PATCH("/complaints/{id}/status")
    suspend fun updateComplaintStatus(
        @Header("Authorization") authorization: String,
        @Path("id") complaintId: String,
        @Body request: UpdateStatusRequest
    ): Response<ComplaintDetailResponse>

    // ── Staff ──

    @GET("/staff")
    suspend fun getStaff(
        @Header("Authorization") authorization: String
    ): Response<StaffListResponse>

    @GET("/staff")
    suspend fun getStaffByJobType(
        @Header("Authorization") authorization: String,
        @Query("jobType") jobType: String
    ): Response<StaffListResponse>

    @POST("/admin/invite-staff")
    suspend fun inviteStaff(
        @Header("Authorization") authorization: String,
        @Body request: InviteStaffRequest
    ): Response<InviteStaffResponse>

    @PATCH("/staff/{id}/deactivate")
    suspend fun deactivateStaff(
        @Header("Authorization") authorization: String,
        @Path("id") staffId: String
    ): Response<StaffDto>

    @PATCH("/staff/{id}/activate")
    suspend fun activateStaff(
        @Header("Authorization") authorization: String,
        @Path("id") staffId: String
    ): Response<StaffDto>

    @PATCH("/staff/{id}/job-type")
    suspend fun updateStaffJobType(
        @Header("Authorization") authorization: String,
        @Path("id") staffId: String,
        @Body request: UpdateJobTypeRequest
    ): Response<StaffDto>

    // ── Invites ──

    @GET("/staff/invites")
    suspend fun getInvites(
        @Header("Authorization") authorization: String
    ): Response<InviteListResponse>

    @DELETE("/staff/invites/{inviteId}")
    suspend fun revokeInvite(
        @Header("Authorization") authorization: String,
        @Path("inviteId") inviteId: String
    ): Response<MessageResponse>

    // ── Profile ──

    @GET("/users/me")
    suspend fun getProfile(
        @Header("Authorization") authorization: String
    ): Response<BuildingAdminProfileResponse>

    @PUT("/users/me")
    suspend fun updateProfile(
        @Header("Authorization") authorization: String,
        @Body request: UpdateProfileRequest
    ): Response<BuildingAdminProfileResponse>
}
