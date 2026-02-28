package com.campusfix.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.campusfix.app.core.firebase.FirebaseAuthManager
import com.campusfix.app.data.repository.AuthRepositoryImpl
import com.campusfix.app.navigation.AppNavigation
import com.campusfix.app.ui.theme.CampusFixTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val firebaseAuthManager = FirebaseAuthManager()
        val authRepository = AuthRepositoryImpl(firebaseAuthManager)

        setContent {
            CampusFixTheme {
                AppNavigation(
                    firebaseAuthManager = firebaseAuthManager,
                    authRepository = authRepository
                )
            }
        }
    }
}
