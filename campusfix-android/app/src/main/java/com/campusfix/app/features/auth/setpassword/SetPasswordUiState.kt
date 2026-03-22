package com.campusfix.app.features.auth.setpassword

sealed class SetPasswordUiState {
    data object Initial : SetPasswordUiState()
    data object Loading : SetPasswordUiState()
    data class Success(val message: String) : SetPasswordUiState()
    data class Error(val message: String) : SetPasswordUiState()
}

