package com.campusfix.app.domain.repository

import com.campusfix.app.core.util.Resource
import com.campusfix.app.data.remote.dto.*

interface BuildingAdminRepository {

    // ── Complaints ──
    suspend fun getComplaints(): Resource<List<ComplaintDto>>
    suspend fun getComplaintById(complaintId: String): Resource<ComplaintDetailResponse>
    suspend fun assignStaffToComplaint(complaintId: String, staffId: String): Resource<ComplaintDetailResponse>
    suspend fun updateComplaintStatus(complaintId: String, status: String): Resource<ComplaintDetailResponse>

    // ── Staff ──
    suspend fun getStaff(): Resource<List<StaffDto>>
    suspend fun getStaffByJobType(jobType: String): Resource<List<StaffDto>>
    suspend fun inviteStaff(email: String, jobType: String): Resource<String>
    suspend fun deactivateStaff(staffId: String): Resource<StaffDto>
    suspend fun activateStaff(staffId: String): Resource<StaffDto>
    suspend fun updateStaffJobType(staffId: String, jobType: String): Resource<StaffDto>

    suspend fun getInvites(): Resource<List<InviteDto>>
    suspend fun revokeInvite(inviteId: String): Resource<String>

    suspend fun getProfile(): Resource<BuildingAdminProfileResponse>
    suspend fun updateProfile(name: String, phoneNumber: String?): Resource<BuildingAdminProfileResponse>
}
