package com.campusfix.app.data.repository

import android.util.Log
import com.campusfix.app.core.firebase.FirebaseAuthManager
import com.campusfix.app.core.util.Resource
import com.campusfix.app.data.remote.RetrofitClient
import com.campusfix.app.data.remote.dto.*
import com.campusfix.app.domain.repository.CampusAdminRepository

class CampusAdminRepositoryImpl(
    private val firebaseAuthManager: FirebaseAuthManager
) : CampusAdminRepository {

    private val api = RetrofitClient.campusAdminApiService

    private suspend fun getAuthHeader(): String? {
        val token = firebaseAuthManager.getIdToken(true)
        return if (token != null) "Bearer $token" else null
    }

    // ── Buildings ──

    override suspend fun getBuildings(): Resource<List<BuildingDto>> {
        return try {
            val auth = getAuthHeader() ?: return Resource.Error("Authentication failed")
            val response = api.getBuildings(auth)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.buildings)
            } else {
                val err = response.errorBody()?.string() ?: response.message()
                Resource.Error(err ?: "Failed to fetch buildings")
            }
        } catch (e: Exception) {
            Log.e("CampusAdminRepo", "getBuildings: ${e.message}", e)
            Resource.Error(e.message ?: "Failed to fetch buildings")
        }
    }

    override suspend fun createBuilding(
        name: String,
        description: String,
        number: String
    ): Resource<String> {
        return try {
            val auth = getAuthHeader() ?: return Resource.Error("Authentication failed")
            val response = api.createBuilding(
                authorization = auth,
                request = CreateBuildingRequest(
                    number = number,
                    name = name
                )
            )
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.message)
            } else {
                val err = response.errorBody()?.string() ?: response.message()
                Resource.Error(err ?: "Failed to create building")
            }
        } catch (e: Exception) {
            Log.e("CampusAdminRepo", "createBuilding: ${e.message}", e)
            Resource.Error(e.message ?: "Failed to create building")
        }
    }

    override suspend fun getBuildingById(buildingId: String): Resource<BuildingDetailResponse> {
        return try {
            val auth = getAuthHeader() ?: return Resource.Error("Authentication failed")
            val response = api.getBuildingById(auth, buildingId)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val err = response.errorBody()?.string() ?: response.message()
                Resource.Error(err ?: "Failed to fetch building details")
            }
        } catch (e: Exception) {
            Log.e("CampusAdminRepo", "getBuildingById: ${e.message}", e)
            Resource.Error(e.message ?: "Failed to fetch building details")
        }
    }

    override suspend fun updateBuilding(
        buildingId: String,
        name: String,
        description: String?,
        number: String?
    ): Resource<BuildingDetailResponse> {
        return try {
            val auth = getAuthHeader() ?: return Resource.Error("Authentication failed")
            val response = api.updateBuilding(
                authorization = auth,
                buildingId = buildingId,
                request = UpdateBuildingRequest(
                    number = number ?: "",
                    name = name,
                    description = description
                )
            )
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val err = response.errorBody()?.string() ?: response.message()
                Resource.Error(err ?: "Failed to update building")
            }
        } catch (e: Exception) {
            Log.e("CampusAdminRepo", "updateBuilding: ${e.message}", e)
            Resource.Error(e.message ?: "Failed to update building")
        }
    }

    override suspend fun deleteBuilding(buildingId: String): Resource<String> {
        return try {
            val auth = getAuthHeader() ?: return Resource.Error("Authentication failed")
            val response = api.deleteBuilding(auth, buildingId)
            if (response.isSuccessful) {
                // Handle 204 No Content which has no body
                val message = response.body()?.message ?: "Building deleted successfully"
                Resource.Success(message)
            } else {
                val err = response.errorBody()?.string() ?: response.message()
                Resource.Error(err ?: "Failed to delete building")
            }
        } catch (e: Exception) {
            Log.e("CampusAdminRepo", "deleteBuilding: ${e.message}", e)
            Resource.Error(e.message ?: "Failed to delete building")
        }
    }

    // ── Users ──

    override suspend fun getUsers(role: String): Resource<List<CampusUserDto>> {
        return try {
            val auth = getAuthHeader() ?: return Resource.Error("Authentication failed")
            val response = api.getUsers(auth, role)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.users)
            } else {
                val err = response.errorBody()?.string() ?: response.message()
                Resource.Error(err ?: "Failed to fetch users")
            }
        } catch (e: Exception) {
            Log.e("CampusAdminRepo", "getUsers: ${e.message}", e)
            Resource.Error(e.message ?: "Failed to fetch users")
        }
    }

    override suspend fun inviteBuildingAdmin(
        email: String,
        buildingId: String
    ): Resource<String> {
        return try {
            val auth = getAuthHeader() ?: return Resource.Error("Authentication failed")
            val response = api.inviteBuildingAdmin(
                authorization = auth,
                request = InviteBuildingAdminRequest(email = email, buildingId = buildingId)
            )
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.message)
            } else {
                val err = response.errorBody()?.string() ?: response.message()
                Resource.Error(err ?: "Failed to invite building admin")
            }
        } catch (e: Exception) {
            Log.e("CampusAdminRepo", "inviteBuildingAdmin: ${e.message}", e)
            Resource.Error(e.message ?: "Failed to invite building admin")
        }
    }

    // ── Building Admins ──

    override suspend fun deactivateBuildingAdmin(adminId: String): Resource<String> {
        return try {
            val auth = getAuthHeader() ?: return Resource.Error("Authentication failed")
            val response = api.deactivateBuildingAdmin(auth, adminId)

            if (response.isSuccessful) {
                val admin = response.body()
                val message = if (admin != null) {
                    "Building admin ${admin.name ?: admin.email} deactivated successfully"
                } else {
                    "Building admin deactivated successfully"
                }
                Resource.Success(message)
            } else {
                val err = response.errorBody()?.string() ?: response.message()
                Resource.Error(err ?: "Failed to deactivate building admin")
            }
        } catch (e: Exception) {
            Log.e("CampusAdminRepo", "deactivateBuildingAdmin: ${e.message}", e)
            Resource.Error(e.message ?: "Failed to deactivate building admin")
        }
    }

    override suspend fun activateBuildingAdmin(adminId: String): Resource<String> {
        return try {
            val auth = getAuthHeader() ?: return Resource.Error("Authentication failed")
            val response = api.activateBuildingAdmin(auth, adminId)

            if (response.isSuccessful) {
                val admin = response.body()
                val message = if (admin != null) {
                    "Building admin ${admin.name ?: admin.email} activated successfully"
                } else {
                    "Building admin activated successfully"
                }
                Resource.Success(message)
            } else {
                val err = response.errorBody()?.string() ?: response.message()
                Resource.Error(err ?: "Failed to activate building admin")
            }
        } catch (e: Exception) {
            Log.e("CampusAdminRepo", "activateBuildingAdmin: ${e.message}", e)
            Resource.Error(e.message ?: "Failed to activate building admin")
        }
    }

    // ── Invites ──

    override suspend fun getInvites(): Resource<List<InviteDto>> {
        return try {
            val auth = getAuthHeader() ?: return Resource.Error("Authentication failed")
            val response = api.getInvites(auth)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.invites)
            } else {
                val err = response.errorBody()?.string() ?: response.message()
                Resource.Error(err ?: "Failed to fetch invites")
            }
        } catch (e: Exception) {
            Log.e("CampusAdminRepo", "getInvites: ${e.message}", e)
            Resource.Error(e.message ?: "Failed to fetch invites")
        }
    }

    override suspend fun revokeInvite(inviteId: String): Resource<String> {
        return try {
            val auth = getAuthHeader() ?: return Resource.Error("Authentication failed")
            val response = api.revokeInvite(auth, inviteId)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.message)
            } else {
                val err = response.errorBody()?.string() ?: response.message()
                Resource.Error(err ?: "Failed to revoke invite")
            }
        } catch (e: Exception) {
            Log.e("CampusAdminRepo", "revokeInvite: ${e.message}", e)
            Resource.Error(e.message ?: "Failed to revoke invite")
        }
    }

    // ── Assign Building Admin ──

    override suspend fun assignBuildingAdmin(
        buildingId: String,
        userId: String
    ): Resource<BuildingDetailResponse> {
        return try {
            val auth = getAuthHeader() ?: return Resource.Error("Authentication failed")
            val response = api.assignBuildingAdmin(
                authorization = auth,
                buildingId = buildingId,
                request = AssignBuildingAdminRequest(userId = userId)
            )
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val err = response.errorBody()?.string() ?: response.message()
                Resource.Error(err ?: "Failed to assign building admin")
            }
        } catch (e: Exception) {
            Log.e("CampusAdminRepo", "assignBuildingAdmin: ${e.message}", e)
            Resource.Error(e.message ?: "Failed to assign building admin")
        }
    }

    // ── Profile ──

    override suspend fun getProfile(): Resource<ProfileResponse> {
        return try {
            val auth = getAuthHeader() ?: return Resource.Error("Authentication failed")
            val response = api.getProfile(auth)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val err = response.errorBody()?.string() ?: response.message()
                Resource.Error(err ?: "Failed to fetch profile")
            }
        } catch (e: Exception) {
            Log.e("CampusAdminRepo", "getProfile: ${e.message}", e)
            Resource.Error(e.message ?: "Failed to fetch profile")
        }
    }

    override suspend fun updateProfile(name: String, phoneNumber: String?): Resource<ProfileResponse> {
        return try {
            val auth = getAuthHeader() ?: return Resource.Error("Authentication failed")
            val response = api.updateProfile(auth, UpdateProfileRequest(name, phoneNumber))
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val err = response.errorBody()?.string() ?: response.message()
                Resource.Error(err ?: "Failed to update profile")
            }
        } catch (e: Exception) {
            Log.e("CampusAdminRepo", "updateProfile: ${e.message}", e)
            Resource.Error(e.message ?: "Failed to update profile")
        }
    }
}
