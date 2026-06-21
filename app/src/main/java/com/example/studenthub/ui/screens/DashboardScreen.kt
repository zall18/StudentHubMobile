package com.example.studenthub.ui.screens

import StudentHubViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EnhancedDashboardScreen(viewModel: StudentHubViewModel) {
    val upcomingTasks by viewModel.upcomingTasks.collectAsStateWithLifecycle()
    val activeGoals by viewModel.activeGoals.collectAsStateWithLifecycle()
    val allSubjects by viewModel.allSubjects.collectAsStateWithLifecycle()
    val highPriorityTaskCount by remember { derivedStateOf { upcomingTasks.count { it.priority == 3 } } }

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var taskTitle by remember { mutableStateOf("") }
    var taskDescription by remember { mutableStateOf("") }
    var taskCategory by remember { mutableStateOf("Kuliah") }
    var taskPriority by remember { mutableStateOf(2) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTaskDialog = true },
                containerColor = Color(0xFF0066FF),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Tugas")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Selamat Pagi! 👋",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Text(
                    text = "Mari optimalkan hari kamu dengan StudentHub",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            // Quick Stats Cards
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Card 1: Active Tasks
                    StatsCard(
                        title = "Tugas Aktif",
                        value = upcomingTasks.size.toString(),
                        subtitle = "dalam 7 hari",
                        backgroundColor = Color(0xFFE3F2FD),
                        textColor = Color(0xFF1E88E5),
                        modifier = Modifier.weight(1f)
                    )

                    // Card 2: High Priority
                    StatsCard(
                        title = "Prioritas Tinggi",
                        value = highPriorityTaskCount.toString(),
                        subtitle = "Urgent!",
                        backgroundColor = Color(0xFFFFEBEE),
                        textColor = Color(0xFFC62828),
                        modifier = Modifier.weight(1f)
                    )

                    // Card 3: Active Goals
                    StatsCard(
                        title = "Goals Aktif",
                        value = activeGoals.size.toString(),
                        subtitle = "Sedang dikerjakan",
                        backgroundColor = Color(0xFFF3E5F5),
                        textColor = Color(0xFF6A1B9A),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Today's Focus Section
            if (upcomingTasks.isNotEmpty()) {
                item {
                    Text(
                        text = "Fokus Hari Ini 🎯",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                }

                items(upcomingTasks.take(3), key = { it.id }) { task ->
                    DashboardTaskCard(
                        task = task,
                        onMarkComplete = { viewModel.completeTask(task.id) },
                        onDelete = { viewModel.deleteTask(task.id) }
                    )
                }
            }

            // Subjects Schedule
            if (allSubjects.isNotEmpty()) {
                item {
                    Text(
                        text = "Jadwal Kuliah 📚",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                }

                items(allSubjects.take(3), key = { it.id }) { subject ->
                    SubjectScheduleCard(subject)
                }
            }

            // Goals Progress
            if (activeGoals.isNotEmpty()) {
                item {
                    Text(
                        text = "Progress Goals 📈",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                }

                items(activeGoals.take(2), key = { it.id }) { goal ->
                    GoalProgressCard(goal)
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Add Task Dialog
    if (showAddTaskDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddTaskDialog = false
                taskTitle = ""
                taskDescription = ""
                taskCategory = "Kuliah"
                taskPriority = 2
            },
            title = { Text("Tambah Tugas Baru") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = taskTitle,
                        onValueChange = { taskTitle = it },
                        label = { Text("Judul Tugas") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 1
                    )

                    OutlinedTextField(
                        value = taskDescription,
                        onValueChange = { taskDescription = it },
                        label = { Text("Deskripsi") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 3
                    )

                    // Priority Selector
                    Text("Prioritas:", fontWeight = FontWeight.Medium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(1 to "Rendah", 2 to "Sedang", 3 to "Tinggi").forEach { (priority, label) ->
                            FilterChip(
                                selected = taskPriority == priority,
                                onClick = { taskPriority = priority },
                                label = { Text(label) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Category Selector
                    Text("Kategori:", fontWeight = FontWeight.Medium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Kuliah", "UKM", "Personal").forEach { category ->
                            FilterChip(
                                selected = taskCategory == category,
                                onClick = { taskCategory = category },
                                label = { Text(category) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (taskTitle.isNotBlank()) {
                            viewModel.addTask(
                                title = taskTitle,
                                category = taskCategory,
                                description = taskDescription,
                                priority = taskPriority,
                                deadline = System.currentTimeMillis() + (24 * 60 * 60 * 1000)
                            )
                            showAddTaskDialog = false
                            taskTitle = ""
                            taskDescription = ""
                            taskCategory = "Kuliah"
                            taskPriority = 2
                        }
                    }
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddTaskDialog = false
                    taskTitle = ""
                    taskDescription = ""
                }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun StatsCard(
    title: String,
    value: String,
    subtitle: String,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxHeight()
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                color = textColor.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = textColor.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun DashboardTaskCard(
    task: com.example.studenthub.data.Task,
    onMarkComplete: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM", Locale("id", "ID"))
    val deadlineDate = dateFormat.format(Date(task.deadline))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color(0xFF1A1A1A)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Priority Badge
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = when (task.priority) {
                            3 -> Color(0xFFFFEBEE)
                            2 -> Color(0xFFFFF3E0)
                            else -> Color(0xFFF1F8E9)
                        }
                    ) {
                        Text(
                            text = when (task.priority) {
                                3 -> "🔴 Tinggi"
                                2 -> "🟡 Sedang"
                                else -> "🟢 Rendah"
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Category Badge
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFE3F2FD)
                    ) {
                        Text(
                            text = task.category,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            color = Color(0xFF1E88E5)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Deadline: $deadlineDate",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = onMarkComplete,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Mark Complete",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SubjectScheduleCard(subject: com.example.studenthub.data.Subject) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = subject.code,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = subject.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1A1A1A)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(subject.color.ifBlank { "#0066FF" }.toLongOrNull(16)?.toInt() ?: 0xFF0066FF.toInt())
                ) {
                    Text(
                        text = "${subject.credits} SKS",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            if (subject.scheduleDay.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${subject.scheduleDay} • ${subject.scheduleTime} • ${subject.roomLocation}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Dosen: ${subject.lecturer}",
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun GoalProgressCard(goal: com.example.studenthub.data.Goal) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    text = goal.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1A1A),
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = when (goal.category) {
                        "ACADEMIC" -> Color(0xFFE3F2FD)
                        "PERSONAL" -> Color(0xFFF3E5F5)
                        else -> Color(0xFFFFF3E0)
                    }
                ) {
                    Text(
                        text = goal.category.replace("_", " "),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = when (goal.category) {
                            "ACADEMIC" -> Color(0xFF1E88E5)
                            "PERSONAL" -> Color(0xFF6A1B9A)
                            else -> Color(0xFFF57C00)
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { goal.progress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = Color(0xFF4CAF50),
                trackColor = Color(0xFFE0E0E0)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${goal.progress.toInt()}% Progress",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Status: ${goal.status}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
