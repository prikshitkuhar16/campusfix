package com.campusfix.app.features.dashboard.staff.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusfix.app.core.firebase.FirebaseAuthManager
import com.campusfix.app.core.util.Resource
import com.campusfix.app.data.remote.dto.StaffProfileResponse
import com.campusfix.app.domain.repository.StaffRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StaffProfileViewModel(
    private val repository: StaffRepository,
    private val firebaseAuthManager: FirebaseAuthManager
) : ViewModel() {

    // ── Profile state ──
    private val _profileState = MutableStateFlow<StaffProfileUiState>(StaffProfileUiState.Loading)
    val profileState: StateFlow<StaffProfileUiState> = _profileState.asStateFlow()

    // ── Edit form ──
    private val _editName = MutableStateFlow("")
    val editName: StateFlow<String> = _editName.asStateFlow()

    private val _editPhoneNumber = MutableStateFlow("")
    val editPhoneNumber: StateFlow<String> = _editPhoneNumber.asStateFlow()

    private val _editState = MutableStateFlow<StaffEditProfileUiState>(StaffEditProfileUiState.Idle)
    val editState: StateFlow<StaffEditProfileUiState> = _editState.asStateFlow()

    // ── Snackbar ──
    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _profileState.value = StaffProfileUiState.Loading
            when (val result = repository.getProfile()) {
                is Resource.Success -> {
                    _profileState.value = StaffProfileUiState.Success(result.data)
                    _editName.value = result.data.name ?: ""
                    _editPhoneNumber.value = result.data.phoneNumber ?: ""
                }
                is Resource.Error -> {
                    _profileState.value = StaffProfileUiState.Error(result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun onEditNameChange(value: String) {
        _editName.value = value.replace("\n", "").replace("\r", "")
    }

    fun onEditPhoneNumberChange(value: String) {
        _editPhoneNumber.value = value.replace("\n", "").replace("\r", "")
    }

    fun onSaveProfile(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val name = _editName.value.trim()
            val phone = _editPhoneNumber.value.trim()
            if (name.isBlank()) {
                _editState.value = StaffEditProfileUiState.Error("Name is required")
                return@launch
            }

            _editState.value = StaffEditProfileUiState.Loading
            when (val result = repository.updateProfile(name, phone.ifBlank { null })) {
                is Resource.Success -> {
                    _editState.value = StaffEditProfileUiState.Success("Profile updated")
                    _profileState.value = StaffProfileUiState.Success(result.data)
                    _snackbarEvent.emit("Profile updated successfully")
                    onSuccess()
                }
                is Resource.Error -> {
                    _editState.value = StaffEditProfileUiState.Error(result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun logout() {
        firebaseAuthManager.signOut()
    }

    fun clearEditState() {
        _editState.value = StaffEditProfileUiState.Idle
    }
}

// ── UI States ──

sealed class StaffProfileUiState {
    data object Loading : StaffProfileUiState()
    data class Success(val profile: StaffProfileResponse) : StaffProfileUiState()
    data class Error(val message: String) : StaffProfileUiState()
}

sealed class StaffEditProfileUiState {
    data object Idle : StaffEditProfileUiState()
    data object Loading : StaffEditProfileUiState()
    data class Success(val message: String) : StaffEditProfileUiState()
    data class Error(val message: String) : StaffEditProfileUiState()
}

