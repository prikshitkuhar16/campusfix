package com.campusfix.app.features.auth.createcampus

sealed class CampusAdminDetailsUiState {
    data object Initial : CampusAdminDetailsUiState()
    data object Loading : CampusAdminDetailsUiState()
    data class Success(val message: String) : CampusAdminDetailsUiState()
    data class Error(val message: String) : CampusAdminDetailsUiState()
}

