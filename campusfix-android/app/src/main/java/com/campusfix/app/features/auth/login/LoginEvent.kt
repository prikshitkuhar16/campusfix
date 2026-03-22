package com.campusfix.app.features.auth.login

sealed class LoginEvent {
    data object NavigateToHome : LoginEvent()
    data object NavigateToCampusAdminDashboard : LoginEvent()
    data object NavigateToStaffDashboard : LoginEvent()
    data object NavigateToBuildingAdminDashboard : LoginEvent()
    data object NavigateToSignup : LoginEvent()
    data object NavigateToForgotPassword : LoginEvent()
    data object NavigateToCreateCampus : LoginEvent()
    data class ShowError(val message: String) : LoginEvent()
}

