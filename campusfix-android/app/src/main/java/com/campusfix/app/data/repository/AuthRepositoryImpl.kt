package com.campusfix.app.data.repository

import com.campusfix.app.core.firebase.FirebaseAuthManager
import com.campusfix.app.core.util.Resource
import com.campusfix.app.data.remote.RetrofitClient
import com.campusfix.app.data.remote.dto.*
import com.campusfix.app.data.remote.mapper.toDomain
import com.campusfix.app.domain.model.User
import com.campusfix.app.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser

class AuthRepositoryImpl(
    private val firebaseAuthManager: FirebaseAuthManager
) : AuthRepository {

    private val api = RetrofitClient.authApiService

    override suspend fun signInWithEmail(email: String, password: String): Resource<FirebaseUser> {
        return try {
            android.util.Log.d("AuthRepository", "signInWithEmail - Attempting Firebase sign-in for: $email")
            val user = firebaseAuthManager.signInWithEmail(email, password)
            android.util.Log.d("AuthRepository", "signInWithEmail - Firebase sign-in successful: ${user.email}")
            Resource.Success(user)
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "signInWithEmail - Firebase sign-in failed: ${e.message}", e)
            Resource.Error(e.message ?: "Unknown error occurred")
        }
    }


    override suspend fun createFirebaseUser(email: String, password: String): Resource<FirebaseUser> {
        return try {
            android.util.Log.d("AuthRepository", "createFirebaseUser - Creating Firebase account for: $email")
            val user = firebaseAuthManager.createUserWithEmail(email, password)
            android.util.Log.d("AuthRepository", "createFirebaseUser - Firebase account created successfully")
            Resource.Success(user)
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "createFirebaseUser - Failed: ${e.message}", e)
            Resource.Error(e.message ?: "Failed to create Firebase account")
        }
    }

    override suspend fun deleteFirebaseUser() {
        android.util.Log.d("AuthRepository", "deleteFirebaseUser - Deleting orphaned Firebase user")
        firebaseAuthManager.deleteCurrentUser()
    }

    override suspend fun resolveUserRole(idToken: String): Resource<User> {
        return try {
            android.util.Log.d("AuthRepository", "resolveUserRole - Making API call with Bearer token")
            val response = api.resolveUserRole("Bearer $idToken")
            if (response.isSuccessful && response.body() != null) {
                val user = response.body()!!.user.toDomain()
                android.util.Log.d("AuthRepository", "resolveUserRole - User resolved: ${user.email}, role: ${user.role}")
                Resource.Success(user)
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("AuthRepository", "resolveUserRole - Failed: code=${response.code()}, errorBody=$errorBody")
                Resource.Error(errorBody ?: response.message() ?: "Failed to resolve user role")
            }
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "resolveUserRole - Exception: ${e.message}", e)
            Resource.Error(e.message ?: "Failed to resolve user role")
        }
    }

    override suspend fun sendSignupOtp(email: String): Resource<Unit> {
        return try {
            val response = api.sendSignupOtp(SendOtpRequest(email))
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error(response.message() ?: "Failed to send OTP")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to send OTP")
        }
    }

    override suspend fun verifySignupOtp(email: String, otp: String): Resource<Unit> {
        return try {
            val response = api.verifySignupOtp(VerifyOtpRequest(email, otp))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.verified) {
                    Resource.Success(Unit)
                } else {
                    Resource.Error(body?.message ?: "OTP verification failed")
                }
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message()
                Resource.Error("OTP verification failed: $errorMsg")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "OTP verification failed")
        }
    }

    override suspend fun createStudent(name: String, email: String, password: String, idToken: String): Resource<User> {
        return try {
            val response = api.createStudent(CreateStudentRequest(idToken, name, email, password))
            if (response.isSuccessful && response.body() != null) {
                val user = response.body()!!.user.toDomain()
                Resource.Success(user)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message()
                Resource.Error("Failed to create student: $errorMsg")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to create student")
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Resource<Unit> {
        return try {
            firebaseAuthManager.sendPasswordResetEmail(email)
            Resource.Success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "sendPasswordResetEmail - Failed: ${e.message}", e)
            Resource.Error(e.message ?: "Failed to send password reset email")
        }
    }

    override suspend fun sendCampusOtp(email: String): Resource<Unit> {
        return try {
            val response = api.sendCampusOtp(SendOtpRequest(email))
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error(response.message() ?: "Failed to send OTP")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to send OTP")
        }
    }

    override suspend fun verifyCampusOtp(email: String, otp: String): Resource<Unit> {
        return try {
            val response = api.verifyCampusOtp(VerifyOtpRequest(email, otp))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.verified) {
                    Resource.Success(Unit)
                } else {
                    Resource.Error(body?.message ?: "OTP verification failed")
                }
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message()
                Resource.Error("OTP verification failed: $errorMsg")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "OTP verification failed")
        }
    }

    override suspend fun checkDomain(email: String): Resource<Boolean> {
        return try {
            val response = api.checkDomain(CheckDomainRequest(email))
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.exists)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message()
                Resource.Error(errorMsg ?: "Failed to check domain")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to check domain availability")
        }
    }

    override suspend fun createCampus(idToken: String, request: CreateCampusRequest): Resource<Unit> {
        return try {
            val response = api.createCampus(
                authorization = "Bearer $idToken",
                request = request
            )
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message()
                Resource.Error(errorMsg ?: "Failed to create campus")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to create campus")
        }
    }

    override suspend fun verifyInvite(token: String): Resource<User> {
        return try {
            val response = api.verifyInvite(VerifyInviteRequest(token))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val user = if (body.user != null) {
                    body.user.toDomain()
                } else if (body.email != null) {
                    // Backend may return flat fields instead of nested user object
                    User(
                        id = "",
                        email = body.email,
                        name = "",
                        role = when (body.role) {
                            "STAFF" -> com.campusfix.app.domain.model.UserRole.STAFF
                            "BUILDING_ADMIN" -> com.campusfix.app.domain.model.UserRole.BUILDING_ADMIN
                            "CAMPUS_ADMIN" -> com.campusfix.app.domain.model.UserRole.CAMPUS_ADMIN
                            else -> com.campusfix.app.domain.model.UserRole.STUDENT
                        },
                        campusId = null,
                        buildingIds = null,
                        isOnboarded = false
                    )
                } else {
                    return Resource.Error("Invalid invite response: no user data")
                }
                Resource.Success(user)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message()
                Resource.Error(errorMsg ?: "Invalid or expired invite")
            }
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "verifyInvite - Exception: ${e.message}", e)
            Resource.Error(e.message ?: "Invalid invite token")
        }
    }

    override suspend fun completeInvite(idToken: String, inviteToken: String, name: String): Resource<User> {
        return try {
            android.util.Log.d("AuthRepository", "completeInvite - calling POST /auth/complete-invite")
            val response = api.completeInvite(
                authorization = "Bearer $idToken",
                request = CompleteInviteRequest(token = inviteToken, name = name)
            )
            if (response.isSuccessful && response.body() != null) {
                val user = response.body()!!.user.toDomain()
                android.util.Log.d("AuthRepository", "completeInvite - Success: ${user.email}, role: ${user.role}")
                Resource.Success(user)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message()
                android.util.Log.e("AuthRepository", "completeInvite - Failed: code=${response.code()}, msg=$errorMsg")
                Resource.Error(errorMsg ?: "Failed to complete invite")
            }
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "completeInvite - Exception: ${e.message}", e)
            Resource.Error(e.message ?: "Failed to complete invite")
        }
    }

    override fun signOut() {
        firebaseAuthManager.signOut()
    }
}
