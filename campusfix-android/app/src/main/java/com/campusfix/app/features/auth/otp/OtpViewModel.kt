package com.campusfix.app.features.auth.otp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusfix.app.core.util.Resource
import com.campusfix.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

enum class OtpMode {
    SIGNUP,
    CREATE_CAMPUS
}

class OtpViewModel(
    private val authRepository: AuthRepository,
    private val email: String,
    private val mode: OtpMode,
    private val signupData: Triple<String, String, String>? = null
) : ViewModel() {

    init {
        android.util.Log.d("OtpViewModel", "Created with email=$email, mode=$mode")
        android.util.Log.d("OtpViewModel", "signupData=$signupData")
    }

    private val _uiState = MutableStateFlow<OtpUiState>(OtpUiState.Initial)
    val uiState: StateFlow<OtpUiState> = _uiState.asStateFlow()

    private val _otp = MutableStateFlow("")
    val otp: StateFlow<String> = _otp.asStateFlow()

    private val _navigationEvent = MutableStateFlow<OtpEvent?>(null)
    val navigationEvent: StateFlow<OtpEvent?> = _navigationEvent.asStateFlow()

    fun onOtpChange(newOtp: String) {
        if (newOtp.length <= 6 && newOtp.all { it.isDigit() }) {
            _otp.value = newOtp
        }
    }

    fun onVerifyClick() {
        viewModelScope.launch {
            if (_otp.value.isBlank()) {
                _uiState.value = OtpUiState.Error("Please enter OTP")
                return@launch
            }

            if (_otp.value.length != 6) {
                _uiState.value = OtpUiState.Error("OTP must be 6 digits")
                return@launch
            }

            _uiState.value = OtpUiState.Loading

            when (mode) {
                OtpMode.SIGNUP -> handleSignupOtp()
                OtpMode.CREATE_CAMPUS -> handleCampusOtp()
            }
        }
    }


    private suspend fun handleSignupOtp() {
        android.util.Log.d("OtpViewModel", "handleSignupOtp - Starting OTP verification")
        android.util.Log.d("OtpViewModel", "handleSignupOtp - email=$email, otp=${_otp.value}")
        android.util.Log.d("OtpViewModel", "handleSignupOtp - signupData=$signupData")

        when (val verifyResult = authRepository.verifySignupOtp(email, _otp.value)) {
            is Resource.Success -> {
                android.util.Log.d("OtpViewModel", "handleSignupOtp - OTP verified successfully!")

                if (signupData != null) {
                    val (name, email, password) = signupData
                    android.util.Log.d("OtpViewModel", "handleSignupOtp - signupData found, creating Firebase account...")

                    // Step 1: Create Firebase account (domain already verified before OTP was sent)
                    when (val firebaseResult = authRepository.createFirebaseUser(email, password)) {
                        is Resource.Success -> {
                            android.util.Log.d("OtpViewModel", "handleSignupOtp - Firebase account created!")

                            // Step 3: Get idToken from Firebase user
                            val idToken = firebaseResult.data.getIdToken(false).await().token
                            if (idToken != null) {
                                android.util.Log.d("OtpViewModel", "handleSignupOtp - Got Firebase idToken, creating student in backend...")

                                // Step 4: Create student in backend with idToken
                                when (val createResult = authRepository.createStudent(name, email, password, idToken)) {
                                    is Resource.Success -> {
                                        android.util.Log.d("OtpViewModel", "handleSignupOtp - Student created successfully!")
                                        _uiState.value = OtpUiState.Success("Account created successfully")
                                        _navigationEvent.value = OtpEvent.NavigateToHome
                                    }
                                    is Resource.Error -> {
                                        android.util.Log.e("OtpViewModel", "handleSignupOtp - Create student failed: ${createResult.message}")
                                        // Delete orphaned Firebase user since backend rejected
                                        authRepository.deleteFirebaseUser()
                                        _uiState.value = OtpUiState.Error(createResult.message)
                                    }
                                    is Resource.Loading -> {}
                                }
                            } else {
                                android.util.Log.e("OtpViewModel", "handleSignupOtp - Failed to get Firebase idToken")
                                // Delete orphaned Firebase user
                                authRepository.deleteFirebaseUser()
                                _uiState.value = OtpUiState.Error("Failed to get authentication token")
                            }
                        }
                        is Resource.Error -> {
                            android.util.Log.e("OtpViewModel", "handleSignupOtp - Firebase account creation failed: ${firebaseResult.message}")
                            _uiState.value = OtpUiState.Error("Failed to create account: ${firebaseResult.message}")
                        }
                        is Resource.Loading -> {}
                    }
                } else {
                    android.util.Log.e("OtpViewModel", "handleSignupOtp - ERROR: signupData is NULL!")
                    _uiState.value = OtpUiState.Error("Signup data not found")
                }
            }
            is Resource.Error -> {
                android.util.Log.e("OtpViewModel", "handleSignupOtp - OTP verification failed: ${verifyResult.message}")
                _uiState.value = OtpUiState.Error(verifyResult.message)
            }
            is Resource.Loading -> {}
        }
    }

    private suspend fun handleCampusOtp() {
        android.util.Log.d("OtpViewModel", "handleCampusOtp - email=$email, otp=${_otp.value}")

        // Verify campus OTP (domain already checked before OTP was sent)
        when (val verifyResult = authRepository.verifyCampusOtp(email, _otp.value)) {
            is Resource.Success -> {
                android.util.Log.d("OtpViewModel", "handleCampusOtp - OTP verified, navigating to set password")
                _uiState.value = OtpUiState.Success("Email verified")
                _navigationEvent.value = OtpEvent.NavigateToCampusSetPassword
            }
            is Resource.Error -> {
                android.util.Log.e("OtpViewModel", "handleCampusOtp - OTP verification failed: ${verifyResult.message}")
                _uiState.value = OtpUiState.Error(verifyResult.message)
            }
            is Resource.Loading -> {}
        }
    }

    fun onResendOtp() {
        viewModelScope.launch {
            _uiState.value = OtpUiState.Loading

            val result = when (mode) {
                OtpMode.CREATE_CAMPUS -> authRepository.sendCampusOtp(email)
                OtpMode.SIGNUP -> authRepository.sendSignupOtp(email)
            }

            when (result) {
                is Resource.Success -> {
                    _uiState.value = OtpUiState.Success("OTP resent successfully")
                }
                is Resource.Error -> {
                    _uiState.value = OtpUiState.Error(result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun clearNavigationEvent() {
        _navigationEvent.value = null
    }
}

