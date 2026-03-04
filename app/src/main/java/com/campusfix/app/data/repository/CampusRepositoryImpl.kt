package com.campusfix.app.data.repository

import android.util.Log
import com.campusfix.app.core.util.Resource
import com.campusfix.app.data.remote.RetrofitClient
import com.campusfix.app.data.remote.dto.BuildingDto
import com.campusfix.app.data.remote.dto.CreateBuildingRequest
import com.campusfix.app.data.remote.dto.InviteBuildingAdminRequest
import com.campusfix.app.domain.repository.CampusRepository

class CampusRepositoryImpl : CampusRepository {

    private val api = RetrofitClient.authApiService

    override suspend fun createBuilding(
        idToken: String,
        buildingNumber: String,
        buildingName: String
    ): Resource<String> {
        return try {
            Log.d("CampusRepo", "createBuilding - $buildingNumber / $buildingName")
            val response = api.createBuilding(
                authorization = "Bearer $idToken",
                request = CreateBuildingRequest(number = buildingNumber, name = buildingName)
            )
            if (response.isSuccessful && response.body() != null) {
                Log.d("CampusRepo", "createBuilding - success: ${response.body()!!.buildingId}")
                Resource.Success(response.body()!!.message)
            } else {
                val err = response.errorBody()?.string() ?: response.message()
                Log.e("CampusRepo", "createBuilding - failed: $err")
                Resource.Error(err ?: "Failed to create building")
            }
        } catch (e: Exception) {
            Log.e("CampusRepo", "createBuilding - exception: ${e.message}", e)
            Resource.Error(e.message ?: "Failed to create building")
        }
    }

    override suspend fun getBuildings(idToken: String): Resource<List<BuildingDto>> {
        return try {
            Log.d("CampusRepo", "getBuildings")
            val response = api.getBuildings("Bearer $idToken")
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.buildings)
            } else {
                val err = response.errorBody()?.string() ?: response.message()
                Resource.Error(err ?: "Failed to fetch buildings")
            }
        } catch (e: Exception) {
            Log.e("CampusRepo", "getBuildings - exception: ${e.message}", e)
            Resource.Error(e.message ?: "Failed to fetch buildings")
        }
    }

    override suspend fun inviteBuildingAdmin(
        idToken: String,
        email: String,
        buildingId: String
    ): Resource<String> {
        return try {
            Log.d("CampusRepo", "inviteBuildingAdmin - email=$email, buildingId=$buildingId")
            val response = api.inviteBuildingAdmin(
                authorization = "Bearer $idToken",
                request = InviteBuildingAdminRequest(email, buildingId)
            )
            if (response.isSuccessful && response.body() != null) {
                Log.d("CampusRepo", "inviteBuildingAdmin - success")
                Resource.Success(response.body()!!.message)
            } else {
                val err = response.errorBody()?.string() ?: response.message()
                Log.e("CampusRepo", "inviteBuildingAdmin - failed: $err")
                Resource.Error(err ?: "Failed to send invite")
            }
        } catch (e: Exception) {
            Log.e("CampusRepo", "inviteBuildingAdmin - exception: ${e.message}", e)
            Resource.Error(e.message ?: "Failed to send invite")
        }
    }
}

