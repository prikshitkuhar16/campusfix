package com.campusfix.app.features.auth.createcampus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusfix.app.core.util.Resource
import com.campusfix.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CreateCampusViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CreateCampusUiState>(CreateCampusUiState.Initial)
    val uiState: StateFlow<CreateCampusUiState> = _uiState.asStateFlow()

    private val _officialEmail = MutableStateFlow("")
    val officialEmail: StateFlow<String> = _officialEmail.asStateFlow()

    private val _campusName = MutableStateFlow("")
    val campusName: StateFlow<String> = _campusName.asStateFlow()

    private val _campusAddress = MutableStateFlow("")
    val campusAddress: StateFlow<String> = _campusAddress.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _shouldNavigateToOtp = MutableStateFlow(false)
    val shouldNavigateToOtp: StateFlow<Boolean> = _shouldNavigateToOtp.asStateFlow()

    fun onOfficialEmailChange(value: String) {
        _officialEmail.value = value.replace("\n", "").replace("\r", "").trim()
    }

    fun onCampusNameChange(value: String) {
        _campusName.value = value.replace("\n", "").replace("\r", "").trim()
    }

    fun onCampusAddressChange(value: String) {
        _campusAddress.value = value.replace("\n", "").replace("\r", "").trim()
    }

    fun onDescriptionChange(value: String) {
        _description.value = value
    }

    fun onSendOtpClick() {
        viewModelScope.launch {
            if (_officialEmail.value.isBlank()) {
                _uiState.value = CreateCampusUiState.Error("Please enter your official domain email")
                return@launch
            }
            if (!_officialEmail.value.contains("@") || !_officialEmail.value.contains(".")) {
                _uiState.value = CreateCampusUiState.Error("Please enter a valid email address")
                return@launch
            }
            if (_campusName.value.isBlank()) {
                _uiState.value = CreateCampusUiState.Error("Please enter campus name")
                return@launch
            }
            if (_campusAddress.value.isBlank()) {
                _uiState.value = CreateCampusUiState.Error("Please enter campus address")
                return@launch
            }
            if (_description.value.isBlank()) {
                _uiState.value = CreateCampusUiState.Error("Please enter a description")
                return@launch
            }

            _uiState.value = CreateCampusUiState.Loading

            // Check if domain is already taken before sending OTP
            // exists=true  → campus already registered → block creation
            // exists=false → domain is free → can create campus
            when (val domainResult = authRepository.checkDomain(_officialEmail.value)) {
                is Resource.Success -> {
                    if (domainResult.data) {
                        _uiState.value = CreateCampusUiState.Error(
                            "A campus is already registered for this email domain."
                        )
                        return@launch
                    }
                }
                is Resource.Error -> {
                    _uiState.value = CreateCampusUiState.Error("Failed to check domain: ${domainResult.message}")
                    return@launch
                }
                is Resource.Loading -> {}
            }

            when (val result = authRepository.sendCampusOtp(_officialEmail.value)) {
                is Resource.Success -> {
                    _uiState.value = CreateCampusUiState.Success("OTP sent to your email")
                    _shouldNavigateToOtp.value = true
                }
                is Resource.Error -> {
                    _uiState.value = CreateCampusUiState.Error(result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun clearNavigationFlag() {
        _shouldNavigateToOtp.value = false
    }
}
