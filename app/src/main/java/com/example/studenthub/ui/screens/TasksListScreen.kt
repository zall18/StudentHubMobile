package com.example.studenthub.ui.screens

import StudentHubViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.studenthub.data.Task
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TasksListScreen(viewModel: StudentHubViewModel) {
    val activeTasks by viewModel.activeTasks.collectAsStateWithLifecycle()
    val completedTasks by viewModel.completedTasks.collectAsStateWithLifecycle()
    
    var selectedFilter by remember { mutableStateOf("ALL") }
    var selectedSort by remember { mutableStateOf("DEADLINE") }
    var showAddDialog by remember { mutableStateOf(false) }
    var taskTitle by remember { mutableStateOf("") }
    var taskDescription by remember { mutableStateOf("") }
    var taskCategory by remember { mutableStateOf("Kuliah") }
    var taskPriority by remember { mutableStateOf(2) }

    val filteredTasks = when (selectedFilter) {
        "TODAY" -> activeTasks.filter { isTaskToday(it.deadline) }
        "WEEK" -> activeTasks.filter { isTaskThisWeek(it.deadline) }
        "OVERDUE" -> activeTasks.filter { it.deadline < System.currentTimeMillis() }
        "HIGH_PRIORITY" -> activeTasks.filter { it.priority == 3 }
        "COMPLETED" -> completedTasks
        else -> activeTasks
    }

    val sortedTasks = when (selectedSort) {
        "PRIORITY" -> filteredTasks.sortedByDescending { it.priority }
        "CATEGORY" -> filteredTasks.sortedBy { it.category }
        else -> filteredTasks.sortedBy { it.deadline }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Daftar Tugas",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
            }

            // Filter Tabs
            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf(
                        "ALL" to "Semua",
                        "TODAY" to "Hari Ini",
                        "WEEK" to "Minggu Ini",
                        "HIGH_PRIORITY" to "Prioritas Tinggi",
                        "OVERDUE" to "Terlambat",
                        "COMPLETED" to "Selesai"
                    )
                    items(filters.size) { index ->
                        val (filterKey, filterLabel) = filters[index]
                        FilterChip(
                            selected = selectedFilter == filterKey,
                            onClick = { selectedFilter = filterKey },
                            label = { Text(filterLabel, fontSize = 12.sp) }
                        )
                    }
                }
            }

            // Sort Options
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Sort:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.align(androidx.compose.ui.Alignment.CenterVertically)
                    )
                    listOf(
                        "DEADLINE" to "Deadline",
                        "PRIORITY" to "Prioritas",
                        "CATEGORY" to "Kategori"
                    ).forEach { (sortKey, sortLabel) ->
                        FilterChip(
                            selected = selectedSort == sortKey,
                            onClick = { selectedSort = sortKey },
                            label = { Text(sortLabel, fontSize = 11.sp) }
                        )
                    }
                }
            }

            // Task List
            if (sortedTasks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Text(
                            text = "Tidak ada tugas",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                items(sortedTasks, key = { it.id }) { task ->
                    TaskListCard(
                        task = task,
                        onMarkComplete = { viewModel.completeTask(task.id) },
                        onDelete = { viewModel.deleteTask(task.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Add Task Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
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

                    Text("Prioritas:", fontWeight = FontWeight.Medium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(1 to "Rendah", 2 to "Sedang", 3 to "Tinggi").forEach { (priority, label) ->
                            FilterChip(
                                selected = taskPriority == priority,
                                onClick = { taskPriority = priority },
                                label = { Text(label) }
                            )
                        }
                    }

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
                            showAddDialog = false
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
                    showAddDialog = false
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
fun TaskListCard(
    task: Task,
    onMarkComplete: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
    val deadlineDate = dateFormat.format(Date(task.deadline))
    val isOverdue = task.deadline < System.currentTimeMillis()

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
                        text = task.title,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = Color(0xFF1A1A1A)
                    )
                    if (task.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = task.description,
                            fontSize = 12.sp,
                            color = Color.Gray,
                            maxLines = 2
                        )
                    }
                }

                Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
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
                                3 -> "🔴"
                                2 -> "🟡"
                                else -> "🟢"
                            },
                            modifier = Modifier.padding(4.dp),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Category Badge
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFE3F2FD)
                    ) {
                        Text(
                            text = task.category,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            color = Color(0xFF1E88E5),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Deadline
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isOverdue) Color(0xFFFFEBEE) else Color(0xFFF1F8E9)
                    ) {
                        Text(
                            text = deadlineDate,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            color = if (isOverdue) Color(0xFFC62828) else Color(0xFF2E7D32),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onMarkComplete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Mark Complete",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun isTaskToday(deadline: Long): Boolean {
    val today = Calendar.getInstance()
    val taskDate = Calendar.getInstance().apply { timeInMillis = deadline }
    return today.get(Calendar.YEAR) == taskDate.get(Calendar.YEAR) &&
            today.get(Calendar.DAY_OF_YEAR) == taskDate.get(Calendar.DAY_OF_YEAR)
}

private fun isTaskThisWeek(deadline: Long): Boolean {
    val today = Calendar.getInstance()
    val taskDate = Calendar.getInstance().apply { timeInMillis = deadline }
    val daysUntilDeadline = (deadline - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)
    return daysUntilDeadline in 0..7
}
