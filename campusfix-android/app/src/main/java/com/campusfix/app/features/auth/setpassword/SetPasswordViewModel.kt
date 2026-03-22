package com.campusfix.app.features.auth.setpassword

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * DEPRECATED: This ViewModel is no longer used.
 * Forgot password now uses Firebase sendPasswordResetEmail directly.
 * Invite flow uses InviteViewModel.
 */
class SetPasswordViewModel(
    private val mode: String = "",
    private val email: String = "",
    private val otp: String = "",
    private val inviteToken: String = ""
) : ViewModel() {

    private val _uiState = MutableStateFlow<SetPasswordUiState>(SetPasswordUiState.Initial)
    val uiState: StateFlow<SetPasswordUiState> = _uiState.asStateFlow()

    private val _newPassword = MutableStateFlow("")
    val newPassword: StateFlow<String> = _newPassword.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

    private val _shouldNavigate = MutableStateFlow(false)
    val shouldNavigate: StateFlow<Boolean> = _shouldNavigate.asStateFlow()

    fun onNewPasswordChange(newPasswordValue: String) {
        _newPassword.value = newPasswordValue
    }

    fun onConfirmPasswordChange(confirmPasswordValue: String) {
        _confirmPassword.value = confirmPasswordValue
    }

    fun onSubmitClick() {
        // No-op: this class is deprecated
    }

    fun clearNavigationFlag() {
        _shouldNavigate.value = false
    }
}
