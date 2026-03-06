package com.campusfix.app.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.campusfix.app.core.firebase.FirebaseAuthManager
import com.campusfix.app.data.repository.AuthRepositoryImpl
import com.campusfix.app.domain.model.UserRole
import com.campusfix.app.features.auth.createcampus.CampusAdminDetailsScreen
import com.campusfix.app.features.auth.createcampus.CampusAdminDetailsViewModel
import com.campusfix.app.features.auth.createcampus.CreateCampusScreen
import com.campusfix.app.features.auth.createcampus.CreateCampusViewModel
import com.campusfix.app.features.auth.forgotpassword.ForgotPasswordScreen
import com.campusfix.app.features.auth.forgotpassword.ForgotPasswordViewModel
import com.campusfix.app.features.auth.invite.InviteScreen
import com.campusfix.app.features.auth.invite.InviteViewModel
import com.campusfix.app.features.auth.login.LoginScreen
import com.campusfix.app.features.auth.login.LoginViewModel
import com.campusfix.app.features.auth.otp.OtpMode
import com.campusfix.app.features.auth.otp.OtpScreen
import com.campusfix.app.features.auth.otp.OtpViewModel
import com.campusfix.app.features.auth.signup.SignupScreen
import com.campusfix.app.features.auth.signup.SignupViewModel
import com.campusfix.app.features.dashboard.buildingadmin.BuildingAdminDashboardScreen
import com.campusfix.app.features.dashboard.campusadmin.CampusAdminDashboardScreen
import com.campusfix.app.features.dashboard.staff.StaffDashboardScreen
import com.campusfix.app.features.dashboard.student.StudentDashboardScreen

