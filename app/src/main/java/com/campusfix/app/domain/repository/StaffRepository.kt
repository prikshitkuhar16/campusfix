package com.campusfix.app.domain.repository

import com.campusfix.app.core.util.Resource
import com.campusfix.app.data.remote.dto.*

interface StaffRepository {

    // ── Complaints ──
    suspend fun getComplaints(): Resource<List<ComplaintDto>>
    suspend fun getComplaintsByStatus(status: String): Resource<List<ComplaintDto>>
    suspend fun getComplaintById(complaintId: String): Resource<ComplaintDetailResponse>
    suspend fun updateComplaintStatus(complaintId: String, status: String): Resource<ComplaintDetailResponse>

    // ── Profile ──
    suspend fun getProfile(): Resource<StaffProfileResponse>
    suspend fun updateProfile(name: String, phoneNumber: String?): Resource<StaffProfileResponse>
}

