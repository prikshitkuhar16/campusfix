package com.campusfix.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.campusfix.app.features.splash.SplashScreen
import com.campusfix.app.features.splash.SplashViewModel

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
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            val viewModel = remember { SplashViewModel(authRepository, firebaseAuthManager) }
            SplashScreen(
                viewModel = viewModel,
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToHome = { role ->
                    val route = when (role) {
                        UserRole.CAMPUS_ADMIN -> Screen.CampusAdminDashboard.route
                        UserRole.STAFF -> Screen.StaffDashboard.route
                        UserRole.BUILDING_ADMIN -> Screen.BuildingAdminDashboard.route
                        UserRole.STUDENT -> Screen.StudentDashboard.route
                    }
                    navController.navigate(route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

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

        composable(Screen.ForgotPassword.route) {
            val viewModel = remember { ForgotPasswordViewModel(authRepository) }
            ForgotPasswordScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

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


    }
}
