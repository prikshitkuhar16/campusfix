package com.campusfix.app.features.dashboard.staff.complaints

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusfix.app.core.util.Resource
import com.campusfix.app.data.remote.dto.ComplaintDetailResponse
import com.campusfix.app.data.remote.dto.ComplaintDto
import com.campusfix.app.domain.repository.StaffRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StaffComplaintsViewModel(
    private val repository: StaffRepository
) : ViewModel() {

    // ── Filter ──
    private val _selectedFilter = MutableStateFlow(StaffComplaintFilter.ALL)
    val selectedFilter: StateFlow<StaffComplaintFilter> = _selectedFilter.asStateFlow()

    // ── Complaints list ──
    private val _complaintsState = MutableStateFlow<StaffComplaintsUiState>(StaffComplaintsUiState.Loading)
    val complaintsState: StateFlow<StaffComplaintsUiState> = _complaintsState.asStateFlow()

    private var allComplaints: List<ComplaintDto> = emptyList()

    // ── Complaint detail ──
    private val _complaintDetailState = MutableStateFlow<StaffComplaintDetailUiState>(StaffComplaintDetailUiState.Loading)
    val complaintDetailState: StateFlow<StaffComplaintDetailUiState> = _complaintDetailState.asStateFlow()

    // ── Status change ──
    private val _statusChangeState = MutableStateFlow<StaffComplaintActionState>(StaffComplaintActionState.Idle)
    val statusChangeState: StateFlow<StaffComplaintActionState> = _statusChangeState.asStateFlow()

    // ── History ──
    private val _historyState = MutableStateFlow<StaffComplaintsUiState>(StaffComplaintsUiState.Loading)
    val historyState: StateFlow<StaffComplaintsUiState> = _historyState.asStateFlow()

    // ── Snackbar ──
    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    init {
        loadComplaints()
    }

    fun loadComplaints() {
        viewModelScope.launch {
            _complaintsState.value = StaffComplaintsUiState.Loading
            when (val result = repository.getComplaints()) {
                is Resource.Success -> {
                    allComplaints = result.data
                    applyFilter()
                }
                is Resource.Error -> {
                    _complaintsState.value = StaffComplaintsUiState.Error(result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun onFilterSelected(filter: StaffComplaintFilter) {
        _selectedFilter.value = filter
        applyFilter()
    }

    private fun applyFilter() {
        val filtered = when (_selectedFilter.value) {
            StaffComplaintFilter.ALL -> allComplaints.filter {
                it.status == "ASSIGNED" || it.status == "IN_PROGRESS"
            }
            StaffComplaintFilter.ASSIGNED -> allComplaints.filter { it.status == "ASSIGNED" }
            StaffComplaintFilter.IN_PROGRESS -> allComplaints.filter { it.status == "IN_PROGRESS" }
        }
        if (filtered.isEmpty()) {
            _complaintsState.value = StaffComplaintsUiState.Empty
        } else {
            _complaintsState.value = StaffComplaintsUiState.Success(filtered)
        }
    }

    fun loadComplaintDetail(complaintId: String) {
        viewModelScope.launch {
            _complaintDetailState.value = StaffComplaintDetailUiState.Loading
            when (val result = repository.getComplaintById(complaintId)) {
                is Resource.Success -> {
                    _complaintDetailState.value = StaffComplaintDetailUiState.Success(result.data)
                }
                is Resource.Error -> {
                    _complaintDetailState.value = StaffComplaintDetailUiState.Error(result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun updateStatus(complaintId: String, newStatus: String) {
        viewModelScope.launch {
            _statusChangeState.value = StaffComplaintActionState.Loading
            when (val result = repository.updateComplaintStatus(complaintId, newStatus)) {
                is Resource.Success -> {
                    _statusChangeState.value = StaffComplaintActionState.Success("Status updated")
                    _complaintDetailState.value = StaffComplaintDetailUiState.Success(result.data)
                    _snackbarEvent.emit("Status updated to ${newStatus.replace("_", " ")}")
                    loadComplaints()
                }
                is Resource.Error -> {
                    _statusChangeState.value = StaffComplaintActionState.Error(result.message)
                    _snackbarEvent.emit(result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun loadHistory() {
        viewModelScope.launch {
            _historyState.value = StaffComplaintsUiState.Loading
            when (val result = repository.getComplaintsByStatus("RESOLVED")) {
                is Resource.Success -> {
                    if (result.data.isEmpty()) {
                        _historyState.value = StaffComplaintsUiState.Empty
                    } else {
                        _historyState.value = StaffComplaintsUiState.Success(result.data)
                    }
                }
                is Resource.Error -> {
                    _historyState.value = StaffComplaintsUiState.Error(result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun clearStatusChangeState() {
        _statusChangeState.value = StaffComplaintActionState.Idle
    }
}

// ── Filter enum ──

enum class StaffComplaintFilter(val label: String) {
    ALL("All"),
    ASSIGNED("Assigned"),
    IN_PROGRESS("In Progress")
}

// ── UI States ──

sealed class StaffComplaintsUiState {
    data object Loading : StaffComplaintsUiState()
    data object Empty : StaffComplaintsUiState()
    data class Success(val complaints: List<ComplaintDto>) : StaffComplaintsUiState()
    data class Error(val message: String) : StaffComplaintsUiState()
}

sealed class StaffComplaintDetailUiState {
    data object Loading : StaffComplaintDetailUiState()
    data class Success(val complaint: ComplaintDetailResponse) : StaffComplaintDetailUiState()
    data class Error(val message: String) : StaffComplaintDetailUiState()
}

sealed class StaffComplaintActionState {
    data object Idle : StaffComplaintActionState()
    data object Loading : StaffComplaintActionState()
    data class Success(val message: String) : StaffComplaintActionState()
    data class Error(val message: String) : StaffComplaintActionState()
}

