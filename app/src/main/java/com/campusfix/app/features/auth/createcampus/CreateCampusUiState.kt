package com.campusfix.app.features.auth.createcampus

sealed class CreateCampusUiState {
    data object Initial : CreateCampusUiState()
    data object Loading : CreateCampusUiState()
    data class Success(val message: String) : CreateCampusUiState()
    data class Error(val message: String) : CreateCampusUiState()
}