@Composable
fun AppNavigation(
    firebaseAuthManager: FirebaseAuthManager,
    authRepository: AuthRepositoryImpl
) {
    val navController = rememberNavController()

    val signupViewModel = remember { SignupViewModel(authRepository) }
    val createCampusViewModel = remember { CreateCampusViewModel(authRepository) }

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        // ──────────────────────────────────────────────
        // Login
        // ──────────────────────────────────────────────
        composable(Screen.Login.route) {
            val viewModel = remember { LoginViewModel(authRepository) }
            LoginScreen(
                viewModel = viewModel,
                onNavigateToHome = {
                    navController.navigate(Screen.StudentDashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToCampusAdminDashboard = {
                    navController.navigate(Screen.CampusAdminDashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToStaffDashboard = {
                    navController.navigate(Screen.StaffDashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToBuildingAdminDashboard = {
                    navController.navigate(Screen.BuildingAdminDashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToSignup = {
                    navController.navigate(Screen.Signup.route)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Screen.ForgotPassword.route)
                },
                onNavigateToCreateCampus = {
                    navController.navigate(Screen.CreateCampus.route)
                }
            )
        }

        // ──────────────────────────────────────────────
        // Student Signup
        // ──────────────────────────────────────────────
        composable(Screen.Signup.route) {
            SignupScreen(
                viewModel = signupViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToOtp = {
                    val email = signupViewModel.email.value
                    navController.navigate("otp/$email/SIGNUP")
                }
            )
        }

        // ──────────────────────────────────────────────
        // Create Campus — Step 1
        // ──────────────────────────────────────────────
        composable(Screen.CreateCampus.route) {
            CreateCampusScreen(
                viewModel = createCampusViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToOtp = {
                    val email = createCampusViewModel.officialEmail.value
                    navController.navigate("otp/$email/CREATE_CAMPUS")
                }
            )
        }

        // ──────────────────────────────────────────────
        // Forgot Password (Firebase-only)
        // ──────────────────────────────────────────────
        composable(Screen.ForgotPassword.route) {
            val viewModel = remember { ForgotPasswordViewModel(authRepository) }
            ForgotPasswordScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ──────────────────────────────────────────────
        // OTP (Signup + Campus only — no forgot password)
        // ──────────────────────────────────────────────
        composable(
            route = "otp/{email}/{mode}",
            arguments = listOf(
                navArgument("email") { type = NavType.StringType },
                navArgument("mode") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            val modeString = backStackEntry.arguments?.getString("mode") ?: "SIGNUP"
            val mode = when (modeString) {
                "SIGNUP" -> OtpMode.SIGNUP
                "CREATE_CAMPUS" -> OtpMode.CREATE_CAMPUS
                else -> OtpMode.SIGNUP
            }

            val signupData = if (mode == OtpMode.SIGNUP) {
                signupViewModel.getSignupData()
            } else null

            val viewModel = remember(email, mode) {
                OtpViewModel(
                    authRepository = authRepository,
                    email = email,
                    mode = mode,
                    signupData = signupData
                )
            }

            OtpScreen(
                viewModel = viewModel,
                email = email,
                mode = mode,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHome = {
                    navController.navigate(Screen.StudentDashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToCampusSetPassword = {
                    val e = createCampusViewModel.officialEmail.value
                    val n = createCampusViewModel.campusName.value
                    val a = createCampusViewModel.campusAddress.value
                    val d = createCampusViewModel.description.value
                    navController.navigate(
                        Screen.CampusSetPassword.createRoute(e, n, a, d)
                    ) {
                        popUpTo(Screen.CreateCampus.route) { inclusive = false }
                    }
                }
            )
        }

        // ──────────────────────────────────────────────
        // Campus Set Password → Firebase signup → POST /campus/create
        // ──────────────────────────────────────────────
        composable(
            route = Screen.CampusSetPassword.route,
            arguments = listOf(
                navArgument("email") { type = NavType.StringType },
                navArgument("campusName") { type = NavType.StringType },
                navArgument("campusAddress") { type = NavType.StringType },
                navArgument("campusDescription") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val email = android.net.Uri.decode(backStackEntry.arguments?.getString("email") ?: "")
            val campusName = android.net.Uri.decode(backStackEntry.arguments?.getString("campusName") ?: "")
            val campusAddress = android.net.Uri.decode(backStackEntry.arguments?.getString("campusAddress") ?: "")
            val campusDescription = android.net.Uri.decode(backStackEntry.arguments?.getString("campusDescription") ?: "")

            val vm = remember {
                CampusAdminDetailsViewModel(
                    authRepository = authRepository,
                    officialEmail = email,
                    campusName = campusName,
                    campusAddress = campusAddress,
                    campusDescription = campusDescription
                )
            }

            CampusAdminDetailsScreen(
                viewModel = vm,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDashboard = {
                    navController.navigate(Screen.CampusAdminDashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // ──────────────────────────────────────────────
        // Invite Flow (deep link: campusfix://invite?token=...)
        // ──────────────────────────────────────────────
        composable(
            route = Screen.Invite.route,
            arguments = listOf(
                navArgument("token") { type = NavType.StringType }
            ),
            deepLinks = listOf(
                navDeepLink { uriPattern = "https://campusfix.app/invite?token={token}" },
                navDeepLink { uriPattern = "campusfix://invite?token={token}" }
            )
        ) { backStackEntry ->
            val token = backStackEntry.arguments?.getString("token") ?: ""

            val vm = remember(token) {
                InviteViewModel(
                    authRepository = authRepository,
                    inviteToken = token
                )
            }

            InviteScreen(
                viewModel = vm,
                onNavigateToDashboard = { role ->
                    val route = when (role) {
                        UserRole.CAMPUS_ADMIN -> Screen.CampusAdminDashboard.route
                        UserRole.STAFF -> Screen.StaffDashboard.route
                        UserRole.BUILDING_ADMIN -> Screen.BuildingAdminDashboard.route
                        UserRole.STUDENT -> Screen.StudentDashboard.route
                    }
                    navController.navigate(route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ──────────────────────────────────────────────
        // Dashboards
        // ──────────────────────────────────────────────
        composable(Screen.CampusAdminDashboard.route) {
            CampusAdminDashboardScreen(
                firebaseAuthManager = firebaseAuthManager,
                onLogout = {
                    firebaseAuthManager.signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.StudentDashboard.route) {
            StudentDashboardScreen(
                firebaseAuthManager = firebaseAuthManager,
                onLogout = {
                    firebaseAuthManager.signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.StaffDashboard.route) {
            StaffDashboardScreen(
                firebaseAuthManager = firebaseAuthManager,
                onLogout = {
                    firebaseAuthManager.signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.BuildingAdminDashboard.route) {
            BuildingAdminDashboardScreen(
                firebaseAuthManager = firebaseAuthManager,
                onLogout = {
                    firebaseAuthManager.signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Home Screen", style = MaterialTheme.typography.headlineMedium)
                    Button(onClick = {
                        firebaseAuthManager.signOut()
                        navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                    }) { Text("Logout") }
                }
            }
        }
    }
}
