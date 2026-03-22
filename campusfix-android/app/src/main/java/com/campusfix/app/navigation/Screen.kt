package com.campusfix.app.navigation

import android.net.Uri

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object Signup : Screen("signup")
    data object ForgotPassword : Screen("forgot_password")
    data object Otp : Screen("otp/{email}/{mode}")
    data object CreateCampus : Screen("create_campus")
    data object CampusSetPassword : Screen("campus_set_password/{email}/{campusName}/{campusAddress}/{campusDescription}") {
        fun createRoute(email: String, campusName: String, campusAddress: String, campusDescription: String): String {
            return "campus_set_password/${Uri.encode(email)}/${Uri.encode(campusName)}/${Uri.encode(campusAddress)}/${Uri.encode(campusDescription)}"
        }
    }
    data object Invite : Screen("invite/{token}") {
        fun createRoute(token: String): String = "invite/${Uri.encode(token)}"
    }
    data object CampusAdminDashboard : Screen("campus_admin_dashboard")
    data object StaffDashboard : Screen("staff_dashboard")
    data object BuildingAdminDashboard : Screen("building_admin_dashboard")
    data object StudentDashboard : Screen("student_dashboard")
}
