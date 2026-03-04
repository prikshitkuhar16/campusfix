package com.campusfix.app.data.remote.dto

// ── Campus creation ──

data class CreateCampusRequest(
    val name: String,
    val campusName: String,
    val campusAddress: String,
    val description: String
)

data class CreateCampusResponse(
    val campusId: String,
    val message: String
)

// ── Domain check ──

data class CheckDomainRequest(
    val officialEmail: String
)

data class CheckDomainResponse(
    val exists: Boolean,
    val message: String?
)

