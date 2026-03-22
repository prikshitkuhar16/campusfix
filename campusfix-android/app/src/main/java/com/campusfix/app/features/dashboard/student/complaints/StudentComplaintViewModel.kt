package com.campusfix.app.features.dashboard.student.complaints

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusfix.app.core.util.Resource
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

    private val _room = MutableStateFlow("")
    val room: StateFlow<String> = _room.asStateFlow()

    private val _selectedJobType = MutableStateFlow<String?>(null)
    val selectedJobType: StateFlow<String?> = _selectedJobType.asStateFlow()

    private val _complaint = MutableStateFlow("")
    val complaint: StateFlow<String> = _complaint.asStateFlow()

    private val _availableAnytime = MutableStateFlow(false)
    val availableAnytime: StateFlow<Boolean> = _availableAnytime.asStateFlow()

    private val _availableFrom = MutableStateFlow("")
    val availableFrom: StateFlow<String> = _availableFrom.asStateFlow()

    private val _availableTo = MutableStateFlow("")
    val availableTo: StateFlow<String> = _availableTo.asStateFlow()

    private val _submitState = MutableStateFlow<SubmitComplaintUiState>(SubmitComplaintUiState.Idle)
    val submitState: StateFlow<SubmitComplaintUiState> = _submitState.asStateFlow()

    // ── Verify resolution ──
    private val _verifyState = MutableStateFlow<VerifyResolutionUiState>(VerifyResolutionUiState.Idle)
    val verifyState: StateFlow<VerifyResolutionUiState> = _verifyState.asStateFlow()

    private val _reopenState = MutableStateFlow<ReopenComplaintUiState>(ReopenComplaintUiState.Idle)
    val reopenState: StateFlow<ReopenComplaintUiState> = _reopenState.asStateFlow()

    // ── Snackbar ──
    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    // ── Building status ──
    private val _isBuildingSet = MutableStateFlow<Boolean?>(null)
    val isBuildingSet: StateFlow<Boolean?> = _isBuildingSet.asStateFlow()

    init {
        loadMyComplaints()
        checkBuildingSet()
    }

    // ── Form field updates ──


    fun onRoomChanged(value: String) {
        _room.value = value.replace("\n", "").replace("\r", "")
    }

    fun onJobTypeSelected(jobType: String) {
        _selectedJobType.value = jobType
    }

    fun onComplaintChanged(value: String) {
        _complaint.value = value
    }

    fun onAvailableAnytimeChanged(value: Boolean) {
        _availableAnytime.value = value
        if (value) {
            _availableFrom.value = ""
            _availableTo.value = ""
        }
    }

    fun onAvailableFromChanged(value: String) {
        _availableFrom.value = value
    }

    fun onAvailableToChanged(value: String) {
        _availableTo.value = value
    }

    private fun checkBuildingSet() {
        viewModelScope.launch {
            when (val result = repository.getProfile()) {
                is Resource.Success -> {
                    _isBuildingSet.value = !result.data.buildingId.isNullOrBlank()
                }
                is Resource.Error -> {
                    // Assume true to not block user if profile fetch fails, let backend handle it
                    _isBuildingSet.value = true
                }
                is Resource.Loading -> {}
            }
        }
    }

    // ── Submit complaint ──

    fun submitComplaint(onSuccess: () -> Unit) {
        viewModelScope.launch {
            // Check building status first
            if (_isBuildingSet.value == false) {
                _submitState.value = SubmitComplaintUiState.Error("You must set your building in your profile before creating a complaint")
                return@launch
            }

            // Ensure we have checked building status if it is still loading (null)
            if (_isBuildingSet.value == null) {
                _submitState.value = SubmitComplaintUiState.Loading
                checkBuildingSet()
                // Wait for a short moment or simply proceed and let backend handle if still null?
                // Better: fetch profile synchronously here inside coroutine
                val profileResult = repository.getProfile()
                if (profileResult is Resource.Success) {
                    val isSet = !profileResult.data.buildingId.isNullOrBlank()
                    _isBuildingSet.value = isSet
                    if (!isSet) {
                        _submitState.value = SubmitComplaintUiState.Error("You must set your building in your profile before creating a complaint")
                        return@launch
                    }
                }
            }

            val room = _room.value.trim()
            val jobType = _selectedJobType.value
            val complaintText = _complaint.value.trim()
            val isAnytime = _availableAnytime.value
            val from = _availableFrom.value.trim()
            val to = _availableTo.value.trim()

            // Validation
            if (room.isBlank()) {
                _submitState.value = SubmitComplaintUiState.Error("Please enter room / location")
                return@launch
            }
            if (jobType == null) {
                _submitState.value = SubmitComplaintUiState.Error("Please select a job type")
                return@launch
            }
            if (complaintText.isBlank()) {
                _submitState.value = SubmitComplaintUiState.Error("Please enter the complaint")
                return@launch
            }
            if (!isAnytime && (from.isBlank() || to.isBlank())) {
                _submitState.value = SubmitComplaintUiState.Error("Please enter available from/to times or select 'Available Anytime'")
                return@launch
            }

            _submitState.value = SubmitComplaintUiState.Loading

            val request = CreateComplaintRequest(
                room = room,
                jobType = jobType,
                complaint = complaintText,
                availableAnytime = isAnytime,
                availableFrom = if (isAnytime) null else from,
                availableTo = if (isAnytime) null else to
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
                    if (result.message.contains("set your building", ignoreCase = true)) {
                        _isBuildingSet.value = false
                        _submitState.value = SubmitComplaintUiState.Error("Please set your building in profile")
                    } else {
                        _submitState.value = SubmitComplaintUiState.Error(result.message)
                    }
                }
                is Resource.Loading -> {}
            }
        }
    }

    private fun clearForm() {
        _room.value = ""
        _selectedJobType.value = null
        _complaint.value = ""
        _availableAnytime.value = false
        _availableFrom.value = ""
        _availableTo.value = ""
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

    fun reopenComplaint(complaintId: String) {
        viewModelScope.launch {
            _reopenState.value = ReopenComplaintUiState.Loading
            when (val result = repository.updateComplaintStatus(complaintId, "ASSIGNED")) {
                is Resource.Success -> {
                    _reopenState.value = ReopenComplaintUiState.Success
                    _complaintDetailState.value = StudentComplaintDetailUiState.Success(result.data)
                    _snackbarEvent.emit("Complaint reopened successfully")
                    loadMyComplaints()
                }
                is Resource.Error -> {
                    _reopenState.value = ReopenComplaintUiState.Error(result.message)
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

    fun clearReopenState() {
        _reopenState.value = ReopenComplaintUiState.Idle
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

sealed class ReopenComplaintUiState {
    data object Idle : ReopenComplaintUiState()
    data object Loading : ReopenComplaintUiState()
    data object Success : ReopenComplaintUiState()
    data class Error(val message: String) : ReopenComplaintUiState()
}
