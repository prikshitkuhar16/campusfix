package com.campusfix.app.features.auth.signup

sealed class SignupUiState {
    data object Initial : SignupUiState()
    data object Loading : SignupUiState()
    data class Success(val message: String) : SignupUiState()
    data class Error(val message: String) : SignupUiState()
}

