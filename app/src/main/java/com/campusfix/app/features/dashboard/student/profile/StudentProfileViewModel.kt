package com.campusfix.app.features.dashboard.student.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusfix.app.core.firebase.FirebaseAuthManager
import com.campusfix.app.core.util.Resource
import com.campusfix.app.data.remote.dto.StudentProfileResponse
import com.campusfix.app.domain.repository.StudentRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StudentProfileViewModel(
    private val repository: StudentRepository,
    private val firebaseAuthManager: FirebaseAuthManager
) : ViewModel() {

    // ── Profile state ──
    private val _profileState = MutableStateFlow<StudentProfileUiState>(StudentProfileUiState.Loading)
    val profileState: StateFlow<StudentProfileUiState> = _profileState.asStateFlow()

    // ── Edit form ──
    private val _editName = MutableStateFlow("")
    val editName: StateFlow<String> = _editName.asStateFlow()

    private val _editState = MutableStateFlow<StudentEditProfileUiState>(StudentEditProfileUiState.Idle)
    val editState: StateFlow<StudentEditProfileUiState> = _editState.asStateFlow()

    // ── Snackbar ──
    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _profileState.value = StudentProfileUiState.Loading
            when (val result = repository.getProfile()) {
                is Resource.Success -> {
                    _profileState.value = StudentProfileUiState.Success(result.data)
                    _editName.value = result.data.name ?: ""
                }
                is Resource.Error -> {
                    _profileState.value = StudentProfileUiState.Error(result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun onEditNameChange(value: String) {
        _editName.value = value.replace("\n", "").replace("\r", "")
    }

    fun onSaveProfile(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val name = _editName.value.trim()
            if (name.isBlank()) {
                _editState.value = StudentEditProfileUiState.Error("Name is required")
                return@launch
            }

            _editState.value = StudentEditProfileUiState.Loading
            when (val result = repository.updateProfile(name)) {
                is Resource.Success -> {
                    _editState.value = StudentEditProfileUiState.Success("Profile updated")
                    _profileState.value = StudentProfileUiState.Success(result.data)
                    _snackbarEvent.emit("Profile updated successfully")
                    onSuccess()
                }
                is Resource.Error -> {
                    _editState.value = StudentEditProfileUiState.Error(result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun logout() {
        firebaseAuthManager.signOut()
    }

    fun clearEditState() {
        _editState.value = StudentEditProfileUiState.Idle
    }
}

// ── UI States ──

sealed class StudentProfileUiState {
    data object Loading : StudentProfileUiState()
    data class Success(val profile: StudentProfileResponse) : StudentProfileUiState()
    data class Error(val message: String) : StudentProfileUiState()
}

sealed class StudentEditProfileUiState {
    data object Idle : StudentEditProfileUiState()
    data object Loading : StudentEditProfileUiState()
    data class Success(val message: String) : StudentEditProfileUiState()
    data class Error(val message: String) : StudentEditProfileUiState()
}

