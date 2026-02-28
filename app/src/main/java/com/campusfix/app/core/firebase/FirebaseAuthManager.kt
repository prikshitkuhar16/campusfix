package com.campusfix.app.core.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class FirebaseAuthManager {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    suspend fun signInWithEmail(email: String, password: String): FirebaseUser {
        Log.d("FirebaseAuthManager", "Attempting sign-in for: $email")
        val result = auth.signInWithEmailAndPassword(email, password).await()
        val user = result.user ?: throw Exception("Sign in failed")

        // Reload user to get latest email verification status
        user.reload().await()

        // Check if email is verified
        if (!user.isEmailVerified) {
            Log.w("FirebaseAuthManager", "Email not verified for: $email")
            // Don't throw error, let backend handle it or proceed anyway
            // Backend might handle unverified emails differently
        }

        Log.d("FirebaseAuthManager", "Sign-in successful, emailVerified: ${user.isEmailVerified}")
        return user
    }

    suspend fun createUserWithEmail(email: String, password: String): FirebaseUser {
        Log.d("FirebaseAuthManager", "Creating Firebase account for: $email")
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val user = result.user ?: throw Exception("User creation failed")

        Log.d("FirebaseAuthManager", "Account creation successful")
        return user
    }

    suspend fun deleteCurrentUser() {
        try {
            val user = auth.currentUser
            if (user != null) {
                user.delete().await()
                Log.d("FirebaseAuthManager", "Firebase user deleted: ${user.email}")
            }
        } catch (e: Exception) {
            Log.e("FirebaseAuthManager", "Failed to delete Firebase user: ${e.message}")
        }
    }


    suspend fun sendPasswordResetEmail(email: String) {
        Log.d("FirebaseAuthManager", "Sending password reset email to: $email")
        auth.sendPasswordResetEmail(email).await()
        Log.d("FirebaseAuthManager", "Password reset email sent to: $email")
    }

    suspend fun getIdToken(forceRefresh: Boolean = false): String? {
        return try {
            auth.currentUser?.getIdToken(forceRefresh)?.await()?.token
        } catch (e: Exception) {
            Log.e("FirebaseAuthManager", "Failed to get ID token: ${e.message}")
            null
        }
    }

    fun signOut() {
        auth.signOut()
        Log.d("FirebaseAuthManager", "User signed out")
    }
}

