package com.campusfix.app.features.auth.forgotpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusfix.app.core.util.Resource
import com.campusfix.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ForgotPasswordUiState>(ForgotPasswordUiState.Initial)
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail.replace("\n", "").replace("\r", "").trim()
    }

    fun onSendResetEmailClick() {
        viewModelScope.launch {
            if (_email.value.isBlank()) {
                _uiState.value = ForgotPasswordUiState.Error("Please enter your email")
                return@launch
            }

            if (!_email.value.contains("@")) {
                _uiState.value = ForgotPasswordUiState.Error("Please enter a valid email")
                return@launch
            }

            _uiState.value = ForgotPasswordUiState.Loading

            when (val result = authRepository.sendPasswordResetEmail(_email.value)) {
                is Resource.Success -> {
                    _uiState.value = ForgotPasswordUiState.Success("Password reset email sent. Check your inbox.")
                }
                is Resource.Error -> {
                    _uiState.value = ForgotPasswordUiState.Error(result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }
}
