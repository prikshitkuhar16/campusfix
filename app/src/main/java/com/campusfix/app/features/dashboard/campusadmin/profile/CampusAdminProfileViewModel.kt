package com.campusfix.app.features.dashboard.campusadmin.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusfix.app.core.firebase.FirebaseAuthManager
import com.campusfix.app.core.util.Resource
import com.campusfix.app.data.remote.dto.ProfileResponse
import com.campusfix.app.domain.repository.CampusAdminRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CampusAdminProfileViewModel(
    private val repository: CampusAdminRepository,
    private val firebaseAuthManager: FirebaseAuthManager
) : ViewModel() {

    // ── Profile state ──
    private val _profileState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val profileState: StateFlow<ProfileUiState> = _profileState.asStateFlow()

    // ── Edit form ──
    private val _editName = MutableStateFlow("")
    val editName: StateFlow<String> = _editName.asStateFlow()

    private val _editPhoneNumber = MutableStateFlow("")
    val editPhoneNumber: StateFlow<String> = _editPhoneNumber.asStateFlow()

    private val _editState = MutableStateFlow<EditProfileUiState>(EditProfileUiState.Idle)
    val editState: StateFlow<EditProfileUiState> = _editState.asStateFlow()

    // ── Snackbar ──
    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _profileState.value = ProfileUiState.Loading
            when (val result = repository.getProfile()) {
                is Resource.Success -> {
                    _profileState.value = ProfileUiState.Success(result.data)
                    _editName.value = result.data.name ?: ""
                    _editPhoneNumber.value = result.data.phoneNumber ?: ""
                }
                is Resource.Error -> {
                    _profileState.value = ProfileUiState.Error(result.message)
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
                _editState.value = EditProfileUiState.Error("Name is required")
                return@launch
            }

            _editState.value = EditProfileUiState.Loading
            when (val result = repository.updateProfile(name, phone.ifBlank { null })) {
                is Resource.Success -> {
                    _editState.value = EditProfileUiState.Success("Profile updated")
                    _profileState.value = ProfileUiState.Success(result.data)
                    _snackbarEvent.emit("Profile updated successfully")
                    onSuccess()
                }
                is Resource.Error -> {
                    _editState.value = EditProfileUiState.Error(result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun logout() {
        firebaseAuthManager.signOut()
    }

    fun clearEditState() {
        _editState.value = EditProfileUiState.Idle
    }
}

// ── UI States ──

sealed class ProfileUiState {
    data object Loading : ProfileUiState()
    data class Success(val profile: ProfileResponse) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

sealed class EditProfileUiState {
    data object Idle : EditProfileUiState()
    data object Loading : EditProfileUiState()
    data class Success(val message: String) : EditProfileUiState()
    data class Error(val message: String) : EditProfileUiState()
}

