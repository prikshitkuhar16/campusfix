package com.campusfix.app.features.dashboard.buildingadmin.staff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusfix.app.core.util.Resource
import com.campusfix.app.data.remote.dto.StaffDto
import com.campusfix.app.data.remote.dto.InviteDto
import com.campusfix.app.domain.repository.BuildingAdminRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StaffViewModel(
    private val repository: BuildingAdminRepository
) : ViewModel() {

    // ── Staff list ──
    private val _staffState = MutableStateFlow<StaffUiState>(StaffUiState.Loading)
    val staffState: StateFlow<StaffUiState> = _staffState.asStateFlow()

    // ── Selected staff detail ──
    private val _selectedStaff = MutableStateFlow<StaffDto?>(null)
    val selectedStaff: StateFlow<StaffDto?> = _selectedStaff.asStateFlow()

    // ── Deactivate ──
    private val _deactivateState = MutableStateFlow<StaffActionState>(StaffActionState.Idle)
    val deactivateState: StateFlow<StaffActionState> = _deactivateState.asStateFlow()

    // ── Activate ──
    private val _activateState = MutableStateFlow<StaffActionState>(StaffActionState.Idle)
    val activateState: StateFlow<StaffActionState> = _activateState.asStateFlow()

    // ── Update Job Type ──
    private val _updateJobTypeState = MutableStateFlow<StaffActionState>(StaffActionState.Idle)
    val updateJobTypeState: StateFlow<StaffActionState> = _updateJobTypeState.asStateFlow()

    // ── Invites list ──
    private val _invitesState = MutableStateFlow<StaffInvitesUiState>(StaffInvitesUiState.Idle)
    val invitesState: StateFlow<StaffInvitesUiState> = _invitesState.asStateFlow()

    private val _revokeState = MutableStateFlow<StaffActionState>(StaffActionState.Idle)
    val revokeState: StateFlow<StaffActionState> = _revokeState.asStateFlow()

    // ── Invite form ──
    private val _inviteEmail = MutableStateFlow("")
    val inviteEmail: StateFlow<String> = _inviteEmail.asStateFlow()

    private val _inviteJobType = MutableStateFlow("PLUMBER")
    val inviteJobType: StateFlow<String> = _inviteJobType.asStateFlow()

    private val _inviteState = MutableStateFlow<StaffActionState>(StaffActionState.Idle)
    val inviteState: StateFlow<StaffActionState> = _inviteState.asStateFlow()

    // ── Snackbar ──
    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    init {
        loadStaff()
        loadInvites()
    }

    fun loadStaff() {
        viewModelScope.launch {
            _staffState.value = StaffUiState.Loading
            when (val result = repository.getStaff()) {
                is Resource.Success -> {
                    if (result.data.isEmpty()) {
                        _staffState.value = StaffUiState.Empty
                    } else {
                        _staffState.value = StaffUiState.Success(result.data)
                    }
                }
                is Resource.Error -> {
                    _staffState.value = StaffUiState.Error(result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun setSelectedStaff(staff: StaffDto) {
        _selectedStaff.value = staff
    }

    fun deactivateStaff(staffId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _deactivateState.value = StaffActionState.Loading
            when (val result = repository.deactivateStaff(staffId)) {
                is Resource.Success -> {
                    _deactivateState.value = StaffActionState.Success(result.data.jobType ?: "Staff deactivated successfully")
                    _snackbarEvent.emit("Staff deactivated successfully")
                    loadStaff()
                    // Update selected staff if currently viewing details
                    _selectedStaff.value = result.data
                    onSuccess()
                }
                is Resource.Error -> {
                    _deactivateState.value = StaffActionState.Error(result.message)
                    _snackbarEvent.emit(result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun activateStaff(staffId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _activateState.value = StaffActionState.Loading
            when (val result = repository.activateStaff(staffId)) {
                is Resource.Success -> {
                    _activateState.value = StaffActionState.Success("Staff activated successfully")
                    _snackbarEvent.emit("Staff activated successfully")
                    loadStaff()
                    _selectedStaff.value = result.data
                    onSuccess()
                }
                is Resource.Error -> {
                    _activateState.value = StaffActionState.Error(result.message)
                    _snackbarEvent.emit(result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun updateStaffJobType(staffId: String, jobType: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _updateJobTypeState.value = StaffActionState.Loading
            when (val result = repository.updateStaffJobType(staffId, jobType)) {
                is Resource.Success -> {
                    _updateJobTypeState.value = StaffActionState.Success(result.data.jobType ?: "Job Type Updated")
                    _snackbarEvent.emit("Job type updated successfully")
                    loadStaff()
                    _selectedStaff.value = _selectedStaff.value?.copy(jobType = jobType)
                    onSuccess()
                }
                is Resource.Error -> {
                    _updateJobTypeState.value = StaffActionState.Error(result.message)
                    _snackbarEvent.emit(result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }


    fun onInviteEmailChange(value: String) {
        _inviteEmail.value = value.replace("\n", "").replace("\r", "").trim()
    }

    fun onInviteJobTypeChange(jobType: String) {
        _inviteJobType.value = jobType
    }

    fun onSendInvite(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val email = _inviteEmail.value.trim()
            if (email.isBlank() || !email.contains("@")) {
                _inviteState.value = StaffActionState.Error("Please enter a valid email")
                return@launch
            }

            _inviteState.value = StaffActionState.Loading
            when (val result = repository.inviteStaff(
                email = email,
                jobType = _inviteJobType.value
            )) {
                is Resource.Success -> {
                    _inviteState.value = StaffActionState.Success(result.data)
                    _snackbarEvent.emit("Invite sent successfully")
                    _inviteEmail.value = ""
                    _inviteJobType.value = "PLUMBER"
                    onSuccess()
                    loadInvites() 
                }
                is Resource.Error -> {
                    _inviteState.value = StaffActionState.Error(result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun clearInviteState() {
        _inviteState.value = StaffActionState.Idle
    }

    fun clearDeactivateState() {
        _deactivateState.value = StaffActionState.Idle
    }

    fun clearActivateState() {
        _activateState.value = StaffActionState.Idle
    }

    fun clearUpdateJobTypeState() {
        _updateJobTypeState.value = StaffActionState.Idle
    }

    // ── Invites handlers ──

    fun loadInvites() {
        viewModelScope.launch {
            _invitesState.value = StaffInvitesUiState.Loading
            when (val result = repository.getInvites()) {
                is Resource.Success -> {
                    if (result.data.isEmpty()) {
                        _invitesState.value = StaffInvitesUiState.Empty
                    } else {
                        _invitesState.value = StaffInvitesUiState.Success(result.data)
                    }
                }
                is Resource.Error -> {
                    _invitesState.value = StaffInvitesUiState.Error(result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun revokeInvite(inviteId: String) {
        viewModelScope.launch {
            _revokeState.value = StaffActionState.Loading
            when (val result = repository.revokeInvite(inviteId)) {
                is Resource.Success -> {
                    _revokeState.value = StaffActionState.Success(result.data)
                    _snackbarEvent.emit("Invite revoked successfully")
                    loadInvites()
                }
                is Resource.Error -> {
                    _revokeState.value = StaffActionState.Error(result.message)
                    _snackbarEvent.emit(result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }
}

// ── Job types ──

val JOB_TYPES = listOf(
    "PLUMBER", "ELECTRICIAN", "CARPENTER", "PAINTER",
    "CLEANER", "GARDENER", "HVAC_TECHNICIAN", "GENERAL_MAINTENANCE"
)

// ── UI States ──

sealed class StaffUiState {
    data object Loading : StaffUiState()
    data object Empty : StaffUiState()
    data class Success(val staff: List<StaffDto>) : StaffUiState()
    data class Error(val message: String) : StaffUiState()
}

sealed class StaffInvitesUiState {
    data object Idle : StaffInvitesUiState()
    data object Loading : StaffInvitesUiState()
    data object Empty : StaffInvitesUiState()
    data class Success(val invites: List<InviteDto>) : StaffInvitesUiState()
    data class Error(val message: String) : StaffInvitesUiState()
}

sealed class StaffActionState {
    data object Idle : StaffActionState()
    data object Loading : StaffActionState()
    data class Success(val message: String) : StaffActionState()
    data class Error(val message: String) : StaffActionState()
}
