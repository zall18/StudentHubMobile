package com.example.studenthub.ui

import StudentHubViewModel
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

// ── Data class untuk Bottom Nav ──────────────────────────────────────
data class BottomNavItem(
    val title: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(
        title = "Dashboard",
        route = "dashboard",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    BottomNavItem(
        title = "Tugas",
        route = "tasks",
        selectedIcon = Icons.Filled.CheckCircle,
        unselectedIcon = Icons.Outlined.CheckCircle
    ),
    BottomNavItem(
        title = "Kuliah",
        route = "subjects",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    ),
    BottomNavItem(
        title = "UKM",
        route = "clubs",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    ),
    BottomNavItem(
        title = "More",
        route = "more",
        selectedIcon = Icons.Filled.MoreVert,
        unselectedIcon = Icons.Outlined.MoreVert
    )
)

// ── Data class untuk More Screen items ───────────────────────────────
data class MoreFeatureItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val gradientColors: List<Color>,
    val route: String
)

// ── Main Screen ──────────────────────────────────────────────────────
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

// ── Bottom Navigation (Premium) ──────────────────────────────────────
@Composable
fun StudentHubBottomNav(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        modifier = Modifier
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            )
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
    ) {
        bottomNavItems.forEach { item ->
            val isSelected = currentRoute == item.route

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.title,
                        modifier = Modifier.size(if (isSelected) 26.dp else 24.dp)
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                )
            )
        }
    }
}

// ── More Screen (Premium) ────────────────────────────────────────────
@Composable
fun MoreScreen(navController: NavHostController) {
    val features = listOf(
        MoreFeatureItem(
            title = "Learning Resources",
            subtitle = "Simpan referensi belajar & catatan penting",
            icon = Icons.Rounded.MenuBook,
            gradientColors = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)),
            route = "learning"
        ),
        MoreFeatureItem(
            title = "Achievements",
            subtitle = "Track sertifikat, award & pencapaian",
            icon = Icons.Rounded.EmojiEvents,
            gradientColors = listOf(Color(0xFFF59E0B), Color(0xFFF97316)),
            route = "achievements"
        ),
        MoreFeatureItem(
            title = "Goals Tracker",
            subtitle = "Kelola target, milestone & progress",
            icon = Icons.Rounded.Flag,
            gradientColors = listOf(Color(0xFF10B981), Color(0xFF059669)),
            route = "goals"
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header section
        Column(
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text(
                text = "Fitur Lainnya",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Explore lebih banyak tools untuk mendukung perkuliahanmu",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        // Feature cards
        features.forEach { feature ->
            MoreFeatureCard(
                item = feature,
                onClick = { navController.navigate(feature.route) }
            )
        }
    }
}

// ── Premium Feature Card ─────────────────────────────────────────────
@Composable
fun MoreFeatureCard(
    item: MoreFeatureItem,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "cardScale"
    )

    val cardElevation by animateFloatAsState(
        targetValue = if (isPressed) 2f else 6f,
        label = "cardElevation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(
                elevation = cardElevation.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = item.gradientColors[0].copy(alpha = 0.2f),
                spotColor = item.gradientColors[0].copy(alpha = 0.3f)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Gradient icon container
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        brush = Brush.linearGradient(item.gradientColors),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.3).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    lineHeight = 18.sp
                )
            }

            // Chevron arrow
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,

                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}