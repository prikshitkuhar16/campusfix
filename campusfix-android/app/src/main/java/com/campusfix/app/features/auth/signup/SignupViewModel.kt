package com.campusfix.app.features.auth.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusfix.app.core.util.Resource
import com.campusfix.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SignupViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SignupUiState>(SignupUiState.Initial)
    val uiState: StateFlow<SignupUiState> = _uiState.asStateFlow()

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

    private val _navigationEvent = MutableStateFlow<SignupEvent?>(null)
    val navigationEvent: StateFlow<SignupEvent?> = _navigationEvent.asStateFlow()

    fun onNameChange(newName: String) {
        _name.value = newName.replace("\n", "").replace("\r", "").trim()
    }

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail.replace("\n", "").replace("\r", "").trim()
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword.replace("\n", "").replace("\r", "")
    }

    fun onConfirmPasswordChange(newConfirmPassword: String) {
        _confirmPassword.value = newConfirmPassword.replace("\n", "").replace("\r", "")
    }

    fun onSignupClick() {
        viewModelScope.launch {
            if (_name.value.isBlank()) {
                _uiState.value = SignupUiState.Error("Please enter your name")
                return@launch
            }

            if (_email.value.isBlank()) {
                _uiState.value = SignupUiState.Error("Please enter your email")
                return@launch
            }

            if (!_email.value.contains("@")) {
                _uiState.value = SignupUiState.Error("Please enter a valid email")
                return@launch
            }

            if (_password.value.isBlank()) {
                _uiState.value = SignupUiState.Error("Please enter a password")
                return@launch
            }

            if (_password.value.length < 6) {
                _uiState.value = SignupUiState.Error("Password must be at least 6 characters")
                return@launch
            }

            if (_password.value != _confirmPassword.value) {
                _uiState.value = SignupUiState.Error("Passwords do not match")
                return@launch
            }

            _uiState.value = SignupUiState.Loading

            // Check if a campus exists for this email domain before sending OTP
            // exists=true  → campus is registered → student can proceed
            // exists=false → no campus for this domain → block student
            when (val domainResult = authRepository.checkDomain(_email.value)) {
                is Resource.Success -> {
                    if (!domainResult.data) {
                        _uiState.value = SignupUiState.Error(
                            "No campus registered for your email domain. Ask your campus admin to register first."
                        )
                        return@launch
                    }
                }
                is Resource.Error -> {
                    _uiState.value = SignupUiState.Error("Failed to verify campus: ${domainResult.message}")
                    return@launch
                }
                is Resource.Loading -> {}
            }

            when (val result = authRepository.sendSignupOtp(_email.value)) {
                is Resource.Success -> {
                    _uiState.value = SignupUiState.Success("OTP sent to your email")
                    _navigationEvent.value = SignupEvent.NavigateToOtp
                }
                is Resource.Error -> {
                    _uiState.value = SignupUiState.Error(result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun getSignupData(): Triple<String, String, String> {
        return Triple(_name.value, _email.value, _password.value)
    }

    fun clearNavigationEvent() {
        _navigationEvent.value = null
    }
}

