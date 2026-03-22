package com.campusfix.app.features.dashboard.student

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.campusfix.app.features.dashboard.student.complaints.ComplaintDetailScreen
import com.campusfix.app.features.dashboard.student.complaints.MyComplaintsScreen
import com.campusfix.app.features.dashboard.student.complaints.RaiseComplaintScreen
import com.campusfix.app.features.dashboard.student.complaints.StudentComplaintViewModel
import com.campusfix.app.features.dashboard.student.profile.EditProfileScreen
import com.campusfix.app.features.dashboard.student.profile.StudentProfileScreen
import com.campusfix.app.features.dashboard.student.profile.StudentProfileViewModel

// ── Route constants ──
object StudentRoutes {
    const val RAISE_COMPLAINT = "student_raise_complaint"
    const val MY_COMPLAINTS = "student_my_complaints"
    const val COMPLAINT_DETAIL = "student_complaints/{complaintId}"
    const val PROFILE = "student_profile"
    const val EDIT_PROFILE = "student_profile/edit"

    fun complaintDetail(id: String) = "student_complaints/$id"
}

@Composable
fun StudentNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    complaintViewModel: StudentComplaintViewModel,
    profileViewModel: StudentProfileViewModel,
    onLogout: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = StudentRoutes.RAISE_COMPLAINT,
        modifier = modifier
    ) {
        // ── Raise Complaint ──
        composable(StudentRoutes.RAISE_COMPLAINT) {
            RaiseComplaintScreen(
                viewModel = complaintViewModel,
                onComplaintSubmitted = {
                    navController.navigate(StudentRoutes.MY_COMPLAINTS) {
                        popUpTo(StudentRoutes.RAISE_COMPLAINT) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        // ── My Complaints ──
        composable(StudentRoutes.MY_COMPLAINTS) {
            MyComplaintsScreen(
                viewModel = complaintViewModel,
                onNavigateToComplaintDetail = { complaintId ->
                    navController.navigate(StudentRoutes.complaintDetail(complaintId))
                }
            )
        }

        // ── Complaint Detail ──
        composable(
            route = StudentRoutes.COMPLAINT_DETAIL,
            arguments = listOf(
                navArgument("complaintId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val complaintId = backStackEntry.arguments?.getString("complaintId") ?: ""
            ComplaintDetailScreen(
                viewModel = complaintViewModel,
                complaintId = complaintId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ── Profile ──
        composable(StudentRoutes.PROFILE) {
            StudentProfileScreen(
                viewModel = profileViewModel,
                onNavigateToEditProfile = {
                    navController.navigate(StudentRoutes.EDIT_PROFILE)
                },
                onLogout = onLogout
            )
        }

        // ── Edit Profile ──
        composable(StudentRoutes.EDIT_PROFILE) {
            EditProfileScreen(
                viewModel = profileViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

