package com.example.studenthub.ui.screens

import StudentHubViewModel
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedDashboardScreen(viewModel: StudentHubViewModel) {
    val upcomingTasks by viewModel.upcomingTasks.collectAsStateWithLifecycle()
    val activeGoals by viewModel.activeGoals.collectAsStateWithLifecycle()
    val allSubjects by viewModel.allSubjects.collectAsStateWithLifecycle()
    val highPriorityTaskCount by remember { derivedStateOf { upcomingTasks.count { it.priority == 3 } } }

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var taskTitle by remember { mutableStateOf("") }
    var taskCategory by remember { mutableStateOf("Kuliah") }
    var taskPriority by remember { mutableStateOf(2) }

    Scaffold(
        containerColor = Color(0xFFF8F9FA),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTaskDialog = true },
                containerColor = Color(0xFF2563EB),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Tambah Tugas",
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header Section
            item {
                GreetingSection()
            }

            // Stats Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        icon = Icons.Outlined.Create,
                        title = "Tugas",
                        value = upcomingTasks.size.toString(),
                        subtitle = "minggu ini",
                        containerColor = Color(0xFFEFF6FF),
                        contentColor = Color(0xFF2563EB),
                        modifier = Modifier.weight(1f)
                    )

                    StatCard(
                        icon = Icons.Outlined.Lock,
                        title = "Prioritas",
                        value = highPriorityTaskCount.toString(),
                        subtitle = "urgent",
                        containerColor = Color(0xFFFEF2F2),
                        contentColor = Color(0xFFDC2626),
                        modifier = Modifier.weight(1f)
                    )

                    StatCard(
                        icon = Icons.Outlined.PlayArrow,
                        title = "Goals",
                        value = activeGoals.size.toString(),
                        subtitle = "aktif",
                        containerColor = Color(0xFFFAF5FF),
                        contentColor = Color(0xFF7C3AED),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Quick Add Section
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAddTaskDialog = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = CardDefaults.outlinedCardBorder().let {
                        androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.AddCircle,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Tambah tugas baru...",
                            fontSize = 14.sp,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                }
            }

            // Upcoming Tasks Section
            if (upcomingTasks.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Tugas Mendatang",
                        count = upcomingTasks.size
                    )
                }

                items(upcomingTasks.take(5), key = { it.id }) { task ->
                    EnhancedTaskCard(
                        task = task,
                        onComplete = { viewModel.completeTask(task.id) },
                        onDelete = { viewModel.deleteTask(task.id) }
                    )
                }
            }

            // Today's Schedule Section
            if (allSubjects.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Jadwal Kuliah",
                        count = allSubjects.size
                    )
                }

                items(allSubjects.take(3), key = { it.id }) { subject ->
                    ModernSubjectCard(subject)
                }
            }

            // Active Goals Section
            if (activeGoals.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Goals Aktif",
                        count = activeGoals.size
                    )
                }

                items(activeGoals.take(3), key = { it.id }) { goal ->
                    ModernGoalCard(goal)
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Add Task Dialog
    if (showAddTaskDialog) {
        AddTaskDialog(
            taskTitle = taskTitle,
            onTaskTitleChange = { taskTitle = it },
            taskCategory = taskCategory,
            onTaskCategoryChange = { taskCategory = it },
            onTaskPriorityChange = { taskPriority = it },
            taskPriority = taskPriority,
            onDismiss = {
                showAddTaskDialog = false
                taskTitle = ""
                taskCategory = "Kuliah"
                taskPriority = 2
            },
            onConfirm = {
                if (taskTitle.isNotBlank()) {
                    viewModel.addTask(
                        title = taskTitle,
                        category = taskCategory,
                        description = "",
                        priority = taskPriority,
                        deadline = System.currentTimeMillis() + (24 * 60 * 60 * 1000)
                    )
                    showAddTaskDialog = false
                    taskTitle = ""
                    taskCategory = "Kuliah"
                    taskPriority = 2
                }
            }
        )
    }
}

@Composable
fun GreetingSection() {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 0..11 -> "Selamat Pagi"
        in 12..14 -> "Selamat Siang"
        in 15..17 -> "Selamat Sore"
        else -> "Selamat Malam"
    }

    Column {
        Text(
            text = "$greeting! 👋",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111827)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Yuk selesaikan tugasmu hari ini",
            fontSize = 14.sp,
            color = Color(0xFF6B7280)
        )
    }
}

