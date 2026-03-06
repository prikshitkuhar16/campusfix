package com.campusfix.app.features.dashboard.student.complaints

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusfix.app.core.util.Resource
import com.campusfix.app.data.remote.dto.BuildingDto
import com.campusfix.app.data.remote.dto.ComplaintDetailResponse
import com.campusfix.app.data.remote.dto.ComplaintDto
import com.campusfix.app.data.remote.dto.CreateComplaintRequest
import com.campusfix.app.domain.repository.StudentRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StudentComplaintViewModel(
    private val repository: StudentRepository
) : ViewModel() {

    // ── My Complaints list ──
    private val _complaintsState = MutableStateFlow<StudentComplaintsUiState>(StudentComplaintsUiState.Loading)
    val complaintsState: StateFlow<StudentComplaintsUiState> = _complaintsState.asStateFlow()

    // ── Complaint detail ──
    private val _complaintDetailState = MutableStateFlow<StudentComplaintDetailUiState>(StudentComplaintDetailUiState.Loading)
    val complaintDetailState: StateFlow<StudentComplaintDetailUiState> = _complaintDetailState.asStateFlow()

    // ── Raise complaint form ──
    private val _buildingsState = MutableStateFlow<BuildingsUiState>(BuildingsUiState.Loading)
    val buildingsState: StateFlow<BuildingsUiState> = _buildingsState.asStateFlow()

    private val _selectedBuilding = MutableStateFlow<BuildingDto?>(null)
    val selectedBuilding: StateFlow<BuildingDto?> = _selectedBuilding.asStateFlow()

    private val _room = MutableStateFlow("")
    val room: StateFlow<String> = _room.asStateFlow()

    private val _selectedJobType = MutableStateFlow<String?>(null)
    val selectedJobType: StateFlow<String?> = _selectedJobType.asStateFlow()

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _submitState = MutableStateFlow<SubmitComplaintUiState>(SubmitComplaintUiState.Idle)
    val submitState: StateFlow<SubmitComplaintUiState> = _submitState.asStateFlow()

    // ── Verify resolution ──
    private val _verifyState = MutableStateFlow<VerifyResolutionUiState>(VerifyResolutionUiState.Idle)
    val verifyState: StateFlow<VerifyResolutionUiState> = _verifyState.asStateFlow()

    // ── Snackbar ──
    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    init {
        loadMyComplaints()
    }

    // ── Buildings ──

    fun loadBuildings() {
        viewModelScope.launch {
            _buildingsState.value = BuildingsUiState.Loading
            when (val result = repository.getBuildings()) {
                is Resource.Success -> {
                    _buildingsState.value = BuildingsUiState.Success(result.data)
                }
                is Resource.Error -> {
                    _buildingsState.value = BuildingsUiState.Error(result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    // ── Form field updates ──

    fun onBuildingSelected(building: BuildingDto) {
        _selectedBuilding.value = building
    }

    fun onRoomChanged(value: String) {
        _room.value = value.replace("\n", "").replace("\r", "")
    }

    fun onJobTypeSelected(jobType: String) {
        _selectedJobType.value = jobType
    }

    fun onTitleChanged(value: String) {
        _title.value = value.replace("\n", "").replace("\r", "")
    }

    fun onDescriptionChanged(value: String) {
        _description.value = value
    }

    // ── Submit complaint ──

    fun submitComplaint(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val building = _selectedBuilding.value
            val room = _room.value.trim()
            val jobType = _selectedJobType.value
            val title = _title.value.trim()
            val desc = _description.value.trim()

            // Validation
            if (building == null) {
                _submitState.value = SubmitComplaintUiState.Error("Please select a building")
                return@launch
            }
            if (room.isBlank()) {
                _submitState.value = SubmitComplaintUiState.Error("Please enter room / location")
                return@launch
            }
            if (jobType == null) {
                _submitState.value = SubmitComplaintUiState.Error("Please select a job type")
                return@launch
            }
            if (title.isBlank()) {
                _submitState.value = SubmitComplaintUiState.Error("Please enter a title")
                return@launch
            }
            if (desc.isBlank()) {
                _submitState.value = SubmitComplaintUiState.Error("Please enter a description")
                return@launch
            }

            _submitState.value = SubmitComplaintUiState.Loading

            val request = CreateComplaintRequest(
                title = title,
                description = desc,
                buildingId = building.id,
                room = room,
                jobType = jobType
            )

            when (val result = repository.createComplaint(request)) {
                is Resource.Success -> {
                    _submitState.value = SubmitComplaintUiState.Success
                    _snackbarEvent.emit("Complaint submitted successfully")
                    clearForm()
                    loadMyComplaints()
                    onSuccess()
                }
                is Resource.Error -> {
                    _submitState.value = SubmitComplaintUiState.Error(result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    private fun clearForm() {
        _selectedBuilding.value = null
        _room.value = ""
        _selectedJobType.value = null
        _title.value = ""
        _description.value = ""
        _submitState.value = SubmitComplaintUiState.Idle
    }

    // ── My Complaints ──

    fun loadMyComplaints() {
        viewModelScope.launch {
            _complaintsState.value = StudentComplaintsUiState.Loading
            when (val result = repository.getMyComplaints()) {
                is Resource.Success -> {
                    if (result.data.isEmpty()) {
                        _complaintsState.value = StudentComplaintsUiState.Empty
                    } else {
                        _complaintsState.value = StudentComplaintsUiState.Success(result.data)
                    }
                }
                is Resource.Error -> {
                    _complaintsState.value = StudentComplaintsUiState.Error(result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    // ── Complaint Detail ──

    fun loadComplaintDetail(complaintId: String) {
        viewModelScope.launch {
            _complaintDetailState.value = StudentComplaintDetailUiState.Loading
            when (val result = repository.getComplaintById(complaintId)) {
                is Resource.Success -> {
                    _complaintDetailState.value = StudentComplaintDetailUiState.Success(result.data)
                }
                is Resource.Error -> {
                    _complaintDetailState.value = StudentComplaintDetailUiState.Error(result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    // ── Verify Resolution ──

    fun verifyResolution(complaintId: String) {
        viewModelScope.launch {
            _verifyState.value = VerifyResolutionUiState.Loading
            when (val result = repository.verifyResolution(complaintId)) {
                is Resource.Success -> {
                    _verifyState.value = VerifyResolutionUiState.Success
                    _complaintDetailState.value = StudentComplaintDetailUiState.Success(result.data)
                    _snackbarEvent.emit("Resolution verified successfully")
                    loadMyComplaints()
                }
                is Resource.Error -> {
                    _verifyState.value = VerifyResolutionUiState.Error(result.message)
                    _snackbarEvent.emit(result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun clearSubmitState() {
        _submitState.value = SubmitComplaintUiState.Idle
    }

    fun clearVerifyState() {
        _verifyState.value = VerifyResolutionUiState.Idle
    }
}

// ── Job types ──

val jobTypes = listOf("PLUMBER", "ELECTRICIAN", "CARPENTER")

// ── UI States ──

sealed class StudentComplaintsUiState {
    data object Loading : StudentComplaintsUiState()
    data object Empty : StudentComplaintsUiState()
    data class Success(val complaints: List<ComplaintDto>) : StudentComplaintsUiState()
    data class Error(val message: String) : StudentComplaintsUiState()
}

sealed class StudentComplaintDetailUiState {
    data object Loading : StudentComplaintDetailUiState()
    data class Success(val complaint: ComplaintDetailResponse) : StudentComplaintDetailUiState()
    data class Error(val message: String) : StudentComplaintDetailUiState()
}

sealed class BuildingsUiState {
    data object Loading : BuildingsUiState()
    data class Success(val buildings: List<BuildingDto>) : BuildingsUiState()
    data class Error(val message: String) : BuildingsUiState()
}

sealed class SubmitComplaintUiState {
    data object Idle : SubmitComplaintUiState()
    data object Loading : SubmitComplaintUiState()
    data object Success : SubmitComplaintUiState()
    data class Error(val message: String) : SubmitComplaintUiState()
}

sealed class VerifyResolutionUiState {
    data object Idle : VerifyResolutionUiState()
    data object Loading : VerifyResolutionUiState()
    data object Success : VerifyResolutionUiState()
    data class Error(val message: String) : VerifyResolutionUiState()
}

