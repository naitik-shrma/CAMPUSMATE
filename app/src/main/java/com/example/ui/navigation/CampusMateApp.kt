package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AdminPortalScreen
import com.example.ui.screens.AssignmentsScreen
import com.example.ui.screens.AttendanceScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.NotesScreen
import com.example.ui.screens.ScienceHubScreen
import com.example.ui.screens.SubjectsScreen
import com.example.ui.viewmodel.CollegeViewModel

enum class Screen(val route: String, val title: String, val icon: ImageVector) {
    DASHBOARD("dashboard", "Home", Icons.Default.Dashboard),
    ATTENDANCE("attendance", "Attendance", Icons.Default.CalendarMonth),
    NOTES("notes", "Notes", Icons.Default.Description),
    ASSIGNMENTS("assignments", "Tasks", Icons.Default.Assignment),
    SCIENCE("science", "Science", Icons.Default.Science),
    SUBJECTS("subjects", "Subjects", Icons.Default.Dashboard),
    ADMIN("admin", "Admin", Icons.Default.Dashboard)
}

@Composable
fun CampusMateApp(
    viewModel: CollegeViewModel = viewModel()
) {
    var currentScreen by remember { mutableStateOf(Screen.DASHBOARD) }

    // Subscreen Back Handling
    BackHandler(enabled = currentScreen == Screen.ADMIN || currentScreen == Screen.SUBJECTS) {
        currentScreen = Screen.DASHBOARD
    }

    val isSubscreen = currentScreen == Screen.ADMIN || currentScreen == Screen.SUBJECTS

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (!isSubscreen) {
                NavigationBar(
                    modifier = Modifier.testTag("main_navigation_bar"),
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    val navItems = listOf(
                        Screen.DASHBOARD,
                        Screen.ATTENDANCE,
                        Screen.NOTES,
                        Screen.ASSIGNMENTS,
                        Screen.SCIENCE
                    )

                    navItems.forEach { screen ->
                        val selected = currentScreen == screen
                        NavigationBarItem(
                            selected = selected,
                            onClick = { currentScreen = screen },
                            icon = {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.title
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontSize = 11.sp
                                )
                            },
                            modifier = Modifier.testTag("nav_item_${screen.route}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                Screen.DASHBOARD -> DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToAttendance = { currentScreen = Screen.ATTENDANCE },
                    onNavigateToNotes = { currentScreen = Screen.NOTES },
                    onNavigateToAssignments = { currentScreen = Screen.ASSIGNMENTS },
                    onNavigateToScience = { currentScreen = Screen.SCIENCE },
                    onNavigateToAdmin = { currentScreen = Screen.ADMIN }
                )
                Screen.ATTENDANCE -> AttendanceScreen(
                    viewModel = viewModel,
                    onManageSubjects = { currentScreen = Screen.SUBJECTS }
                )
                Screen.NOTES -> NotesScreen(
                    viewModel = viewModel
                )
                Screen.ASSIGNMENTS -> AssignmentsScreen(
                    viewModel = viewModel
                )
                Screen.SCIENCE -> ScienceHubScreen(
                    viewModel = viewModel
                )
                Screen.SUBJECTS -> SubjectsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { currentScreen = Screen.ATTENDANCE }
                )
                Screen.ADMIN -> AdminPortalScreen(
                    viewModel = viewModel,
                    onNavigateBack = { currentScreen = Screen.DASHBOARD },
                    onNavigateToSubjects = { currentScreen = Screen.SUBJECTS },
                    onNavigateToNotes = { currentScreen = Screen.NOTES }
                )
            }
        }
    }
}
