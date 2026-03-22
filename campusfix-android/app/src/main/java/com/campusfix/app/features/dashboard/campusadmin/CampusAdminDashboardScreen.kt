package com.campusfix.app.features.dashboard.campusadmin

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
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
import com.campusfix.app.data.repository.CampusAdminRepositoryImpl
import com.campusfix.app.features.dashboard.campusadmin.buildings.CampusAdminBuildingsViewModel
import com.campusfix.app.features.dashboard.campusadmin.profile.CampusAdminProfileViewModel
import com.campusfix.app.features.dashboard.campusadmin.users.CampusAdminUsersViewModel

// ── Bottom nav items ──

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Buildings : BottomNavItem(
        route = CampusAdminRoutes.BUILDINGS,
        title = "Buildings",
        icon = Icons.Default.Business
    )
    data object Users : BottomNavItem(
        route = CampusAdminRoutes.USERS,
        title = "Users",
        icon = Icons.Default.People
    )
    data object Profile : BottomNavItem(
        route = CampusAdminRoutes.PROFILE,
        title = "Profile",
        icon = Icons.Default.Person
    )
}

private val bottomNavItems = listOf(
    BottomNavItem.Buildings,
    BottomNavItem.Users,
    BottomNavItem.Profile
)

@Composable
fun CampusAdminDashboardScreen(
    firebaseAuthManager: FirebaseAuthManager,
    onLogout: () -> Unit
) {
    val nestedNavController = rememberNavController()

    val repository = remember { CampusAdminRepositoryImpl(firebaseAuthManager) }

    val buildingsViewModel = remember {
        CampusAdminBuildingsViewModel(repository)
    }
    val usersViewModel = remember {
        CampusAdminUsersViewModel(repository)
    }
    val profileViewModel = remember {
        CampusAdminProfileViewModel(repository, firebaseAuthManager)
    }

    val navBackStackEntry by nestedNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Show bottom bar only on the 3 main tabs
    val showBottomNavigation = currentRoute in listOf(
        CampusAdminRoutes.BUILDINGS,
        CampusAdminRoutes.USERS,
        CampusAdminRoutes.PROFILE
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
        CampusAdminNavGraph(
            modifier = Modifier.padding(paddingValues),
            navController = nestedNavController,
            buildingsViewModel = buildingsViewModel,
            usersViewModel = usersViewModel,
            profileViewModel = profileViewModel,
            onLogout = onLogout
        )
    }
}
