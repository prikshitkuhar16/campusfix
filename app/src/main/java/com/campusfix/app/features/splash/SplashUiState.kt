package com.campusfix.app.features.splash

sealed class SplashUiState {
    data object Loading : SplashUiState()
    data object NavigateToLogin : SplashUiState()
    data object NavigateToHome : SplashUiState()
    data class Error(val message: String) : SplashUiState()
}

