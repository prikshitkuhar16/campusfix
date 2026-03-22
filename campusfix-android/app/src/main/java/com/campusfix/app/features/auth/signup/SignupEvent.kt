package com.campusfix.app.features.auth.signup

sealed class SignupEvent {
    data object NavigateToLogin : SignupEvent()
    data object NavigateToOtp : SignupEvent()
    data class ShowError(val message: String) : SignupEvent()
}

