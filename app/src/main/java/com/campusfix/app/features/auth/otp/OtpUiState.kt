package com.campusfix.app.features.auth.otp

sealed class OtpUiState {
    data object Initial : OtpUiState()
    data object Loading : OtpUiState()
    data class Success(val message: String) : OtpUiState()
    data class Error(val message: String) : OtpUiState()
}

