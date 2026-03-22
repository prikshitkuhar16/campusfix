package com.campusfix.app.features.dashboard.buildingadmin.complaints

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusfix.app.core.util.Resource
import com.campusfix.app.data.remote.dto.ComplaintDetailResponse
import com.campusfix.app.data.remote.dto.ComplaintDto
import com.campusfix.app.data.remote.dto.StaffDto
import com.campusfix.app.domain.repository.BuildingAdminRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ComplaintsViewModel(
    private val repository: BuildingAdminRepository
) : ViewModel() {

    // ── Filter ──
    private val _selectedFilter = MutableStateFlow(ComplaintFilter.ALL)
    val selectedFilter: StateFlow<ComplaintFilter> = _selectedFilter.asStateFlow()

    // ── Complaints list ──
    private val _complaintsState = MutableStateFlow<ComplaintsUiState>(ComplaintsUiState.Loading)
    val complaintsState: StateFlow<ComplaintsUiState> = _complaintsState.asStateFlow()

    private var allComplaints: List<ComplaintDto> = emptyList()

    // ── Complaint detail ──
    private val _complaintDetailState = MutableStateFlow<ComplaintDetailUiState>(ComplaintDetailUiState.Loading)
    val complaintDetailState: StateFlow<ComplaintDetailUiState> = _complaintDetailState.asStateFlow()

    // ── Assign staff ──
    private val _staffListState = MutableStateFlow<StaffListUiState>(StaffListUiState.Loading)
    val staffListState: StateFlow<StaffListUiState> = _staffListState.asStateFlow()

    private val _selectedStaffId = MutableStateFlow<String?>(null)
    val selectedStaffId: StateFlow<String?> = _selectedStaffId.asStateFlow()

    private val _assignState = MutableStateFlow<ComplaintActionState>(ComplaintActionState.Idle)
    val assignState: StateFlow<ComplaintActionState> = _assignState.asStateFlow()

    // ── Status change ──
    private val _statusChangeState = MutableStateFlow<ComplaintActionState>(ComplaintActionState.Idle)
    val statusChangeState: StateFlow<ComplaintActionState> = _statusChangeState.asStateFlow()

    // ── Snackbar ──
    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    init {
        loadComplaints()
    }

    fun onFilterSelected(filter: ComplaintFilter) {
        _selectedFilter.value = filter
        applyFilter()
    }

    fun loadComplaints() {
        viewModelScope.launch {
            _complaintsState.value = ComplaintsUiState.Loading
            when (val result = repository.getComplaints()) {
                is Resource.Success -> {
                    allComplaints = result.data
                    applyFilter()
                }
                is Resource.Error -> {
                    _complaintsState.value = ComplaintsUiState.Error(result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    private fun applyFilter() {
        val filtered = when (_selectedFilter.value) {
            ComplaintFilter.ALL -> allComplaints
            ComplaintFilter.CREATED -> allComplaints.filter { it.status == "CREATED" }
            ComplaintFilter.ASSIGNED -> allComplaints.filter { it.status == "ASSIGNED" }
            ComplaintFilter.RESOLVED -> allComplaints.filter { it.status == "RESOLVED" }
            ComplaintFilter.VERIFIED -> allComplaints.filter { it.status == "VERIFIED" }
        }
        if (filtered.isEmpty()) {
            _complaintsState.value = ComplaintsUiState.Empty
        } else {
            _complaintsState.value = ComplaintsUiState.Success(filtered)
        }
    }

    fun loadComplaintDetail(complaintId: String) {
        viewModelScope.launch {
            _complaintDetailState.value = ComplaintDetailUiState.Loading
            when (val result = repository.getComplaintById(complaintId)) {
                is Resource.Success -> {
                    _complaintDetailState.value = ComplaintDetailUiState.Success(result.data)
                }
                is Resource.Error -> {
                    _complaintDetailState.value = ComplaintDetailUiState.Error(result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun loadStaffForAssignment(jobType: String? = null) {
        viewModelScope.launch {
            _staffListState.value = StaffListUiState.Loading
            val result = if (!jobType.isNullOrBlank()) {
                repository.getStaffByJobType(jobType)
            } else {
                repository.getStaff()
            }
            when (result) {
                is Resource.Success -> {
                    val activeStaff = result.data.filter { it.isActive }
                    if (activeStaff.isEmpty()) {
                        _staffListState.value = StaffListUiState.Empty
                    } else {
                        _staffListState.value = StaffListUiState.Success(activeStaff)
                    }
                }
                is Resource.Error -> {
                    _staffListState.value = StaffListUiState.Error(result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun onStaffSelected(staffId: String) {
        _selectedStaffId.value = staffId
    }

    fun assignStaff(complaintId: String, onSuccess: () -> Unit) {
        val staffId = _selectedStaffId.value ?: return
        viewModelScope.launch {
            _assignState.value = ComplaintActionState.Loading
            when (val result = repository.assignStaffToComplaint(complaintId, staffId)) {
                is Resource.Success -> {
                    _assignState.value = ComplaintActionState.Success("Staff assigned successfully")
                    _complaintDetailState.value = ComplaintDetailUiState.Success(result.data)
                    _snackbarEvent.emit("Staff assigned successfully")
                    _selectedStaffId.value = null
                    loadComplaints()
                    onSuccess()
                }
                is Resource.Error -> {
                    _assignState.value = ComplaintActionState.Error(result.message)
                    _snackbarEvent.emit(result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun updateStatus(complaintId: String, newStatus: String) {
        viewModelScope.launch {
            _statusChangeState.value = ComplaintActionState.Loading
            when (val result = repository.updateComplaintStatus(complaintId, newStatus)) {
                is Resource.Success -> {
                    _statusChangeState.value = ComplaintActionState.Success("Status updated")
                    _complaintDetailState.value = ComplaintDetailUiState.Success(result.data)
                    _snackbarEvent.emit("Status updated to ${newStatus.replace("_", " ")}")
                    loadComplaints()
                }
                is Resource.Error -> {
                    _statusChangeState.value = ComplaintActionState.Error(result.message)
                    _snackbarEvent.emit(result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun clearAssignState() {
        _assignState.value = ComplaintActionState.Idle
    }

    fun clearStatusChangeState() {
        _statusChangeState.value = ComplaintActionState.Idle
    }
}

// ── Filter enum ──

enum class ComplaintFilter(val label: String) {
    ALL("All"),
    CREATED("Created"),
    ASSIGNED("Assigned"),
    RESOLVED("Resolved"),
    VERIFIED("Verified")
}

// ── UI States ──

sealed class ComplaintsUiState {
    data object Loading : ComplaintsUiState()
    data object Empty : ComplaintsUiState()
    data class Success(val complaints: List<ComplaintDto>) : ComplaintsUiState()
    data class Error(val message: String) : ComplaintsUiState()
}

sealed class ComplaintDetailUiState {
    data object Loading : ComplaintDetailUiState()
    data class Success(val complaint: ComplaintDetailResponse) : ComplaintDetailUiState()
    data class Error(val message: String) : ComplaintDetailUiState()
}

sealed class StaffListUiState {
    data object Loading : StaffListUiState()
    data object Empty : StaffListUiState()
    data class Success(val staff: List<StaffDto>) : StaffListUiState()
    data class Error(val message: String) : StaffListUiState()
}

sealed class ComplaintActionState {
    data object Idle : ComplaintActionState()
    data object Loading : ComplaintActionState()
    data class Success(val message: String) : ComplaintActionState()
    data class Error(val message: String) : ComplaintActionState()
}
