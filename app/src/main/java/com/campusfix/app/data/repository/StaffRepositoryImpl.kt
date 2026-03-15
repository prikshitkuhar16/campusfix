package com.campusfix.app.data.repository

import android.util.Log
import com.campusfix.app.core.firebase.FirebaseAuthManager
import com.campusfix.app.core.util.Resource
import com.campusfix.app.data.remote.RetrofitClient
import com.campusfix.app.data.remote.dto.*
import com.campusfix.app.domain.repository.StaffRepository

class StaffRepositoryImpl(
    private val firebaseAuthManager: FirebaseAuthManager
) : StaffRepository {

    private val api = RetrofitClient.staffApiService

    private suspend fun getAuthHeader(): String? {
        val token = firebaseAuthManager.getIdToken(true)
        return if (token != null) "Bearer $token" else null
    }


    override suspend fun getComplaints(): Resource<List<ComplaintDto>> {
        return try {
            val auth = getAuthHeader() ?: return Resource.Error("Authentication failed")
            val response = api.getStaffComplaints(auth)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.complaints)
            } else {
                val err = response.errorBody()?.string() ?: response.message()
                Resource.Error(err ?: "Failed to fetch complaints")
            }
        } catch (e: Exception) {
            Log.e("StaffRepo", "getComplaints: ${e.message}", e)
            Resource.Error(e.message ?: "Failed to fetch complaints")
        }
    }

    override suspend fun getComplaintsByStatus(status: String): Resource<List<ComplaintDto>> {
        return try {
            val auth = getAuthHeader() ?: return Resource.Error("Authentication failed")
            val response = api.getStaffComplaintsByStatus(auth, status)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.complaints)
            } else {
                val err = response.errorBody()?.string() ?: response.message()
                Resource.Error(err ?: "Failed to fetch complaints")
            }
        } catch (e: Exception) {
            Log.e("StaffRepo", "getComplaintsByStatus: ${e.message}", e)
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
            Log.e("StaffRepo", "getComplaintById: ${e.message}", e)
            Resource.Error(e.message ?: "Failed to fetch complaint details")
        }
    }

    override suspend fun updateComplaintStatus(
        complaintId: String,
        status: String
    ): Resource<ComplaintDetailResponse> {
        return try {
            val auth = getAuthHeader() ?: return Resource.Error("Authentication failed")
            val response = api.updateComplaintStatus(auth, complaintId, UpdateStatusRequest(status))
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val err = response.errorBody()?.string() ?: response.message()
                Resource.Error(err ?: "Failed to update complaint status")
            }
        } catch (e: Exception) {
            Log.e("StaffRepo", "updateComplaintStatus: ${e.message}", e)
            Resource.Error(e.message ?: "Failed to update complaint status")
        }
    }


    override suspend fun getProfile(): Resource<StaffProfileResponse> {
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
            Log.e("StaffRepo", "getProfile: ${e.message}", e)
            Resource.Error(e.message ?: "Failed to fetch profile")
        }
    }

    override suspend fun updateProfile(name: String, phoneNumber: String?): Resource<StaffProfileResponse> {
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
            Log.e("StaffRepo", "updateProfile: ${e.message}", e)
            Resource.Error(e.message ?: "Failed to update profile")
        }
    }
}

