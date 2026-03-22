package com.campusfix.app.features.auth.createcampus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusfix.app.core.util.Resource
import com.campusfix.app.data.remote.dto.CreateCampusRequest
import com.campusfix.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Set-password screen for campus admin creation.
 *
 * Receives campus info from the shared CreateCampusViewModel via nav-level wiring.
 * Orchestrates:
 *   1. FirebaseAuth.createUserWithEmailAndPassword
 *   2. getIdToken(true)
 *   3. POST /campus/create with Bearer token
 */
class CampusAdminDetailsViewModel(
    private val authRepository: AuthRepository,
    private val officialEmail: String,
    private val campusName: String,
    private val campusAddress: String,
    private val campusDescription: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<CampusAdminDetailsUiState>(CampusAdminDetailsUiState.Initial)
    val uiState: StateFlow<CampusAdminDetailsUiState> = _uiState.asStateFlow()

    private val _adminName = MutableStateFlow("")
    val adminName: StateFlow<String> = _adminName.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

    private val _shouldNavigateToDashboard = MutableStateFlow(false)
    val shouldNavigateToDashboard: StateFlow<Boolean> = _shouldNavigateToDashboard.asStateFlow()

    fun onAdminNameChange(value: String) {
        _adminName.value = value.replace("\n", "").replace("\r", "").trim()
    }

    fun onPasswordChange(value: String) {
        _password.value = value.replace("\n", "").replace("\r", "")
    }

    fun onConfirmPasswordChange(value: String) {
        _confirmPassword.value = value.replace("\n", "").replace("\r", "")
    }

    fun onSubmitClick() {
        viewModelScope.launch {
            if (_adminName.value.isBlank()) {
                _uiState.value = CampusAdminDetailsUiState.Error("Please enter your name")
                return@launch
            }
            if (_password.value.length < 6) {
                _uiState.value = CampusAdminDetailsUiState.Error("Password must be at least 6 characters")
                return@launch
            }
            if (_password.value != _confirmPassword.value) {
                _uiState.value = CampusAdminDetailsUiState.Error("Passwords do not match")
                return@launch
            }

            _uiState.value = CampusAdminDetailsUiState.Loading
            android.util.Log.d("CampusSetPassword", "Step 1 — Creating Firebase user for $officialEmail")

            // Step 1: Create Firebase user
            when (val fbResult = authRepository.createFirebaseUser(officialEmail, _password.value)) {
                is Resource.Success -> {
                    android.util.Log.d("CampusSetPassword", "Step 2 — Getting fresh idToken")
                    try {
                        // Step 2: Get fresh idToken
                        val idToken = fbResult.data.getIdToken(true).await().token
                        if (idToken == null) {
                            _uiState.value = CampusAdminDetailsUiState.Error("Failed to get authentication token")
                            return@launch
                        }

                        android.util.Log.d("CampusSetPassword", "Step 3 — Calling POST /campus/create")

                        // Step 3: Create campus in backend
                        val request = CreateCampusRequest(
                            name = _adminName.value,
                            campusName = campusName,
                            campusAddress = campusAddress,
                            description = campusDescription
                        )

                        when (val createResult = authRepository.createCampus(idToken, request)) {
                            is Resource.Success -> {
                                android.util.Log.d("CampusSetPassword", "Campus created successfully!")
                                _uiState.value = CampusAdminDetailsUiState.Success("Campus created successfully!")
                                _shouldNavigateToDashboard.value = true
                            }
                            is Resource.Error -> {
                                android.util.Log.e("CampusSetPassword", "Create campus failed: ${createResult.message}")
                                _uiState.value = CampusAdminDetailsUiState.Error(createResult.message)
                            }
                            is Resource.Loading -> {}
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("CampusSetPassword", "Token retrieval failed: ${e.message}", e)
                        _uiState.value = CampusAdminDetailsUiState.Error("Authentication error: ${e.message}")
                    }
                }
                is Resource.Error -> {
                    android.util.Log.e("CampusSetPassword", "Firebase signup failed: ${fbResult.message}")
                    _uiState.value = CampusAdminDetailsUiState.Error(fbResult.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun clearNavigationFlag() {
        _shouldNavigateToDashboard.value = false
    }
}
