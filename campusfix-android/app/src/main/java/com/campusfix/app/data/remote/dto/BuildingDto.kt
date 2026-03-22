package com.campusfix.app.data.remote.dto

data class CreateBuildingRequest(
    val number: String,
    val name: String
)

data class CreateBuildingResponse(
    val buildingId: String,
    val message: String
)


data class BuildingDto(
    val id: String,
    val number: String,
    val name: String,
    val campusId: String
)

data class BuildingListResponse(
    val buildings: List<BuildingDto>
)

