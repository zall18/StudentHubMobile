package com.example.studenthub.ui

import StudentHubViewModel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Home

import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.studenthub.ui.screens.AchievementsScreen
import com.example.studenthub.ui.screens.ClubsScreen
import com.example.studenthub.ui.screens.EnhancedDashboardScreen
import com.example.studenthub.ui.screens.GoalsScreen
import com.example.studenthub.ui.screens.LearningResourcesScreen
import com.example.studenthub.ui.screens.SubjectsScreen
import com.example.studenthub.ui.screens.TasksListScreen

 data class BottomNavItem(
    val title: String,
    val route: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(title = "Dashboard", route = "dashboard", icon = Icons.Default.Home),
    BottomNavItem(title = "Tugas", route = "tasks", icon = Icons.Default.CheckCircle),
    BottomNavItem(title = "Kuliah", route = "subjects", icon = Icons.Default.Person),
    BottomNavItem(title = "UKM", route = "clubs", icon = Icons.Default.Person),
    BottomNavItem(title = "More", route = "more", icon = Icons.Default.MoreVert)
)

@Composable
fun MainScreen(viewModel: StudentHubViewModel) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { StudentHubBottomNav(navController = navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("dashboard") { EnhancedDashboardScreen(viewModel = viewModel) }
            composable("tasks") { TasksListScreen(viewModel = viewModel) }
            composable("subjects") { SubjectsScreen(viewModel = viewModel) }
            composable("clubs") { ClubsScreen(viewModel = viewModel) }
            composable("more") { MoreScreen(navController = navController) }
            composable("learning") { LearningResourcesScreen(viewModel = viewModel) }
            composable("achievements") { AchievementsScreen(viewModel = viewModel) }
            composable("goals") { GoalsScreen(viewModel = viewModel) }
        }
    }
}

@Composable
fun StudentHubBottomNav(navController: NavHostController) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        bottomNavItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) }
            )
        }
    }
}

@Composable
fun MoreScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "Fitur Lainnya", style = MaterialTheme.typography.headlineMedium)
        MoreActionCard(title = "Learning Resources", subtitle = "Simpan referensi belajar", onClick = { navController.navigate("learning") })
        MoreActionCard(title = "Achievements", subtitle = "Track sertifikat & award", onClick = { navController.navigate("achievements") })
        MoreActionCard(title = "Goals Tracker", subtitle = "Kelola target & milestone", onClick = { navController.navigate("goals") })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreActionCard(title: String, subtitle: String, onClick: () -> Unit) {
    Card(onClick = onClick, colors = CardDefaults.cardColors()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium)
        }
    }
}


