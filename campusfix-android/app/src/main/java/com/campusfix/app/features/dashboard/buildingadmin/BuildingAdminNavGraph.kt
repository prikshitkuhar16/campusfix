package com.campusfix.app.features.dashboard.buildingadmin

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.campusfix.app.features.dashboard.buildingadmin.complaints.AssignStaffScreen
import com.campusfix.app.features.dashboard.buildingadmin.complaints.ComplaintDetailScreen
import com.campusfix.app.features.dashboard.buildingadmin.complaints.ComplaintsScreen
import com.campusfix.app.features.dashboard.buildingadmin.complaints.ComplaintsViewModel
import com.campusfix.app.features.dashboard.buildingadmin.profile.BuildingAdminProfileViewModel
import com.campusfix.app.features.dashboard.buildingadmin.profile.EditProfileScreen
import com.campusfix.app.features.dashboard.buildingadmin.profile.ProfileScreen
import com.campusfix.app.features.dashboard.buildingadmin.staff.InviteStaffScreen
import com.campusfix.app.features.dashboard.buildingadmin.staff.StaffDetailScreen
import com.campusfix.app.features.dashboard.buildingadmin.staff.StaffScreen
import com.campusfix.app.features.dashboard.buildingadmin.staff.StaffViewModel

// ── Route constants ──
object BuildingAdminRoutes {
    const val COMPLAINTS = "ba_complaints"
    const val COMPLAINT_DETAIL = "ba_complaints/{complaintId}"
    const val ASSIGN_STAFF = "ba_complaints/{complaintId}/assign?jobType={jobType}&isReassign={isReassign}"
    const val STAFF = "ba_staff"
    const val INVITE_STAFF = "ba_staff/invite"
    const val STAFF_DETAIL = "ba_staff/{staffId}"
    const val PROFILE = "ba_profile"
    const val EDIT_PROFILE = "ba_profile/edit"

    fun complaintDetail(id: String) = "ba_complaints/$id"
    fun assignStaff(complaintId: String, jobType: String? = null, isReassign: Boolean = false): String {
        val jt = jobType ?: ""
        return "ba_complaints/$complaintId/assign?jobType=$jt&isReassign=$isReassign"
    }
    fun staffDetail(id: String) = "ba_staff/$id"
}

@Composable
fun BuildingAdminNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    complaintsViewModel: ComplaintsViewModel,
    staffViewModel: StaffViewModel,
    profileViewModel: BuildingAdminProfileViewModel,
    onLogout: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = BuildingAdminRoutes.COMPLAINTS,
        modifier = modifier
    ) {
        // ── Complaints ──
        composable(BuildingAdminRoutes.COMPLAINTS) {
            ComplaintsScreen(
                viewModel = complaintsViewModel,
                onNavigateToComplaintDetail = { complaintId ->
                    navController.navigate(BuildingAdminRoutes.complaintDetail(complaintId))
                }
            )
        }

        composable(
            route = BuildingAdminRoutes.COMPLAINT_DETAIL,
            arguments = listOf(navArgument("complaintId") { type = NavType.StringType })
        ) { backStackEntry ->
            val complaintId = backStackEntry.arguments?.getString("complaintId") ?: ""
            ComplaintDetailScreen(
                viewModel = complaintsViewModel,
                complaintId = complaintId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAssignStaff = { id, jobType, isReassign ->
                    navController.navigate(BuildingAdminRoutes.assignStaff(id, jobType, isReassign))
                }
            )
        }

        composable(
            route = BuildingAdminRoutes.ASSIGN_STAFF,
            arguments = listOf(
                navArgument("complaintId") { type = NavType.StringType },
                navArgument("jobType") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                },
                navArgument("isReassign") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val complaintId = backStackEntry.arguments?.getString("complaintId") ?: ""
            val jobType = backStackEntry.arguments?.getString("jobType")?.ifBlank { null }
            val isReassign = backStackEntry.arguments?.getBoolean("isReassign") ?: false
            AssignStaffScreen(
                viewModel = complaintsViewModel,
                complaintId = complaintId,
                jobType = jobType,
                isReassign = isReassign,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ── Staff ──
        composable(BuildingAdminRoutes.STAFF) {
            StaffScreen(
                viewModel = staffViewModel,
                onNavigateToInviteStaff = {
                    navController.navigate(BuildingAdminRoutes.INVITE_STAFF)
                },
                onNavigateToStaffDetail = { staffId ->
                    navController.navigate(BuildingAdminRoutes.staffDetail(staffId))
                }
            )
        }

        composable(BuildingAdminRoutes.INVITE_STAFF) {
            InviteStaffScreen(
                viewModel = staffViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = BuildingAdminRoutes.STAFF_DETAIL,
            arguments = listOf(navArgument("staffId") { type = NavType.StringType })
        ) {
            StaffDetailScreen(
                viewModel = staffViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ── Profile ──
        composable(BuildingAdminRoutes.PROFILE) {
            ProfileScreen(
                viewModel = profileViewModel,
                onNavigateToEditProfile = {
                    navController.navigate(BuildingAdminRoutes.EDIT_PROFILE)
                },
                onLogout = onLogout
            )
        }

        composable(BuildingAdminRoutes.EDIT_PROFILE) {
            EditProfileScreen(
                viewModel = profileViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

