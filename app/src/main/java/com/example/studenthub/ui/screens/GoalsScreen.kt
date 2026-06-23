package com.example.studenthub.ui.screens

import StudentHubViewModel
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.studenthub.data.Goal
import java.text.SimpleDateFormat
import java.util.*

// ── Goal-specific colors ─────────────────────────────────────────────
private object GoalColors {
    val emerald = Color(0xFF059669)
    val emeraldDark = Color(0xFF047857)
    val emeraldLight = Color(0xFFECFDF5)
    val green = Color(0xFF10B981)

    val gradientHeader = listOf(Color(0xFF059669), Color(0xFF10B981), Color(0xFF34D399))
    val fabGradient = listOf(Color(0xFF059669), Color(0xFF10B981))

    val deleteRed = Color(0xFFDC2626)
    val deleteRedBg = Color(0xFFFEF2F2)

    val priorityHigh = Color(0xFFDC2626)
    val priorityHighBg = Color(0xFFFEF2F2)
    val priorityMed = Color(0xFFD97706)
    val priorityMedBg = Color(0xFFFFFBEB)
    val priorityLow = Color(0xFF059669)
    val priorityLowBg = Color(0xFFECFDF5)

    val progressHigh = Color(0xFF10B981)
    val progressMed = Color(0xFFF59E0B)
    val progressLow = Color(0xFF3B82F6)
    val progressNone = Color(0xFFE5E7EB)

    fun categoryColor(category: String): Color = when (category) {
        "ACADEMIC" -> Color(0xFF2563EB)
        "PERSONAL" -> Color(0xFF7C3AED)
        "PROFESSIONAL" -> Color(0xFFEA580C)
        else -> Color(0xFF64748B)
    }

    fun categoryIcon(category: String): ImageVector = when (category) {
        "ACADEMIC" -> Icons.Rounded.School
        "PERSONAL" -> Icons.Rounded.Person
        "PROFESSIONAL" -> Icons.Rounded.Work
        else -> Icons.Rounded.Flag
    }

    fun categoryLabel(category: String): String = when (category) {
        "ACADEMIC" -> "Akademik"
        "PERSONAL" -> "Personal"
        "PROFESSIONAL" -> "Profesional"
        else -> category
    }

    fun statusColor(status: String): Color = when (status) {
        "COMPLETED" -> Color(0xFF059669)
        "IN_PROGRESS" -> Color(0xFF2563EB)
        "ABANDONED" -> Color(0xFF64748B)
        else -> Color(0xFF64748B)
    }

    fun statusBg(status: String): Color = when (status) {
        "COMPLETED" -> Color(0xFFD1FAE5)
        "IN_PROGRESS" -> Color(0xFFDBEAFE)
        "ABANDONED" -> Color(0xFFF3F4F6)
        else -> Color(0xFFF3F4F6)
    }

    fun statusLabel(status: String): String = when (status) {
        "COMPLETED" -> "Selesai"
        "IN_PROGRESS" -> "Dalam Proses"
        "ABANDONED" -> "Dihentikan"
        else -> status
    }

    fun statusIcon(status: String): ImageVector = when (status) {
        "COMPLETED" -> Icons.Rounded.CheckCircle
        "IN_PROGRESS" -> Icons.Rounded.TrendingUp
        "ABANDONED" -> Icons.Rounded.PauseCircle
        else -> Icons.Rounded.Flag
    }
}

