package com.campusfix.app.features.auth.otp

sealed class OtpEvent {
    data object NavigateToHome : OtpEvent()
    data object NavigateToCampusSetPassword : OtpEvent()
    data class ShowError(val message: String) : OtpEvent()
}

