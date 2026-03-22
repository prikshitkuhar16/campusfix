package com.campusfix.app.data.repository

import android.util.Log
import com.campusfix.app.core.firebase.FirebaseAuthManager
import com.campusfix.app.core.util.Resource
import com.campusfix.app.data.remote.RetrofitClient
import com.campusfix.app.data.remote.dto.*
import com.campusfix.app.domain.repository.BuildingAdminRepository

class BuildingAdminRepositoryImpl(
    private val firebaseAuthManager: FirebaseAuthManager
) : BuildingAdminRepository {

    private val api = RetrofitClient.buildingAdminApiService

    private suspend fun getAuthHeader(): String? {
        val token = firebaseAuthManager.getIdToken(true)
        return if (token != null) "Bearer $token" else null
    }

    // ── Complaints ──

    override suspend fun getComplaints(): Resource<List<ComplaintDto>> {
        return try {
            val auth = getAuthHeader() ?: return Resource.Error("Authentication failed")
            val response = api.getComplaints(auth)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.complaints)
            } else {
                val err = response.errorBody()?.string() ?: response.message()
                Resource.Error(err ?: "Failed to fetch complaints")
            }
        } catch (e: Exception) {
            Log.e("BuildingAdminRepo", "getComplaints: ${e.message}", e)
            Resource.Error(e.message ?: "Failed to fetch complaints")
        }
    }

    override suspend fun getComplaintById(complaintId: String): Resource<ComplaintDetailResponse> {
        return try {
            val auth = getAuthHeader() ?: return Resource.Error("Authentication failed")
            val response = api.getComplaintById(auth, complaintId)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val err = response.errorBody()?.string() ?: response.message()
                Resource.Error(err ?: "Failed to fetch complaint details")
            }
        } catch (e: Exception) {
            Log.e("BuildingAdminRepo", "getComplaintById: ${e.message}", e)
            Resource.Error(e.message ?: "Failed to fetch complaint details")
        }
    }

    override suspend fun assignStaffToComplaint(
        complaintId: String,
        staffId: String
    ): Resource<ComplaintDetailResponse> {
        return try {
            val auth = getAuthHeader() ?: return Resource.Error("Authentication failed")
            val response = api.assignStaffToComplaint(
                authorization = auth,
                complaintId = complaintId,
                request = AssignStaffRequest(staffId = staffId)
            )
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val err = response.errorBody()?.string() ?: response.message()
                Resource.Error(err ?: "Failed to assign staff")
            }
        } catch (e: Exception) {
            Log.e("BuildingAdminRepo", "assignStaffToComplaint: ${e.message}", e)
            Resource.Error(e.message ?: "Failed to assign staff")
        }
    }

    override suspend fun updateComplaintStatus(
        complaintId: String,
        status: String
    ): Resource<ComplaintDetailResponse> {
        return try {
            val auth = getAuthHeader() ?: return Resource.Error("Authentication failed")
            val response = api.updateComplaintStatus(
                authorization = auth,
                complaintId = complaintId,
                request = UpdateStatusRequest(status = status)
            )
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val err = response.errorBody()?.string() ?: response.message()
                Resource.Error(err ?: "Failed to update complaint status")
            }
        } catch (e: Exception) {
            Log.e("BuildingAdminRepo", "updateComplaintStatus: ${e.message}", e)
            Resource.Error(e.message ?: "Failed to update complaint status")
        }
    }

    // ── Staff ──

    override suspend fun getStaff(): Resource<List<StaffDto>> {
        return try {
            val auth = getAuthHeader() ?: return Resource.Error("Authentication failed")
            val response = api.getStaff(auth)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.staff)
            } else {
                val err = response.errorBody()?.string() ?: response.message()
                Resource.Error(err ?: "Failed to fetch staff")
            }
        } catch (e: Exception) {
            Log.e("BuildingAdminRepo", "getStaff: ${e.message}", e)
            Resource.Error(e.message ?: "Failed to fetch staff")
        }
    }

    override suspend fun getStaffByJobType(jobType: String): Resource<List<StaffDto>> {
        return try {
            val auth = getAuthHeader() ?: return Resource.Error("Authentication failed")
            val response = api.getStaffByJobType(auth, jobType)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.staff)
            } else {
                val err = response.errorBody()?.string() ?: response.message()
                Resource.Error(err ?: "Failed to fetch staff")
            }
        } catch (e: Exception) {
            Log.e("BuildingAdminRepo", "getStaffByJobType: ${e.message}", e)
            Resource.Error(e.message ?: "Failed to fetch staff")
        }
    }

    override suspend fun inviteStaff(email: String, jobType: String): Resource<String> {
        return try {
            val auth = getAuthHeader() ?: return Resource.Error("Authentication failed")
            val response = api.inviteStaff(
                authorization = auth,
                request = InviteStaffRequest(
                    email = email,
                    jobType = jobType
                )
            )
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.message)
            } else {
                val err = response.errorBody()?.string() ?: response.message()
                Resource.Error(err ?: "Failed to invite staff")
            }
        } catch (e: Exception) {
            Log.e("BuildingAdminRepo", "inviteStaff: ${e.message}", e)
            Resource.Error(e.message ?: "Failed to invite staff")
        }
    }

    override suspend fun deactivateStaff(staffId: String): Resource<StaffDto> {
        return try {
            val auth = getAuthHeader() ?: return Resource.Error("Authentication failed")
            val response = api.deactivateStaff(auth, staffId)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val err = response.errorBody()?.string() ?: response.message()
                Resource.Error(err ?: "Failed to deactivate staff")
            }
        } catch (e: Exception) {
            Log.e("BuildingAdminRepo", "deactivateStaff: ${e.message}", e)
            Resource.Error(e.message ?: "Failed to deactivate staff")
        }
    }

    override suspend fun activateStaff(staffId: String): Resource<StaffDto> {
        return try {
            val auth = getAuthHeader() ?: return Resource.Error("Authentication failed")
            val response = api.activateStaff(auth, staffId)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val err = response.errorBody()?.string() ?: response.message()
                Resource.Error(err ?: "Failed to activate staff")
            }
        } catch (e: Exception) {
            Log.e("BuildingAdminRepo", "activateStaff: ${e.message}", e)
            Resource.Error(e.message ?: "Failed to activate staff")
        }
    }

    override suspend fun updateStaffJobType(staffId: String, jobType: String): Resource<StaffDto> {
        return try {
            val auth = getAuthHeader() ?: return Resource.Error("Authentication failed")
            val response = api.updateStaffJobType(
                authorization = auth,
                staffId = staffId,
                request = UpdateJobTypeRequest(jobType = jobType)
            )
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val err = response.errorBody()?.string() ?: response.message()
                Resource.Error(err ?: "Failed to update job type")
            }
        } catch (e: Exception) {
            Log.e("BuildingAdminRepo", "updateStaffJobType: ${e.message}", e)
            Resource.Error(e.message ?: "Failed to update job type")
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
            Log.e("BuildingAdminRepo", "getInvites: ${e.message}", e)
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
            Log.e("BuildingAdminRepo", "revokeInvite: ${e.message}", e)
            Resource.Error(e.message ?: "Failed to revoke invite")
        }
    }

    // ── Profile ──

    override suspend fun getProfile(): Resource<BuildingAdminProfileResponse> {
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
            Log.e("BuildingAdminRepo", "getProfile: ${e.message}", e)
            Resource.Error(e.message ?: "Failed to fetch profile")
        }
    }

    override suspend fun updateProfile(name: String, phoneNumber: String?): Resource<BuildingAdminProfileResponse> {
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
            Log.e("BuildingAdminRepo", "updateProfile: ${e.message}", e)
            Resource.Error(e.message ?: "Failed to update profile")
        }
    }
}