// ══════════════════════════════════════════════════════════════════════
// ── MAIN GOALS SCREEN ────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(viewModel: StudentHubViewModel) {
    val allGoals by viewModel.allGoals.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf("IN_PROGRESS") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showMilestoneDialog by remember { mutableStateOf(false) }
    var selectedGoalForMilestone by remember { mutableStateOf<Goal?>(null) }

    // Form fields
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("ACADEMIC") }
    var priority by remember { mutableStateOf(2) }
    var milestoneTitle by remember { mutableStateOf("") }

    fun resetFormFields() {
        title = ""; description = ""; category = "ACADEMIC"; priority = 2
    }

    val tabs = listOf(
        "IN_PROGRESS" to "Aktif",
        "COMPLETED" to "Selesai",
        "ABANDONED" to "Dihentikan",
        "ALL" to "Semua"
    )

    val visibleGoals = remember(allGoals, selectedTab) {
        val filtered = when (selectedTab) {
            "ALL" -> allGoals
            else -> allGoals.filter { it.status == selectedTab }
        }
        filtered.sortedByDescending { it.priority }
    }

    val activeGoalsCount = remember(allGoals) { allGoals.count { it.status == "IN_PROGRESS" } }
    val completedGoalsCount = remember(allGoals) { allGoals.count { it.status == "COMPLETED" } }
    val averageProgress = remember(allGoals) {
        val active = allGoals.filter { it.status == "IN_PROGRESS" }
        if (active.isNotEmpty()) active.map { it.progress }.average().toInt() else 0
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = { GoalGradientFab(onClick = { resetFormFields(); showAddDialog = true }) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // ── Header ───────────────────────────────────────────
            item { GoalScreenHeader(active = activeGoalsCount, completed = completedGoalsCount) }

            // ── Stat Cards ───────────────────────────────────────
            item {
                GoalStatRow(
                    active = activeGoalsCount,
                    completed = completedGoalsCount,
                    avgProgress = averageProgress
                )
            }

            // ── Tab Filters ──────────────────────────────────────
            item {
                GoalTabFilter(
                    tabs = tabs,
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            }

            // ── Result count ─────────────────────────────────────
            if (visibleGoals.isNotEmpty()) {
                item {
                    Text(
                        text = "${visibleGoals.size} goal ditemukan",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            }

            // ── Goals List ───────────────────────────────────────
            if (visibleGoals.isEmpty()) {
                item { PremiumEmptyGoalState(selectedTab) }
            } else {
                items(visibleGoals, key = { it.id }) { goal ->
                    PremiumGoalCard(
                        goal = goal,
                        onAddMilestone = {
                            selectedGoalForMilestone = goal
                            showMilestoneDialog = true
                        },
                        onUpdateStatus = { newStatus -> viewModel.updateGoalStatus(goal.id, newStatus) },
                        onDelete = { viewModel.deleteGoal(goal.id) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    // ── Add Goal Dialog ──────────────────────────────────────────
    if (showAddDialog) {
        PremiumAddGoalDialog(
            title = title, onTitleChange = { title = it },
            description = description, onDescriptionChange = { description = it },
            category = category, onCategoryChange = { category = it },
            priority = priority, onPriorityChange = { priority = it },
            onDismiss = { showAddDialog = false; resetFormFields() },
            onConfirm = {
                if (title.isNotBlank()) {
                    viewModel.addGoal(
                        title = title, description = description,
                        category = category,
                        targetDate = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000),
                        priority = priority
                    )
                    showAddDialog = false; resetFormFields()
                }
            }
        )
    }

    // ── Milestone Dialog ─────────────────────────────────────────
    if (showMilestoneDialog && selectedGoalForMilestone != null) {
        PremiumMilestoneDialog(
            title = milestoneTitle,
            onTitleChange = { milestoneTitle = it },
            onDismiss = {
                showMilestoneDialog = false; milestoneTitle = ""
                selectedGoalForMilestone = null
            },
            onConfirm = {
                val goal = selectedGoalForMilestone
                if (goal != null && milestoneTitle.isNotBlank()) {
                    viewModel.addGoalMilestone(
                        goalId = goal.id, title = milestoneTitle,
                        targetDate = System.currentTimeMillis()
                    )
                    showMilestoneDialog = false; milestoneTitle = ""
                    selectedGoalForMilestone = null
                }
            }
        )
    }
}

// ══════════════════════════════════════════════════════════════════════
// ── HEADER ───────────────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════
@Composable
private fun GoalScreenHeader(active: Int, completed: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Goals Tracker",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(shape = RoundedCornerShape(6.dp), color = GoalColors.emeraldLight) {
                    Text(
                        text = "$active aktif",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                        color = GoalColors.emerald
                    )
                }
                Text("•", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = "$completed selesai",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(
                    brush = Brush.linearGradient(GoalColors.gradientHeader),
                    shape = RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.Flag, null, tint = Color.White, modifier = Modifier.size(22.dp))
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
// ── GRADIENT FAB ─────────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════
@Composable
private fun GoalGradientFab(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick, shape = CircleShape,
        containerColor = Color.Transparent, contentColor = Color.White,
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp, pressedElevation = 12.dp),
        modifier = Modifier
            .shadow(10.dp, CircleShape, ambientColor = GoalColors.emerald.copy(alpha = 0.3f), spotColor = GoalColors.emerald.copy(alpha = 0.4f))
            .background(brush = Brush.linearGradient(GoalColors.fabGradient), shape = CircleShape)
    ) {
        Icon(Icons.Rounded.Add, "Tambah Goal", Modifier.size(28.dp))
    }
}

// ══════════════════════════════════════════════════════════════════════
// ── STAT ROW ─────────────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════
@Composable
private fun GoalStatRow(active: Int, completed: Int, avgProgress: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        GoalStatCard(
            icon = Icons.Rounded.Flag,
            value = active.toString(),
            label = "Aktif",
            gradientColors = listOf(Color(0xFF059669), Color(0xFF10B981)),
            modifier = Modifier.weight(1f)
        )
        GoalStatCard(
            icon = Icons.Rounded.CheckCircle,
            value = completed.toString(),
            label = "Selesai",
            gradientColors = listOf(Color(0xFF2563EB), Color(0xFF3B82F6)),
            modifier = Modifier.weight(1f)
        )
        GoalStatCard(
            icon = Icons.Rounded.TrendingUp,
            value = "$avgProgress%",
            label = "Progress",
            gradientColors = listOf(Color(0xFF7C3AED), Color(0xFF8B5CF6)),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun GoalStatCard(
    icon: ImageVector, value: String, label: String,
    gradientColors: List<Color>, modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.shadow(
            elevation = 6.dp, shape = RoundedCornerShape(18.dp),
            ambientColor = gradientColors.first().copy(alpha = 0.1f),
            spotColor = gradientColors.first().copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier.size(36.dp).background(
                    brush = Brush.linearGradient(gradientColors),
                    shape = RoundedCornerShape(10.dp)
                ),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, Modifier.size(18.dp), tint = Color.White)
            }
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface, letterSpacing = (-0.5).sp)
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
// ── TAB FILTER ───────────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════
@Composable
private fun GoalTabFilter(
    tabs: List<Pair<String, String>>,
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier.size(28.dp)
                    .background(GoalColors.emerald.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.FilterList, null, Modifier.size(16.dp), tint = GoalColors.emerald)
            }
            Text("Status", fontSize = 16.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground, letterSpacing = (-0.3).sp)
        }

        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabs.forEach { (key, label) ->
                val isSelected = selectedTab == key
                val tabColor = when (key) {
                    "IN_PROGRESS" -> GoalColors.emerald
                    "COMPLETED" -> Color(0xFF2563EB)
                    "ABANDONED" -> Color(0xFF64748B)
                    else -> Color(0xFF7C3AED)
                }
                val tabIcon = when (key) {
                    "IN_PROGRESS" -> Icons.Rounded.TrendingUp
                    "COMPLETED" -> Icons.Rounded.CheckCircle
                    "ABANDONED" -> Icons.Rounded.PauseCircle
                    else -> Icons.Rounded.ViewList
                }

                Surface(
                    onClick = { onTabSelected(key) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) tabColor else MaterialTheme.colorScheme.surface,
                    border = if (!isSelected) {
                        androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    } else null,
                    shadowElevation = if (isSelected) 2.dp else 0.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(tabIcon, null, Modifier.size(16.dp),
                            tint = if (isSelected) Color.White
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        Text(label, fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
// ── EMPTY STATE ──────────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════
@Composable
private fun PremiumEmptyGoalState(selectedTab: String) {
    Card(
        modifier = Modifier.fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp), ambientColor = GoalColors.emerald.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier.size(72.dp)
                    .background(GoalColors.emerald.copy(alpha = 0.08f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    GoalColors.statusIcon(selectedTab), null,
                    Modifier.size(36.dp), tint = GoalColors.emerald.copy(alpha = 0.4f)
                )
            }
            Text(
                text = when (selectedTab) {
                    "IN_PROGRESS" -> "Belum ada goal aktif"
                    "COMPLETED" -> "Belum ada goal selesai"
                    "ABANDONED" -> "Tidak ada goal dihentikan"
                    else -> "Belum ada goal"
                },
                fontSize = 17.sp, fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Buat goal baru untuk mulai\nmelacak progresmu",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                lineHeight = 19.sp, textAlign = TextAlign.Center
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
// ── PREMIUM GOAL CARD ────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════
@Composable
fun PremiumGoalCard(
    goal: Goal,
    onAddMilestone: () -> Unit,
    onUpdateStatus: (String) -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
    val targetText = dateFormat.format(Date(goal.targetDate))
    val daysRemaining = ((goal.targetDate - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)).toInt()

    val catColor = GoalColors.categoryColor(goal.category)
    val catIcon = GoalColors.categoryIcon(goal.category)
    val catLabel = GoalColors.categoryLabel(goal.category)

    val statusColor = GoalColors.statusColor(goal.status)
    val statusBg = GoalColors.statusBg(goal.status)
    val statusLabel = GoalColors.statusLabel(goal.status)
    val statusIcon = GoalColors.statusIcon(goal.status)

    val progressColor = when {
        goal.progress >= 80 -> GoalColors.progressHigh
        goal.progress >= 40 -> GoalColors.progressMed
        goal.progress > 0 -> GoalColors.progressLow
        else -> GoalColors.progressNone
    }

    val isCompleted = goal.status == "COMPLETED"
    val isOverdue = goal.status == "IN_PROGRESS" && daysRemaining <= 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isCompleted) 3.dp else 4.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = if (isCompleted) GoalColors.emerald.copy(alpha = 0.08f)
                else catColor.copy(alpha = 0.06f),
                spotColor = if (isCompleted) GoalColors.emerald.copy(alpha = 0.12f)
                else catColor.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) GoalColors.emeraldLight
            else MaterialTheme.colorScheme.surface
        ),
        border = if (isCompleted) {
            androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD1FAE5))
        } else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                // Gradient category icon
                Box(
                    modifier = Modifier.size(52.dp)
                        .background(
                            brush = Brush.linearGradient(listOf(catColor, catColor.copy(alpha = 0.7f))),
                            shape = RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(catIcon, null, Modifier.size(26.dp), tint = Color.White)
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    // Title + Priority
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = goal.title,
                            fontSize = 16.sp, fontWeight = FontWeight.Bold,
                            color = if (isCompleted) GoalColors.emerald
                            else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                            maxLines = 2, overflow = TextOverflow.Ellipsis,
                            lineHeight = 21.sp, letterSpacing = (-0.2).sp
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Priority badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when (goal.priority) {
                                3 -> GoalColors.priorityHighBg
                                2 -> GoalColors.priorityMedBg
                                else -> GoalColors.priorityLowBg
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    when (goal.priority) {
                                        3 -> Icons.Rounded.KeyboardDoubleArrowUp
                                        2 -> Icons.Rounded.DragHandle
                                        else -> Icons.Rounded.KeyboardArrowDown
                                    },
                                    null, Modifier.size(12.dp),
                                    tint = when (goal.priority) {
                                        3 -> GoalColors.priorityHigh
                                        2 -> GoalColors.priorityMed
                                        else -> GoalColors.priorityLow
                                    }
                                )
                                Text(
                                    text = when (goal.priority) {
                                        3 -> "Tinggi"; 2 -> "Sedang"; else -> "Rendah"
                                    },
                                    fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                    color = when (goal.priority) {
                                        3 -> GoalColors.priorityHigh
                                        2 -> GoalColors.priorityMed
                                        else -> GoalColors.priorityLow
                                    }
                                )
                            }
                        }
                    }

                    // Description
                    if (goal.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = goal.description, fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                            maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tags row
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category
                Surface(shape = RoundedCornerShape(8.dp), color = catColor.copy(alpha = 0.1f)) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(catIcon, null, Modifier.size(12.dp), tint = catColor)
                        Text(catLabel, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = catColor)
                    }
                }

                // Status
                Surface(shape = RoundedCornerShape(8.dp), color = statusBg) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(statusIcon, null, Modifier.size(12.dp), tint = statusColor)
                        Text(statusLabel, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = statusColor)
                    }
                }

                // Target date
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isOverdue) GoalColors.deleteRedBg
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Rounded.CalendarToday, null, Modifier.size(12.dp),
                            tint = if (isOverdue) GoalColors.deleteRed
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = targetText, fontSize = 11.sp, fontWeight = FontWeight.Medium,
                            color = if (isOverdue) GoalColors.deleteRed
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Progress Section ─────────────────────────────────
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Analytics, null, Modifier.size(16.dp),
                                tint = progressColor
                            )
                            Text("Progress", fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface)
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = progressColor.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "${goal.progress.toInt()}%",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                fontSize = 13.sp, fontWeight = FontWeight.Bold,
                                color = progressColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { (goal.progress.coerceIn(0f, 100f)) / 100f },
                        modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                        color = progressColor,
                        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                    )

                    // Days remaining
                    if (goal.status == "IN_PROGRESS") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Timer, null, Modifier.size(14.dp),
                                tint = when {
                                    isOverdue -> GoalColors.deleteRed
                                    daysRemaining <= 7 -> GoalColors.priorityMed
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                }
                            )
                            Text(
                                text = if (isOverdue) "Sudah melewati target!"
                                else if (daysRemaining <= 7) "$daysRemaining hari tersisa ⚡"
                                else "$daysRemaining hari tersisa",
                                fontSize = 12.sp, fontWeight = FontWeight.Medium,
                                color = when {
                                    isOverdue -> GoalColors.deleteRed
                                    daysRemaining <= 7 -> GoalColors.priorityMed
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), thickness = 1.dp)

            Spacer(modifier = Modifier.height(10.dp))

            // ── Action Buttons ───────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Milestone
                if (goal.status == "IN_PROGRESS") {
                    Surface(
                        onClick = onAddMilestone,
                        shape = RoundedCornerShape(10.dp),
                        color = GoalColors.emeraldLight
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(Icons.Rounded.Add, null, Modifier.size(15.dp), tint = GoalColors.emerald)
                            Text("Milestone", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = GoalColors.emerald)
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                // Right: Complete + Delete
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (goal.status == "IN_PROGRESS") {
                        Surface(
                            onClick = { onUpdateStatus("COMPLETED") },
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFD1FAE5)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(Icons.Rounded.CheckCircle, null, Modifier.size(15.dp), tint = GoalColors.emerald)
                                Text("Selesai", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = GoalColors.emerald)
                            }
                        }
                    }

                    Surface(
                        onClick = { showDeleteConfirmation = true },
                        shape = RoundedCornerShape(10.dp),
                        color = GoalColors.deleteRedBg
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(Icons.Rounded.DeleteOutline, null, Modifier.size(15.dp), tint = GoalColors.deleteRed)
                            Text("Hapus", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = GoalColors.deleteRed)
                        }
                    }
                }
            }
        }
    }

    // Delete dialog
    if (showDeleteConfirmation) {
        GoalDeleteDialog(
            goalTitle = goal.title,
            onConfirm = { onDelete(); showDeleteConfirmation = false },
            onDismiss = { showDeleteConfirmation = false }
        )
    }
}

