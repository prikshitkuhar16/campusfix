package com.campusfix.app.features.dashboard.buildingadmin.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusfix.app.core.firebase.FirebaseAuthManager
import com.campusfix.app.core.util.Resource
import com.campusfix.app.data.remote.dto.BuildingAdminProfileResponse
import com.campusfix.app.domain.repository.BuildingAdminRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BuildingAdminProfileViewModel(
    private val repository: BuildingAdminRepository,
    private val firebaseAuthManager: FirebaseAuthManager
) : ViewModel() {

    // ── Profile state ──
    private val _profileState = MutableStateFlow<BAProfileUiState>(BAProfileUiState.Loading)
    val profileState: StateFlow<BAProfileUiState> = _profileState.asStateFlow()

    // ── Edit form ──
    private val _editName = MutableStateFlow("")
    val editName: StateFlow<String> = _editName.asStateFlow()

    private val _editPhoneNumber = MutableStateFlow("")
    val editPhoneNumber: StateFlow<String> = _editPhoneNumber.asStateFlow()

    private val _editState = MutableStateFlow<BAEditProfileUiState>(BAEditProfileUiState.Idle)
    val editState: StateFlow<BAEditProfileUiState> = _editState.asStateFlow()

    // ── Snackbar ──
    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _profileState.value = BAProfileUiState.Loading
            when (val result = repository.getProfile()) {
                is Resource.Success -> {
                    _profileState.value = BAProfileUiState.Success(result.data)
                    _editName.value = result.data.name ?: ""
                    _editPhoneNumber.value = result.data.phoneNumber ?: ""
                }
                is Resource.Error -> {
                    _profileState.value = BAProfileUiState.Error(result.message)
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
                _editState.value = BAEditProfileUiState.Error("Name is required")
                return@launch
            }

            _editState.value = BAEditProfileUiState.Loading
            when (val result = repository.updateProfile(name, phone.ifBlank { null })) {
                is Resource.Success -> {
                    _editState.value = BAEditProfileUiState.Success("Profile updated")
                    _profileState.value = BAProfileUiState.Success(result.data)
                    _snackbarEvent.emit("Profile updated successfully")
                    onSuccess()
                }
                is Resource.Error -> {
                    _editState.value = BAEditProfileUiState.Error(result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun logout() {
        firebaseAuthManager.signOut()
    }

    fun clearEditState() {
        _editState.value = BAEditProfileUiState.Idle
    }
}

// ── UI States ──

sealed class BAProfileUiState {
    data object Loading : BAProfileUiState()
    data class Success(val profile: BuildingAdminProfileResponse) : BAProfileUiState()
    data class Error(val message: String) : BAProfileUiState()
}

sealed class BAEditProfileUiState {
    data object Idle : BAEditProfileUiState()
    data object Loading : BAEditProfileUiState()
    data class Success(val message: String) : BAEditProfileUiState()
    data class Error(val message: String) : BAEditProfileUiState()
}

