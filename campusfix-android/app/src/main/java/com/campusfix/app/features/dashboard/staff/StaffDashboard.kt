package com.campusfix.app.features.dashboard.staff

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.History
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
import com.campusfix.app.data.repository.StaffRepositoryImpl
import com.campusfix.app.features.dashboard.staff.complaints.StaffComplaintsViewModel
import com.campusfix.app.features.dashboard.staff.profile.StaffProfileViewModel

// ── Bottom nav items ──

sealed class StaffBottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Assigned : StaffBottomNavItem(
        route = StaffRoutes.ASSIGNED,
        title = "Work",
        icon = Icons.AutoMirrored.Filled.Assignment
    )
    data object History : StaffBottomNavItem(
        route = StaffRoutes.HISTORY,
        title = "History",
        icon = Icons.Default.History
    )
    data object Profile : StaffBottomNavItem(
        route = StaffRoutes.PROFILE,
        title = "Profile",
        icon = Icons.Default.Person
    )
}

private val bottomNavItems = listOf(
    StaffBottomNavItem.Assigned,
    StaffBottomNavItem.History,
    StaffBottomNavItem.Profile
)

@Composable
fun StaffDashboardScreen(
    firebaseAuthManager: FirebaseAuthManager,
    onLogout: () -> Unit
) {
    val nestedNavController = rememberNavController()

    val repository = remember { StaffRepositoryImpl(firebaseAuthManager) }

    val complaintsViewModel = remember {
        StaffComplaintsViewModel(repository)
    }
    val profileViewModel = remember {
        StaffProfileViewModel(repository, firebaseAuthManager)
    }

    val navBackStackEntry by nestedNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Show bottom bar only on the 3 main tabs
    val showBottomNavigation = currentRoute in listOf(
        StaffRoutes.ASSIGNED,
        StaffRoutes.HISTORY,
        StaffRoutes.PROFILE
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
        StaffNavGraph(
            modifier = Modifier.padding(paddingValues),
            navController = nestedNavController,
            complaintsViewModel = complaintsViewModel,
            profileViewModel = profileViewModel,
            onLogout = onLogout
        )
    }
}

