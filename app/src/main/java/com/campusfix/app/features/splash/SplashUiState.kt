package com.campusfix.app.features.splash

import com.campusfix.app.domain.model.UserRole

sealed class SplashUiState {
    data object Loading : SplashUiState()
    data object NavigateToLogin : SplashUiState()
    data class NavigateToHome(val role: UserRole) : SplashUiState()
    data class Error(val message: String) : SplashUiState()
}

