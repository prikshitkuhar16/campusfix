package com.campusfix.app.features.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusfix.app.core.util.Resource
import com.campusfix.app.domain.model.UserRole
import com.campusfix.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Initial)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _navigationEvent = MutableStateFlow<LoginEvent?>(null)
    val navigationEvent: StateFlow<LoginEvent?> = _navigationEvent.asStateFlow()

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail.replace("\n", "").replace("\r", "").trim()
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword.replace("\n", "").replace("\r", "")
    }

    fun onLoginClick() {
        viewModelScope.launch {
            if (_email.value.isBlank()) {
                _uiState.value = LoginUiState.Error("Please enter your email")
                return@launch
            }

            if (_password.value.isBlank()) {
                _uiState.value = LoginUiState.Error("Please enter your password")
                return@launch
            }

            _uiState.value = LoginUiState.Loading
            android.util.Log.d("LoginViewModel", "onLoginClick - Starting login for: ${_email.value}")

            when (val firebaseResult = authRepository.signInWithEmail(_email.value, _password.value)) {
                is Resource.Success -> {
                    android.util.Log.d("LoginViewModel", "onLoginClick - Firebase sign-in successful")
                    val idToken = firebaseResult.data.getIdToken(false).result?.token
                    android.util.Log.d("LoginViewModel", "onLoginClick - Got idToken: ${idToken != null}")

                    if (idToken != null) {
                        android.util.Log.d("LoginViewModel", "onLoginClick - Resolving user role...")
                        when (val resolveResult = authRepository.resolveUserRole(idToken)) {
                            is Resource.Success -> {
                                android.util.Log.d("LoginViewModel", "onLoginClick - User role resolved: ${resolveResult.data.role}")
                                _uiState.value = LoginUiState.Success("Login successful")
                                _navigationEvent.value = when (resolveResult.data.role) {
                                    UserRole.CAMPUS_ADMIN -> LoginEvent.NavigateToCampusAdminDashboard
                                    UserRole.STAFF -> LoginEvent.NavigateToStaffDashboard
                                    UserRole.BUILDING_ADMIN -> LoginEvent.NavigateToBuildingAdminDashboard
                                    UserRole.STUDENT -> LoginEvent.NavigateToHome
                                }
                            }
                            is Resource.Error -> {
                                android.util.Log.e("LoginViewModel", "onLoginClick - Resolve role failed: ${resolveResult.message}")
                                _uiState.value = LoginUiState.Error(resolveResult.message)
                            }
                            is Resource.Loading -> {}
                        }
                    } else {
                        android.util.Log.e("LoginViewModel", "onLoginClick - Failed to get idToken")
                        _uiState.value = LoginUiState.Error("Failed to get authentication token")
                    }
                }
                is Resource.Error -> {
                    android.util.Log.e("LoginViewModel", "onLoginClick - Firebase sign-in failed: ${firebaseResult.message}")
                    _uiState.value = LoginUiState.Error(firebaseResult.message)
                }
                is Resource.Loading -> {}
            }
        }
    }


    fun onSignupClick() {
        _navigationEvent.value = LoginEvent.NavigateToSignup
    }

    fun onForgotPasswordClick() {
        _navigationEvent.value = LoginEvent.NavigateToForgotPassword
    }

    fun onCreateCampusClick() {
        _navigationEvent.value = LoginEvent.NavigateToCreateCampus
    }

    fun clearNavigationEvent() {
        _navigationEvent.value = null
    }
}

