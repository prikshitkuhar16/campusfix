package com.campusfix.app.features.dashboard.student

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AddCircleOutline
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
import com.campusfix.app.data.repository.StudentRepositoryImpl
import com.campusfix.app.features.dashboard.student.complaints.StudentComplaintViewModel
import com.campusfix.app.features.dashboard.student.profile.StudentProfileViewModel

// ── Bottom nav items ──

sealed class StudentBottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object RaiseComplaint : StudentBottomNavItem(
        route = StudentRoutes.RAISE_COMPLAINT,
        title = "Raise Complaint",
        icon = Icons.Default.AddCircleOutline
    )
    data object MyComplaints : StudentBottomNavItem(
        route = StudentRoutes.MY_COMPLAINTS,
        title = "My Complaints",
        icon = Icons.AutoMirrored.Filled.List
    )
    data object Profile : StudentBottomNavItem(
        route = StudentRoutes.PROFILE,
        title = "Profile",
        icon = Icons.Default.Person
    )
}

private val bottomNavItems = listOf(
    StudentBottomNavItem.RaiseComplaint,
    StudentBottomNavItem.MyComplaints,
    StudentBottomNavItem.Profile
)

@Composable
fun StudentDashboardScreen(
    firebaseAuthManager: FirebaseAuthManager,
    onLogout: () -> Unit
) {
    val nestedNavController = rememberNavController()

    val repository = remember { StudentRepositoryImpl(firebaseAuthManager) }

    val complaintViewModel = remember {
        StudentComplaintViewModel(repository)
    }
    val profileViewModel = remember {
        StudentProfileViewModel(repository, firebaseAuthManager)
    }

    val navBackStackEntry by nestedNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Show bottom bar only on the 3 main tabs
    val showBottomNavigation = currentRoute in listOf(
        StudentRoutes.RAISE_COMPLAINT,
        StudentRoutes.MY_COMPLAINTS,
        StudentRoutes.PROFILE
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
        StudentNavGraph(
            modifier = Modifier.padding(paddingValues),
            navController = nestedNavController,
            complaintViewModel = complaintViewModel,
            profileViewModel = profileViewModel,
            onLogout = onLogout
        )
    }
}

