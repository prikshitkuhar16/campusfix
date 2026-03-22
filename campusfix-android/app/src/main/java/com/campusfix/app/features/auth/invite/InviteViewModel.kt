package com.campusfix.app.features.auth.invite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusfix.app.core.util.Resource
import com.campusfix.app.domain.model.User
import com.campusfix.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Invite flow ViewModel.
 *
 * Orchestrates:
 *   1. POST /auth/verify-invite  → get user email + role
 *   2. FirebaseAuth.createUserWithEmailAndPassword
 *   3. getIdToken(true)
 *   4. POST /auth/complete-invite  (Bearer)
 *   5. Navigate to role-based dashboard
 */
class InviteViewModel(
    private val authRepository: AuthRepository,
    private val inviteToken: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<InviteUiState>(InviteUiState.Initial)
    val uiState: StateFlow<InviteUiState> = _uiState.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _navigationEvent = MutableStateFlow<InviteEvent?>(null)
    val navigationEvent: StateFlow<InviteEvent?> = _navigationEvent.asStateFlow()

    // Populated after verify-invite
    private var invitedUser: User? = null

    init {
        verifyInviteToken()
    }

    private fun verifyInviteToken() {
        viewModelScope.launch {
            _uiState.value = InviteUiState.Loading
            android.util.Log.d("InviteViewModel", "Verifying invite token...")

            when (val result = authRepository.verifyInvite(inviteToken)) {
                is Resource.Success -> {
                    invitedUser = result.data
                    android.util.Log.d("InviteViewModel", "Invite verified: ${result.data.email}, role: ${result.data.role}")
                    _uiState.value = InviteUiState.InviteVerified
                }
                is Resource.Error -> {
                    android.util.Log.e("InviteViewModel", "Invite verification failed: ${result.message}")
                    _uiState.value = InviteUiState.Error(result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun onNameChange(value: String) {
        _name.value = value.replace("\n", "").replace("\r", "")
    }

    fun onPasswordChange(value: String) {
        _password.value = value.replace("\n", "").replace("\r", "")
    }

    fun onConfirmPasswordChange(value: String) {
        _confirmPassword.value = value.replace("\n", "").replace("\r", "")
    }

    fun onSubmitClick() {
        val user = invitedUser
        if (user == null) {
            _uiState.value = InviteUiState.Error("Invite data not available")
            return
        }

        viewModelScope.launch {
            if (_name.value.trim().isBlank()) {
                _uiState.value = InviteUiState.Error("Name is required")
                return@launch
            }
            if (_password.value.length < 6) {
                _uiState.value = InviteUiState.Error("Password must be at least 6 characters")
                return@launch
            }
            if (_password.value != _confirmPassword.value) {
                _uiState.value = InviteUiState.Error("Passwords do not match")
                return@launch
            }

            _uiState.value = InviteUiState.Loading

            // Step 1: Create Firebase account
            android.util.Log.d("InviteViewModel", "Step 1 — Creating Firebase user for ${user.email}")
            when (val fbResult = authRepository.createFirebaseUser(user.email, _password.value)) {
                is Resource.Success -> {
                    try {
                        // Step 2: Get fresh idToken
                        android.util.Log.d("InviteViewModel", "Step 2 — Getting fresh idToken")
                        val idToken = fbResult.data.getIdToken(true).await().token
                        if (idToken == null) {
                            _uiState.value = InviteUiState.Error("Failed to get authentication token")
                            return@launch
                        }

                        // Step 3: Complete invite on backend
                        android.util.Log.d("InviteViewModel", "Step 3 — Calling POST /auth/complete-invite")
                        when (val completeResult = authRepository.completeInvite(idToken, inviteToken, _name.value.trim())) {
                            is Resource.Success -> {
                                android.util.Log.d("InviteViewModel", "Invite completed! Role: ${completeResult.data.role}")
                                _uiState.value = InviteUiState.Success("Account created successfully!")
                                _navigationEvent.value = InviteEvent.NavigateToDashboard(completeResult.data.role)
                            }
                            is Resource.Error -> {
                                android.util.Log.e("InviteViewModel", "Complete invite failed: ${completeResult.message}")
                                _uiState.value = InviteUiState.Error(completeResult.message)
                            }
                            is Resource.Loading -> {}
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("InviteViewModel", "Token retrieval failed: ${e.message}", e)
                        _uiState.value = InviteUiState.Error("Authentication error: ${e.message}")
                    }
                }
                is Resource.Error -> {
                    android.util.Log.e("InviteViewModel", "Firebase signup failed: ${fbResult.message}")
                    _uiState.value = InviteUiState.Error(fbResult.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun clearNavigationEvent() {
        _navigationEvent.value = null
    }
}

