package com.campusfix.app.features.dashboard.staff

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.campusfix.app.features.dashboard.staff.complaints.AssignedComplaintsScreen
import com.campusfix.app.features.dashboard.staff.complaints.ComplaintDetailScreen
import com.campusfix.app.features.dashboard.staff.complaints.ComplaintHistoryScreen
import com.campusfix.app.features.dashboard.staff.complaints.StaffComplaintsViewModel
import com.campusfix.app.features.dashboard.staff.profile.EditProfileScreen
import com.campusfix.app.features.dashboard.staff.profile.StaffProfileScreen
import com.campusfix.app.features.dashboard.staff.profile.StaffProfileViewModel

// ── Route constants ──
object StaffRoutes {
    const val ASSIGNED = "staff_assigned"
    const val COMPLAINT_DETAIL = "staff_complaints/{complaintId}"
    const val HISTORY = "staff_history"
    const val PROFILE = "staff_profile"
    const val EDIT_PROFILE = "staff_profile/edit"

    fun complaintDetail(id: String) = "staff_complaints/$id"
}

@Composable
fun StaffNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    complaintsViewModel: StaffComplaintsViewModel,
    profileViewModel: StaffProfileViewModel,
    onLogout: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = StaffRoutes.ASSIGNED,
        modifier = modifier
    ) {
        // ── Assigned Complaints ──
        composable(StaffRoutes.ASSIGNED) {
            AssignedComplaintsScreen(
                viewModel = complaintsViewModel,
                onNavigateToComplaintDetail = { complaintId ->
                    navController.navigate(StaffRoutes.complaintDetail(complaintId))
                }
            )
        }

        // ── Complaint Detail ──
        composable(
            route = StaffRoutes.COMPLAINT_DETAIL,
            arguments = listOf(
                navArgument("complaintId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val complaintId = backStackEntry.arguments?.getString("complaintId") ?: ""
            ComplaintDetailScreen(
                viewModel = complaintsViewModel,
                complaintId = complaintId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ── History ──
        composable(StaffRoutes.HISTORY) {
            ComplaintHistoryScreen(
                viewModel = complaintsViewModel,
                onNavigateToComplaintDetail = { complaintId ->
                    navController.navigate(StaffRoutes.complaintDetail(complaintId))
                }
            )
        }

        // ── Profile ──
        composable(StaffRoutes.PROFILE) {
            StaffProfileScreen(
                viewModel = profileViewModel,
                onNavigateToEditProfile = {
                    navController.navigate(StaffRoutes.EDIT_PROFILE)
                },
                onLogout = onLogout
            )
        }

        composable(StaffRoutes.EDIT_PROFILE) {
            EditProfileScreen(
                viewModel = profileViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
