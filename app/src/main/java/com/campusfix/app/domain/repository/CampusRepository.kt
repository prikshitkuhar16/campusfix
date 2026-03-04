package com.campusfix.app.domain.repository

import com.campusfix.app.core.util.Resource
import com.campusfix.app.data.remote.dto.BuildingDto

interface CampusRepository {
    suspend fun createBuilding(idToken: String, buildingNumber: String, buildingName: String): Resource<String>
    suspend fun getBuildings(idToken: String): Resource<List<BuildingDto>>
    suspend fun inviteBuildingAdmin(idToken: String, email: String, buildingId: String): Resource<String>
}

