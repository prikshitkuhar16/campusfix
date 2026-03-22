package com.campusfix.app.features.auth.invite

sealed class InviteUiState {
    data object Initial : InviteUiState()
    data object Loading : InviteUiState()
    data object InviteVerified : InviteUiState()
    data class Success(val message: String) : InviteUiState()
    data class Error(val message: String) : InviteUiState()
}

