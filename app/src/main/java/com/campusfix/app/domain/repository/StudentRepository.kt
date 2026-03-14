package com.campusfix.app.domain.repository

import com.campusfix.app.core.util.Resource
import com.campusfix.app.data.remote.dto.*

interface StudentRepository {

    // ── Complaints ──
    suspend fun createComplaint(request: CreateComplaintRequest): Resource<CreateComplaintResponse>
    suspend fun getMyComplaints(): Resource<List<ComplaintDto>>
    suspend fun getComplaintById(complaintId: String): Resource<ComplaintDetailResponse>
    suspend fun verifyResolution(complaintId: String): Resource<ComplaintDetailResponse>
    suspend fun updateComplaintStatus(complaintId: String, status: String): Resource<ComplaintDetailResponse>

    // ── Buildings ──
    suspend fun getBuildings(): Resource<List<BuildingDto>>

    // ── Profile ──
    suspend fun getProfile(): Resource<StudentProfileResponse>
    suspend fun updateProfile(name: String, phoneNumber: String?, buildingId: String? = null): Resource<StudentProfileResponse>
}
