package com.campusfix.app.data.remote.api

import com.campusfix.app.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface CampusAdminApiService {

    // ── Buildings ──

    @GET("/buildings")
    suspend fun getBuildings(
        @Header("Authorization") authorization: String
    ): Response<BuildingListResponse>

    @POST("/buildings")
    suspend fun createBuilding(
        @Header("Authorization") authorization: String,
        @Body request: CreateBuildingRequest
    ): Response<CreateBuildingResponse>

    @GET("/buildings/{id}")
    suspend fun getBuildingById(
        @Header("Authorization") authorization: String,
        @Path("id") buildingId: String
    ): Response<BuildingDetailResponse>

    @PUT("/buildings/{id}")
    suspend fun updateBuilding(
        @Header("Authorization") authorization: String,
        @Path("id") buildingId: String,
        @Body request: UpdateBuildingRequest
    ): Response<BuildingDetailResponse>

    @DELETE("/buildings/{id}")
    suspend fun deleteBuilding(
        @Header("Authorization") authorization: String,
        @Path("id") buildingId: String
    ): Response<MessageResponse>

    // ── Users ──

    @GET("/users")
    suspend fun getUsers(
        @Header("Authorization") authorization: String,
        @Query("role") role: String
    ): Response<UserListResponse>

    @POST("/admin/invite-building-admin")
    suspend fun inviteBuildingAdmin(
        @Header("Authorization") authorization: String,
        @Body request: InviteBuildingAdminRequest
    ): Response<InviteBuildingAdminResponse>

    @PATCH("/admin/building-admins/{id}/deactivate")
    suspend fun deactivateBuildingAdmin(
        @Header("Authorization") authorization: String,
        @Path("id") adminId: String
    ): Response<BuildingAdminResponse>

    @PATCH("/admin/building-admins/{id}/activate")
    suspend fun activateBuildingAdmin(
        @Header("Authorization") authorization: String,
        @Path("id") adminId: String
    ): Response<BuildingAdminResponse>

    // ── Invites ──

    @GET("/admin/invites")
    suspend fun getInvites(
        @Header("Authorization") authorization: String
    ): Response<InviteListResponse>

    @DELETE("/admin/invites/{inviteId}")
    suspend fun revokeInvite(
        @Header("Authorization") authorization: String,
        @Path("inviteId") inviteId: String
    ): Response<MessageResponse>

    // ── Assign Building Admin ──

    @PUT("/buildings/{id}/admin")
    suspend fun assignBuildingAdmin(
        @Header("Authorization") authorization: String,
        @Path("id") buildingId: String,
        @Body request: AssignBuildingAdminRequest
    ): Response<BuildingDetailResponse>

    // ── Profile ──

    @GET("/users/me")
    suspend fun getProfile(
        @Header("Authorization") authorization: String
    ): Response<ProfileResponse>

    @PUT("/users/me")
    suspend fun updateProfile(
        @Header("Authorization") authorization: String,
        @Body request: UpdateProfileRequest
    ): Response<ProfileResponse>
}
