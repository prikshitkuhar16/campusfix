package com.campusfix.app.domain.repository

import com.campusfix.app.core.util.Resource
import com.campusfix.app.data.remote.dto.*

interface CampusAdminRepository {

    // ── Buildings ──
    suspend fun getBuildings(): Resource<List<BuildingDto>>
    suspend fun createBuilding(name: String, description: String, number: String): Resource<String>
    suspend fun getBuildingById(buildingId: String): Resource<BuildingDetailResponse>
    suspend fun updateBuilding(buildingId: String, name: String, description: String?, number: String?): Resource<BuildingDetailResponse>
    suspend fun deleteBuilding(buildingId: String): Resource<String>

    // ── Users ──
    suspend fun getUsers(role: String): Resource<List<CampusUserDto>>
    suspend fun inviteBuildingAdmin(email: String, buildingId: String): Resource<String>
    suspend fun deactivateBuildingAdmin(adminId: String): Resource<String>
    suspend fun activateBuildingAdmin(adminId: String): Resource<String>

    // ── Invites ──
    suspend fun getInvites(): Resource<List<InviteDto>>
    suspend fun revokeInvite(inviteId: String): Resource<String>

    // ── Assign Building Admin ──
    suspend fun assignBuildingAdmin(buildingId: String, userId: String): Resource<BuildingDetailResponse>

    // ── Profile ──
    suspend fun getProfile(): Resource<ProfileResponse>
    suspend fun updateProfile(name: String, phoneNumber: String?): Resource<ProfileResponse>
}
