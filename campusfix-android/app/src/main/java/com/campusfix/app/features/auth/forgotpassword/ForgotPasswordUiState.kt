package com.campusfix.app.features.auth.forgotpassword

sealed class ForgotPasswordUiState {
    data object Initial : ForgotPasswordUiState()
    data object Loading : ForgotPasswordUiState()
    data class Success(val message: String) : ForgotPasswordUiState()
    data class Error(val message: String) : ForgotPasswordUiState()
}

