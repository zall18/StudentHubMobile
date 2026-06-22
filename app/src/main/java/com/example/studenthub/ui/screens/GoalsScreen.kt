package com.example.studenthub.ui.screens

import StudentHubViewModel
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.studenthub.data.Goal
import com.example.studenthub.data.GoalMilestone
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(viewModel: StudentHubViewModel) {
    val allGoals by viewModel.allGoals.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf("IN_PROGRESS") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showMilestoneDialog by remember { mutableStateOf(false) }
    var selectedGoalForMilestone by remember { mutableStateOf<Goal?>(null) }
    var selectedGoalForDetail by remember { mutableStateOf<Goal?>(null) }

    // Form fields
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("ACADEMIC") }
    var priority by remember { mutableStateOf(2) }

    var milestoneTitle by remember { mutableStateOf("") }

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

    // Stats
    val activeGoalsCount = remember(allGoals) {
        allGoals.count { it.status == "IN_PROGRESS" }
    }
    val completedGoalsCount = remember(allGoals) {
        allGoals.count { it.status == "COMPLETED" }
    }
    val averageProgress = remember(allGoals) {
        val activeGoals = allGoals.filter { it.status == "IN_PROGRESS" }
        if (activeGoals.isNotEmpty()) {
            activeGoals.map { it.progress }.average().toInt()
        } else 0
    }

    Scaffold(
        containerColor = Color(0xFFF8F9FA),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF2563EB),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Tambah Goal",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Column {
                    Text(
                        text = "Goals Tracker",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Lacak dan capai tujuanmu",
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }

            // Stats Summary
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        icon = Icons.Outlined.Flag,
                        value = activeGoalsCount.toString(),
                        label = "Goals Aktif",
                        containerColor = Color(0xFFEFF6FF),
                        contentColor = Color(0xFF2563EB),
                        modifier = Modifier.weight(1f)
                    )

                    StatCard(
                        icon = Icons.Outlined.CheckCircle,
                        value = completedGoalsCount.toString(),
                        label = "Selesai",
                        containerColor = Color(0xFFF0FDF4),
                        contentColor = Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    )

                    StatCard(
                        icon = Icons.Outlined.TrendingUp,
                        value = "$averageProgress%",
                        label = "Rata-rata Progress",
                        containerColor = Color(0xFFFAF5FF),
                        contentColor = Color(0xFF7C3AED),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Tab Filters
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tabs.size) { index ->
                        val (tabKey, tabLabel) = tabs[index]
                        FilterChip(
                            selected = selectedTab == tabKey,
                            onClick = { selectedTab = tabKey },
                            label = {
                                Text(
                                    text = tabLabel,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == tabKey) FontWeight.Medium else FontWeight.Normal
                                )
                            },
                            leadingIcon = if (selectedTab == tabKey) {
                                {
                                    Icon(
                                        Icons.Filled.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF2563EB),
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }

            // Goals count
            item {
                if (visibleGoals.isNotEmpty()) {
                    Text(
                        text = "${visibleGoals.size} goal ditemukan",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF374151)
                    )
                }
            }

            // Goals List
            if (visibleGoals.isEmpty()) {
                item {
                    EmptyGoalState(selectedTab)
                }
            } else {
                items(visibleGoals, key = { it.id }) { goal ->
                    EnhancedGoalCard(
                        goal = goal,
                        onAddMilestone = {
                            selectedGoalForMilestone = goal
                            showMilestoneDialog = true
                        },
                        onUpdateStatus = { newStatus ->
                            viewModel.updateGoalStatus(goal.id, newStatus)
                        },
                        onDelete = { viewModel.deleteGoal(goal.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Add Goal Dialog
    if (showAddDialog) {
        AddGoalDialog(
            title = title,
            onTitleChange = { title = it },
            description = description,
            onDescriptionChange = { description = it },
            category = category,
            onCategoryChange = { category = it },
            priority = priority,
            onPriorityChange = { priority = it },
            onDismiss = {
                showAddDialog = false
                title = ""
                description = ""
                category = "ACADEMIC"
                priority = 2
            },
            onConfirm = {
                if (title.isNotBlank()) {
                    viewModel.addGoal(
                        title = title,
                        description = description,
                        category = category,
                        targetDate = System.currentTimeMillis() + (30L * 24L * 60L * 60L * 1000L),
                        priority = priority
                    )
                    showAddDialog = false
                    title = ""
                    description = ""
                    category = "ACADEMIC"
                    priority = 2
                }
            }
        )
    }

    // Add Milestone Dialog
    if (showMilestoneDialog && selectedGoalForMilestone != null) {
        AddMilestoneDialog(
            title = milestoneTitle,
            onTitleChange = { milestoneTitle = it },
            onDismiss = {
                showMilestoneDialog = false
                milestoneTitle = ""
                selectedGoalForMilestone = null
            },
            onConfirm = {
                val goal = selectedGoalForMilestone
                if (goal != null && milestoneTitle.isNotBlank()) {
                    viewModel.addGoalMilestone(
                        goalId = goal.id,
                        title = milestoneTitle,
                        targetDate = System.currentTimeMillis()
                    )
                    showMilestoneDialog = false
                    milestoneTitle = ""
                    selectedGoalForMilestone = null
                }
            }
        )
    }
}

@Composable
fun StatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    text = value,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = contentColor.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun EmptyGoalState(selectedTab: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Outlined.Flag,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color(0xFFD1D5DB)
            )
            Text(
                text = when (selectedTab) {
                    "IN_PROGRESS" -> "Belum ada goal aktif"
                    "COMPLETED" -> "Belum ada goal selesai"
                    "ABANDONED" -> "Tidak ada goal dihentikan"
                    else -> "Belum ada goal"
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF6B7280)
            )
            Text(
                text = "Buat goal baru untuk mulai melacak progresmu",
                fontSize = 13.sp,
                color = Color(0xFF9CA3AF),
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun EnhancedGoalCard(
    goal: Goal,
    onAddMilestone: () -> Unit,
    onUpdateStatus: (String) -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showStatusMenu by remember { mutableStateOf(false) }

    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
    val targetText = dateFormat.format(Date(goal.targetDate))
    val daysRemaining = ((goal.targetDate - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)).toInt()

    val categoryColor = remember(goal.category) {
        when (goal.category) {
            "ACADEMIC" -> Color(0xFF2563EB)
            "PERSONAL" -> Color(0xFF7C3AED)
            "PROFESSIONAL" -> Color(0xFFEA580C)
            else -> Color(0xFF6B7280)
        }
    }

    val categoryIcon = remember(goal.category) {
        when (goal.category) {
            "ACADEMIC" -> Icons.Outlined.School
            "PERSONAL" -> Icons.Outlined.Person
            "PROFESSIONAL" -> Icons.Outlined.Work
            else -> Icons.Outlined.Flag
        }
    }

    val progressColor = when {
        goal.progress >= 80 -> Color(0xFF10B981)
        goal.progress >= 40 -> Color(0xFFF59E0B)
        goal.progress > 0 -> Color(0xFF3B82F6)
        else -> Color(0xFFE5E7EB)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (goal.status == "COMPLETED") Color(0xFFF0FDF4) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = if (goal.status == "COMPLETED") {
            androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD1FAE5))
        } else null
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Category icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(categoryColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        categoryIcon,
                        contentDescription = goal.category,
                        modifier = Modifier.size(24.dp),
                        tint = categoryColor
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    // Title & Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = goal.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (goal.status == "COMPLETED") Color(0xFF059669) else Color(0xFF111827),
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Priority badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when (goal.priority) {
                                3 -> Color(0xFFFEF2F2)
                                2 -> Color(0xFFFFFBEB)
                                else -> Color(0xFFF0FDF4)
                            }
                        ) {
                            Text(
                                text = when (goal.priority) {
                                    3 -> "Tinggi"
                                    2 -> "Sedang"
                                    else -> "Rendah"
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = when (goal.priority) {
                                    3 -> Color(0xFFDC2626)
                                    2 -> Color(0xFFD97706)
                                    else -> Color(0xFF059669)
                                }
                            )
                        }
                    }

                    // Description
                    if (goal.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = goal.description,
                            fontSize = 13.sp,
                            color = Color(0xFF6B7280),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tags
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Category badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = categoryColor.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = when (goal.category) {
                                    "ACADEMIC" -> "Akademik"
                                    "PERSONAL" -> "Personal"
                                    "PROFESSIONAL" -> "Profesional"
                                    else -> goal.category
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = categoryColor
                            )
                        }

                        // Status badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when (goal.status) {
                                "COMPLETED" -> Color(0xFFD1FAE5)
                                "IN_PROGRESS" -> Color(0xFFDBEAFE)
                                "ABANDONED" -> Color(0xFFF3F4F6)
                                else -> Color(0xFFF3F4F6)
                            }
                        ) {
                            Text(
                                text = when (goal.status) {
                                    "COMPLETED" -> "Selesai"
                                    "IN_PROGRESS" -> "Dalam Proses"
                                    "ABANDONED" -> "Dihentikan"
                                    else -> goal.status
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = when (goal.status) {
                                    "COMPLETED" -> Color(0xFF059669)
                                    "IN_PROGRESS" -> Color(0xFF2563EB)
                                    "ABANDONED" -> Color(0xFF6B7280)
                                    else -> Color(0xFF6B7280)
                                }
                            )
                        }

                        // Target date
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Outlined.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = Color(0xFF9CA3AF)
                            )
                            Text(
                                text = targetText,
                                fontSize = 11.sp,
                                color = Color(0xFF9CA3AF)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress section
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Progress",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF6B7280)
                    )
                    Text(
                        text = "${goal.progress.toInt()}%",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = progressColor
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { (goal.progress.coerceIn(0f, 100f)) / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = progressColor,
                    trackColor = Color(0xFFF3F4F6),
                )

                // Days remaining (only for active goals)
                if (goal.status == "IN_PROGRESS" && daysRemaining > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "$daysRemaining hari tersisa",
                        fontSize = 11.sp,
                        color = if (daysRemaining <= 7) Color(0xFFDC2626) else Color(0xFF6B7280)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left actions
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (goal.status == "IN_PROGRESS") {
                        // Add milestone button
                        TextButton(
                            onClick = onAddMilestone,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Milestone",
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // Right actions
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (goal.status == "IN_PROGRESS") {
                        // Complete button
                        IconButton(
                            onClick = { onUpdateStatus("COMPLETED") },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Outlined.CheckCircle,
                                contentDescription = "Tandai Selesai",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Delete button
                    IconButton(
                        onClick = { showDeleteConfirmation = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = "Hapus",
                            tint = Color(0xFFD1D5DB),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    // Delete Confirmation
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    text = "Hapus Goal",
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text("Apakah kamu yakin ingin menghapus \"${goal.title}\"? Semua milestone akan ikut terhapus.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFDC2626)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalDialog(
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    category: String,
    onCategoryChange: (String) -> Unit,
    priority: Int,
    onPriorityChange: (Int) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "Buat Goal Baru",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF111827)
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    label = { Text("Judul Goal") },
                    placeholder = { Text("Masukkan judul goal...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    label = { Text("Deskripsi") },
                    placeholder = { Text("Deskripsi singkat...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )

                // Category
                Column {
                    Text(
                        text = "Kategori",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF374151)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "ACADEMIC" to "Akademik",
                            "PERSONAL" to "Personal",
                            "PROFESSIONAL" to "Profesional"
                        ).forEach { (key, label) ->
                            FilterChip(
                                selected = category == key,
                                onClick = { onCategoryChange(key) },
                                label = {
                                    Text(
                                        text = label,
                                        fontSize = 13.sp
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF2563EB),
                                    selectedLabelColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }

                // Priority
                Column {
                    Text(
                        text = "Prioritas",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF374151)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            1 to "Rendah",
                            2 to "Sedang",
                            3 to "Tinggi"
                        ).forEach { (value, label) ->
                            FilterChip(
                                selected = priority == value,
                                onClick = { onPriorityChange(value) },
                                label = {
                                    Text(
                                        text = label,
                                        fontSize = 13.sp
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF2563EB),
                                    selectedLabelColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = title.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2563EB)
                )
            ) {
                Text(
                    text = "Simpan",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Batal",
                    color = Color(0xFF6B7280)
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMilestoneDialog(
    title: String,
    onTitleChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "Tambah Milestone",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF111827)
            )
        },
        text = {
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                label = { Text("Judul Milestone") },
                placeholder = { Text("Masukkan milestone...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = title.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2563EB)
                )
            ) {
                Text(
                    text = "Simpan",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Batal",
                    color = Color(0xFF6B7280)
                )
            }
        }
    )
}