@Composable
fun StatCard(
    icon: ImageVector,
    title: String,
    value: String,
    subtitle: String,
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
                    text = title,
                    fontSize = 12.sp,
                    color = contentColor.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = contentColor.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    count: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF111827)
        )
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFF3F4F6)
        ) {
            Text(
                text = "$count",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF6B7280)
            )
        }
    }
}

@Composable
fun EnhancedTaskCard(
    task: com.example.studenthub.data.Task,
    onComplete: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy", Locale("id", "ID"))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Priority indicator
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        when (task.priority) {
                            3 -> Color(0xFFEF4444)
                            2 -> Color(0xFFF59E0B)
                            else -> Color(0xFF10B981)
                        }
                    )
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF111827),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color(0xFF9CA3AF)
                    )
                    Text(
                        text = dateFormat.format(Date(task.deadline)),
                        fontSize = 12.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PriorityChip(task.priority)
                    CategoryChip(task.category)
                }
            }

            // Action buttons
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onComplete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Outlined.CheckCircle,
                        contentDescription = "Selesai",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = "Hapus",
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PriorityChip(priority: Int) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = when (priority) {
            3 -> Color(0xFFFEF2F2)
            2 -> Color(0xFFFFFBEB)
            else -> Color(0xFFF0FDF4)
        }
    ) {
        Text(
            text = when (priority) {
                3 -> "Tinggi"
                2 -> "Sedang"
                else -> "Rendah"
            },
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = when (priority) {
                3 -> Color(0xFFDC2626)
                2 -> Color(0xFFD97706)
                else -> Color(0xFF059669)
            }
        )
    }
}

@Composable
fun CategoryChip(category: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = Color(0xFFF3F4F6)
    ) {
        Text(
            text = category,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF6B7280)
        )
    }
}

@Composable
fun ModernSubjectCard(subject: com.example.studenthub.data.Subject) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color indicator
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        try {
                            val colorLong = subject.color.removePrefix("#").toLong(16)
                            Color(colorLong.toInt())
                        } catch (e: Exception) {
                            Color(0xFF2563EB)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = subject.code.take(2),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = subject.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF111827)
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (subject.scheduleDay.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Email,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFF9CA3AF)
                        )
                        Text(
                            text = "${subject.scheduleDay}, ${subject.scheduleTime}",
                            fontSize = 12.sp,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Outlined.Person,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color(0xFF9CA3AF)
                    )
                    Text(
                        text = subject.lecturer,
                        fontSize = 12.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = try {
                        val colorLong = subject.color.removePrefix("#").toLong(16)
                        Color(colorLong.toInt()).copy(alpha = 0.1f)
                    } catch (e: Exception) {
                        Color(0xFFEFF6FF)
                    }
                ) {
                    Text(
                        text = "${subject.credits} SKS",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = try {
                            val colorLong = subject.color.removePrefix("#").toLong(16)
                            Color(colorLong.toInt())
                        } catch (e: Exception) {
                            Color(0xFF2563EB)
                        }
                    )
                }

                if (subject.roomLocation.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subject.roomLocation,
                        fontSize = 11.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }
            }
        }
    }
}

@Composable
fun ModernGoalCard(goal: com.example.studenthub.data.Goal) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = goal.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF111827),
                    modifier = Modifier.weight(1f)
                )

                GoalCategoryBadge(goal.category)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar with percentage
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LinearProgressIndicator(
                    progress = { (goal.progress / 100f) },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = when {
                        goal.progress >= 80 -> Color(0xFF10B981)
                        goal.progress >= 40 -> Color(0xFFF59E0B)
                        else -> Color(0xFF3B82F6)
                    },
                    trackColor = Color(0xFFF3F4F6),
                )

                Text(
                    text = "${goal.progress.roundToInt()}%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = when {
                        goal.progress >= 80 -> Color(0xFF059669)
                        goal.progress >= 40 -> Color(0xFFD97706)
                        else -> Color(0xFF2563EB)
                    }
                )
            }
        }
    }
}

@Composable
fun GoalCategoryBadge(category: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = when (category) {
            "ACADEMIC" -> Color(0xFFEFF6FF)
            "PERSONAL" -> Color(0xFFFAF5FF)
            else -> Color(0xFFFFFBEB)
        }
    ) {
        Text(
            text = category.replace("_", " ").lowercase()
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() },
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = when (category) {
                "ACADEMIC" -> Color(0xFF2563EB)
                "PERSONAL" -> Color(0xFF7C3AED)
                else -> Color(0xFFD97706)
            }
        )
    }
}

