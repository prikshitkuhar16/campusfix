package com.campusfix.app.data.repository

import android.util.Log
import com.campusfix.app.core.firebase.FirebaseAuthManager
import com.campusfix.app.core.util.Resource
import com.campusfix.app.data.remote.RetrofitClient
import com.campusfix.app.data.remote.dto.*
import com.campusfix.app.domain.repository.StudentRepository

class StudentRepositoryImpl(
    private val firebaseAuthManager: FirebaseAuthManager
) : StudentRepository {

    private val api = RetrofitClient.studentApiService

    private suspend fun getAuthHeader(): String? {
        val token = firebaseAuthManager.getIdToken(true)
        return if (token != null) "Bearer $token" else null
    }

    // ── Complaints ──

    override suspend fun createComplaint(request: CreateComplaintRequest): Resource<CreateComplaintResponse> {
        return try {
            val auth = getAuthHeader() ?: return Resource.Error("Authentication failed")
            val response = api.createComplaint(auth, request)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val err = response.errorBody()?.string() ?: response.message()
                Resource.Error(err ?: "Failed to create complaint")
            }
        } catch (e: Exception) {
            Log.e("StudentRepo", "createComplaint: ${e.message}", e)
            Resource.Error(e.message ?: "Failed to create complaint")
        }
    }

    override suspend fun getMyComplaints(): Resource<List<ComplaintDto>> {
        return try {
            val auth = getAuthHeader() ?: return Resource.Error("Authentication failed")
            val response = api.getMyComplaints(auth)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.complaints)
            } else {
                val err = response.errorBody()?.string() ?: response.message()
                Resource.Error(err ?: "Failed to fetch complaints")
            }
        } catch (e: Exception) {
            Log.e("StudentRepo", "getMyComplaints: ${e.message}", e)
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
            Log.e("StudentRepo", "getComplaintById: ${e.message}", e)
            Resource.Error(e.message ?: "Failed to fetch complaint details")
        }
    }

    override suspend fun verifyResolution(complaintId: String): Resource<ComplaintDetailResponse> {
        return try {
            val auth = getAuthHeader() ?: return Resource.Error("Authentication failed")
            val response = api.verifyResolution(auth, complaintId)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val err = response.errorBody()?.string() ?: response.message()
                Resource.Error(err ?: "Failed to verify resolution")
            }
        } catch (e: Exception) {
            Log.e("StudentRepo", "verifyResolution: ${e.message}", e)
            Resource.Error(e.message ?: "Failed to verify resolution")
        }
    }

    override suspend fun updateComplaintStatus(complaintId: String, status: String): Resource<ComplaintDetailResponse> {
        return try {
            val auth = getAuthHeader() ?: return Resource.Error("Authentication failed")
            val response = api.updateComplaintStatus(auth, complaintId, UpdateStatusRequest(status))
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val err = response.errorBody()?.string() ?: response.message()
                Resource.Error(err ?: "Failed to update status")
            }
        } catch (e: Exception) {
            Log.e("StudentRepo", "updateComplaintStatus: ${e.message}", e)
            Resource.Error(e.message ?: "Failed to update status")
        }
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
            Log.e("StudentRepo", "getBuildings: ${e.message}", e)
            Resource.Error(e.message ?: "Failed to fetch buildings")
        }
    }

    // ── Profile ──

    override suspend fun getProfile(): Resource<StudentProfileResponse> {
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
            Log.e("StudentRepo", "getProfile: ${e.message}", e)
            Resource.Error(e.message ?: "Failed to fetch profile")
        }
    }

    override suspend fun updateProfile(name: String, phoneNumber: String?, buildingId: String?): Resource<StudentProfileResponse> {
        return try {
            val auth = getAuthHeader() ?: return Resource.Error("Authentication failed")
            val response = api.updateProfile(auth, UpdateProfileRequest(name, phoneNumber, buildingId))
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val err = response.errorBody()?.string() ?: response.message()
                Resource.Error(err ?: "Failed to update profile")
            }
        } catch (e: Exception) {
            Log.e("StudentRepo", "updateProfile: ${e.message}", e)
            Resource.Error(e.message ?: "Failed to update profile")
        }
    }
}
