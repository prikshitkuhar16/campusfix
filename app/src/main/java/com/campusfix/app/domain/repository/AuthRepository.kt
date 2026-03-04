package com.campusfix.app.domain.repository

import com.campusfix.app.core.util.Resource
import com.campusfix.app.data.remote.dto.CreateCampusRequest
import com.campusfix.app.domain.model.User
import com.google.firebase.auth.FirebaseUser

interface AuthRepository {
    suspend fun signInWithEmail(email: String, password: String): Resource<FirebaseUser>
    suspend fun createFirebaseUser(email: String, password: String): Resource<FirebaseUser>
    suspend fun deleteFirebaseUser()
    suspend fun resolveUserRole(idToken: String): Resource<User>
    suspend fun sendSignupOtp(email: String): Resource<Unit>
    suspend fun verifySignupOtp(email: String, otp: String): Resource<Unit>
    suspend fun createStudent(name: String, email: String, password: String, idToken: String): Resource<User>
    suspend fun sendPasswordResetEmail(email: String): Resource<Unit>
    suspend fun sendCampusOtp(email: String): Resource<Unit>
    suspend fun verifyCampusOtp(email: String, otp: String): Resource<Unit>
    suspend fun checkDomain(email: String): Resource<Boolean>
    suspend fun createCampus(idToken: String, request: CreateCampusRequest): Resource<Unit>
    suspend fun verifyInvite(token: String): Resource<User>
    suspend fun completeInvite(idToken: String, inviteToken: String, name: String): Resource<User>
    fun signOut()
}