// ══════════════════════════════════════════════════════════════════════
// ── DELETE DIALOG ────────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════
@Composable
private fun GoalDeleteDialog(goalTitle: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        icon = {
            Box(Modifier.size(52.dp).background(GoalColors.deleteRedBg, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.DeleteOutline, null, Modifier.size(26.dp), tint = GoalColors.deleteRed)
            }
        },
        title = {
            Text("Hapus Goal?", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        },
        text = {
            Text(
                "\"$goalTitle\" beserta semua milestone akan dihapus secara permanen.",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), lineHeight = 20.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = GoalColors.deleteRed),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Icon(Icons.Rounded.DeleteOutline, null, Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Hapus", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
            }
        }
    )
}

// ══════════════════════════════════════════════════════════════════════
// ── ADD GOAL DIALOG ──────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumAddGoalDialog(
    title: String, onTitleChange: (String) -> Unit,
    description: String, onDescriptionChange: (String) -> Unit,
    category: String, onCategoryChange: (String) -> Unit,
    priority: Int, onPriorityChange: (Int) -> Unit,
    onDismiss: () -> Unit, onConfirm: () -> Unit
) {
    val categories = listOf("ACADEMIC" to "Akademik", "PERSONAL" to "Personal", "PROFESSIONAL" to "Profesional")
    val priorities = listOf(1 to "Rendah", 2 to "Sedang", 3 to "Tinggi")

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier.size(42.dp)
                        .background(brush = Brush.linearGradient(GoalColors.fabGradient), shape = RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Flag, null, tint = Color.White, modifier = Modifier.size(22.dp))
                }
                Column {
                    Text("Buat Goal Baru", fontSize = 19.sp, fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface, letterSpacing = (-0.3).sp)
                    Text("Tentukan target yang ingin dicapai", fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Reuse DialogSectionLabel from SubjectsScreen
                DialogSectionLabel("Detail Goal")

                // Reuse PremiumTextField from SubjectsScreen
                PremiumTextField(value = title, onValueChange = onTitleChange,
                    label = "Judul Goal", placeholder = "Masukkan judul goal...")

                OutlinedTextField(
                    value = description, onValueChange = onDescriptionChange,
                    label = { Text("Deskripsi") },
                    placeholder = { Text("Deskripsi singkat...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)) },
                    modifier = Modifier.fillMaxWidth(), maxLines = 3,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        focusedBorderColor = GoalColors.emerald,
                        focusedLabelColor = GoalColors.emerald,
                        cursorColor = GoalColors.emerald
                    )
                )

                // Category grid
                DialogSectionLabel("Kategori")

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    categories.forEach { (key, label) ->
                        val isSelected = category == key
                        val catColor = GoalColors.categoryColor(key)
                        val catIcon = GoalColors.categoryIcon(key)

                        Surface(
                            onClick = { onCategoryChange(key) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) catColor.copy(alpha = 0.12f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, catColor.copy(alpha = 0.5f)) else null,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(catIcon, null, Modifier.size(20.dp),
                                    tint = if (isSelected) catColor
                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                                Text(label, fontSize = 11.sp, maxLines = 1,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) catColor else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                // Priority
                DialogSectionLabel("Prioritas")

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    priorities.forEach { (value, label) ->
                        val isSelected = priority == value
                        val prioColor = when (value) {
                            3 -> GoalColors.priorityHigh
                            2 -> GoalColors.priorityMed
                            else -> GoalColors.priorityLow
                        }
                        val prioIcon = when (value) {
                            3 -> Icons.Rounded.KeyboardDoubleArrowUp
                            2 -> Icons.Rounded.DragHandle
                            else -> Icons.Rounded.KeyboardArrowDown
                        }

                        Surface(
                            onClick = { onPriorityChange(value) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) prioColor.copy(alpha = 0.12f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, prioColor.copy(alpha = 0.5f)) else null,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(prioIcon, null, Modifier.size(20.dp),
                                    tint = if (isSelected) prioColor
                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                                Text(label, fontSize = 11.sp, maxLines = 1,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) prioColor else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm, enabled = title.isNotBlank(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoalColors.emerald),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Icon(Icons.Rounded.Flag, null, Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Simpan", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
            }
        }
    )
}

// ══════════════════════════════════════════════════════════════════════
// ── MILESTONE DIALOG ─────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════
@Composable
fun PremiumMilestoneDialog(
    title: String, onTitleChange: (String) -> Unit,
    onDismiss: () -> Unit, onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier.size(42.dp)
                        .background(brush = Brush.linearGradient(GoalColors.fabGradient), shape = RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Rocket, null, tint = Color.White, modifier = Modifier.size(22.dp))
                }
                Column {
                    Text("Tambah Milestone", fontSize = 19.sp, fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface, letterSpacing = (-0.3).sp)
                    Text("Langkah kecil menuju tujuan", fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                }
            }
        },
        text = {
            // Reuse PremiumTextField from SubjectsScreen
            PremiumTextField(
                value = title, onValueChange = onTitleChange,
                label = "Judul Milestone", placeholder = "Masukkan milestone...",
                leadingIcon = Icons.Rounded.CheckCircleOutline
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm, enabled = title.isNotBlank(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoalColors.emerald),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Icon(Icons.Rounded.Add, null, Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Simpan", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
            }
        }
    )
}