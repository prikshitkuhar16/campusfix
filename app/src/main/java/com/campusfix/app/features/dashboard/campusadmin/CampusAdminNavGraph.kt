package com.campusfix.app.features.dashboard.campusadmin

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.campusfix.app.features.dashboard.campusadmin.buildings.AddBuildingScreen
import com.campusfix.app.features.dashboard.campusadmin.buildings.BuildingDetailScreen
import com.campusfix.app.features.dashboard.campusadmin.buildings.CampusAdminBuildingsScreen
import com.campusfix.app.features.dashboard.campusadmin.buildings.CampusAdminBuildingsViewModel
import com.campusfix.app.features.dashboard.campusadmin.profile.CampusAdminProfileScreen
import com.campusfix.app.features.dashboard.campusadmin.profile.CampusAdminProfileViewModel
import com.campusfix.app.features.dashboard.campusadmin.profile.EditProfileScreen
import com.campusfix.app.features.dashboard.campusadmin.users.CampusAdminUsersScreen
import com.campusfix.app.features.dashboard.campusadmin.users.CampusAdminUsersViewModel
import com.campusfix.app.features.dashboard.campusadmin.users.InviteUserScreen
import com.campusfix.app.features.dashboard.campusadmin.users.UserDetailScreen

// ── Route constants ──
object CampusAdminRoutes {
    const val BUILDINGS = "ca_buildings"
    const val ADD_BUILDING = "ca_buildings/add"
    const val BUILDING_DETAIL = "ca_buildings/{buildingId}"
    const val USERS = "ca_users"
    const val INVITE_USER = "ca_users/invite"
    const val USER_DETAIL = "ca_users/{userId}"
    const val PROFILE = "ca_profile"
    const val EDIT_PROFILE = "ca_profile/edit"

    fun buildingDetail(id: String) = "ca_buildings/$id"
    fun userDetail(id: String) = "ca_users/$id"
}

@Composable
fun CampusAdminNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    buildingsViewModel: CampusAdminBuildingsViewModel,
    usersViewModel: CampusAdminUsersViewModel,
    profileViewModel: CampusAdminProfileViewModel,
    onLogout: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = CampusAdminRoutes.BUILDINGS,
        modifier = modifier
    ) {
        // ── Buildings ──
        composable(CampusAdminRoutes.BUILDINGS) {
            CampusAdminBuildingsScreen(
                viewModel = buildingsViewModel,
                onNavigateToAddBuilding = {
                    navController.navigate(CampusAdminRoutes.ADD_BUILDING)
                },
                onNavigateToBuildingDetail = { buildingId ->
                    navController.navigate(CampusAdminRoutes.buildingDetail(buildingId))
                }
            )
        }

        composable(CampusAdminRoutes.ADD_BUILDING) {
            AddBuildingScreen(
                viewModel = buildingsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = CampusAdminRoutes.BUILDING_DETAIL,
            arguments = listOf(navArgument("buildingId") { type = NavType.StringType })
        ) { backStackEntry ->
            val buildingId = backStackEntry.arguments?.getString("buildingId") ?: ""
            BuildingDetailScreen(
                viewModel = buildingsViewModel,
                buildingId = buildingId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ── Users ──
        composable(CampusAdminRoutes.USERS) {
            CampusAdminUsersScreen(
                viewModel = usersViewModel,
                onNavigateToInviteUser = {
                    navController.navigate(CampusAdminRoutes.INVITE_USER)
                },
                onNavigateToUserDetail = { userId ->
                    navController.navigate(CampusAdminRoutes.userDetail(userId))
                }
            )
        }

        composable(CampusAdminRoutes.INVITE_USER) {
            InviteUserScreen(
                viewModel = usersViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = CampusAdminRoutes.USER_DETAIL,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) {
            UserDetailScreen(
                viewModel = usersViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ── Profile ──
        composable(CampusAdminRoutes.PROFILE) {
            CampusAdminProfileScreen(
                viewModel = profileViewModel,
                onNavigateToEditProfile = {
                    navController.navigate(CampusAdminRoutes.EDIT_PROFILE)
                },
                onLogout = onLogout
            )
        }

        composable(CampusAdminRoutes.EDIT_PROFILE) {
            EditProfileScreen(
                viewModel = profileViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

