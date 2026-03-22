package com.campusfix.app.features.dashboard.buildingadmin

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.campusfix.app.core.firebase.FirebaseAuthManager
import com.campusfix.app.data.repository.BuildingAdminRepositoryImpl
import com.campusfix.app.features.dashboard.buildingadmin.complaints.ComplaintsViewModel
import com.campusfix.app.features.dashboard.buildingadmin.profile.BuildingAdminProfileViewModel
import com.campusfix.app.features.dashboard.buildingadmin.staff.StaffViewModel

// ── Bottom nav items ──

sealed class BABottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Complaints : BABottomNavItem(
        route = BuildingAdminRoutes.COMPLAINTS,
        title = "Complaints",
        icon = Icons.AutoMirrored.Filled.Assignment
    )
    data object Staff : BABottomNavItem(
        route = BuildingAdminRoutes.STAFF,
        title = "Staff",
        icon = Icons.Default.People
    )
    data object Profile : BABottomNavItem(
        route = BuildingAdminRoutes.PROFILE,
        title = "Profile",
        icon = Icons.Default.Person
    )
}

private val bottomNavItems = listOf(
    BABottomNavItem.Complaints,
    BABottomNavItem.Staff,
    BABottomNavItem.Profile
)

@Composable
fun BuildingAdminDashboardScreen(
    firebaseAuthManager: FirebaseAuthManager,
    onLogout: () -> Unit
) {
    val nestedNavController = rememberNavController()

    val repository = remember { BuildingAdminRepositoryImpl(firebaseAuthManager) }

    val complaintsViewModel = remember {
        ComplaintsViewModel(repository)
    }
    val staffViewModel = remember {
        StaffViewModel(repository)
    }
    val profileViewModel = remember {
        BuildingAdminProfileViewModel(repository, firebaseAuthManager)
    }

    val navBackStackEntry by nestedNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Show bottom bar only on the 3 main tabs
    val showBottomNavigation = currentRoute in listOf(
        BuildingAdminRoutes.COMPLAINTS,
        BuildingAdminRoutes.STAFF,
        BuildingAdminRoutes.PROFILE
    )

    Scaffold(
        bottomBar = {
            if (showBottomNavigation) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title
                                )
                            },
                            label = { Text(item.title) },
                            selected = currentRoute == item.route,
                            onClick = {
                                if (currentRoute != item.route) {
                                    nestedNavController.navigate(item.route) {
                                        popUpTo(nestedNavController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        BuildingAdminNavGraph(
            modifier = Modifier.padding(paddingValues),
            navController = nestedNavController,
            complaintsViewModel = complaintsViewModel,
            staffViewModel = staffViewModel,
            profileViewModel = profileViewModel,
            onLogout = onLogout
        )
    }
}



