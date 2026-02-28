package com.campusfix.app.features.auth.invite

import com.campusfix.app.domain.model.UserRole

sealed class InviteEvent {
    data class NavigateToDashboard(val role: UserRole) : InviteEvent()
}

