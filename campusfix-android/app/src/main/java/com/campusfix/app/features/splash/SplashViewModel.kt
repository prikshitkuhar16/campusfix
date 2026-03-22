package com.campusfix.app.features.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusfix.app.core.firebase.FirebaseAuthManager
import com.campusfix.app.core.util.Resource
import com.campusfix.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch



class SplashViewModel(
    private val authRepository: AuthRepository,
    private val firebaseAuthManager: FirebaseAuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Loading)
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            val currentUser = firebaseAuthManager.currentUser
            if (currentUser != null) {
                val idToken = currentUser.getIdToken(false).result?.token
                if (idToken != null) {
                    val result = authRepository.resolveUserRole(idToken)
                    when (result) {
                        is Resource.Success -> {
                            _uiState.value = SplashUiState.NavigateToHome(result.data.role)
                        }
                        is Resource.Error -> {
                            firebaseAuthManager.signOut()
                            _uiState.value = SplashUiState.NavigateToLogin
                        }
                        is Resource.Loading -> {}
                    }
                } else {
                    firebaseAuthManager.signOut()
                    _uiState.value = SplashUiState.NavigateToLogin
                }
            } else {
                _uiState.value = SplashUiState.NavigateToLogin
            }
        }
    }
}

