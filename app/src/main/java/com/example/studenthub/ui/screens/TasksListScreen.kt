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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.studenthub.data.Task
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksListScreen(viewModel: StudentHubViewModel) {
    val activeTasks by viewModel.activeTasks.collectAsStateWithLifecycle()
    val completedTasks by viewModel.completedTasks.collectAsStateWithLifecycle()

    var selectedFilter by remember { mutableStateOf("ALL") }
    var selectedSort by remember { mutableStateOf("DEADLINE") }
    var showAddDialog by remember { mutableStateOf(false) }
    var taskTitle by remember { mutableStateOf("") }
    var taskCategory by remember { mutableStateOf("Kuliah") }
    var taskPriority by remember { mutableStateOf(2) }

    // Search query
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    val filteredTasks = remember(activeTasks, completedTasks, selectedFilter, searchQuery) {
        val tasks = when (selectedFilter) {
            "TODAY" -> activeTasks.filter { isTaskToday(it.deadline) }
            "WEEK" -> activeTasks.filter { isTaskThisWeek(it.deadline) }
            "OVERDUE" -> activeTasks.filter { it.deadline < System.currentTimeMillis() }
            "HIGH_PRIORITY" -> activeTasks.filter { it.priority == 3 }
            "COMPLETED" -> completedTasks
            else -> activeTasks
        }

        if (searchQuery.isNotBlank()) {
            tasks.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.category.contains(searchQuery, ignoreCase = true) ||
                        it.description.contains(searchQuery, ignoreCase = true)
            }
        } else {
            tasks
        }
    }

    val sortedTasks = remember(filteredTasks, selectedSort) {
        when (selectedSort) {
            "PRIORITY" -> filteredTasks.sortedByDescending { it.priority }
            "CATEGORY" -> filteredTasks.sortedBy { it.category }
            "TITLE" -> filteredTasks.sortedBy { it.title.lowercase() }
            else -> filteredTasks.sortedBy { it.deadline }
        }
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Column {
                    Text(
                        text = "Daftar Tugas",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${activeTasks.size} tugas aktif • ${completedTasks.size} selesai",
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }

            // Search Bar
            item {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onSearch = { isSearchActive = false },
                    active = false,
                    onActiveChange = { isSearchActive = it },
                    placeholder = { Text("Cari tugas...") },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.Search,
                            contentDescription = "Search"
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    Icons.Outlined.Close,
                                    contentDescription = "Clear"
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = SearchBarDefaults.colors(
                        containerColor = Color.White
                    )
                ) {}
            }

            // Filter Chips
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Category Filters
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val filters = listOf(
                            "ALL" to "Semua",
                            "TODAY" to "Hari Ini",
                            "WEEK" to "Minggu Ini",
                            "HIGH_PRIORITY" to "Prioritas",
                            "OVERDUE" to "Terlambat",
                            "COMPLETED" to "Selesai"
                        )

                        filters.forEach { (filterKey, filterLabel) ->
                            FilterChip(
                                selected = selectedFilter == filterKey,
                                onClick = { selectedFilter = filterKey },
                                label = {
                                    Text(
                                        text = filterLabel,
                                        fontSize = 12.sp,
                                        fontWeight = if (selectedFilter == filterKey) FontWeight.Medium else FontWeight.Normal
                                    )
                                },
                                leadingIcon = if (selectedFilter == filterKey) {
                                    { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF2563EB),
                                    selectedLabelColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    // Sort Options
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Outlined.KeyboardArrowDown,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF6B7280)
                        )

                        Text(
                            text = "Urutkan:",
                            fontSize = 12.sp,
                            color = Color(0xFF6B7280),
                            fontWeight = FontWeight.Medium
                        )

                        listOf(
                            "DEADLINE" to "Deadline",
                            "PRIORITY" to "Prioritas",
                            "CATEGORY" to "Kategori",
                            "TITLE" to "Judul"
                        ).forEach { (sortKey, sortLabel) ->
                            TextButton(
                                onClick = { selectedSort = sortKey },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text(
                                    text = sortLabel,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedSort == sortKey) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (selectedSort == sortKey) Color(0xFF2563EB) else Color(0xFF6B7280)
                                )
                            }
                        }
                    }
                }
            }

            // Task Count & Quick Stats
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${sortedTasks.size} tugas ditemukan",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF374151)
                    )

                    if (selectedFilter == "ALL") {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            TaskStatBadge(
                                icon = Icons.Outlined.Warning,
                                text = activeTasks.count { it.deadline < System.currentTimeMillis() }.toString(),
                                color = Color(0xFFDC2626)
                            )
                            TaskStatBadge(
                                icon = Icons.Outlined.LocationOn,
                                text = activeTasks.count { isTaskToday(it.deadline) }.toString(),
                                color = Color(0xFFF59E0B)
                            )
                        }
                    }
                }
            }

            // Task List
            if (sortedTasks.isEmpty()) {
                item {
                    EmptyStateView()
                }
            } else {
                items(sortedTasks, key = { it.id }) { task ->
                    ModernTaskCard(
                        task = task,
                        isCompleted = selectedFilter == "COMPLETED",
                        onToggleComplete = {
                            if (selectedFilter == "COMPLETED") {
                                viewModel.uncompleteTask(task.id)
                            } else {
                                viewModel.completeTask(task.id)
                            }
                        },
                        onDelete = { viewModel.deleteTask(task.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Add Task Dialog
    if (showAddDialog) {
        AddTaskDialog(
            taskTitle = taskTitle,
            onTaskTitleChange = { taskTitle = it },
            taskCategory = taskCategory,
            onTaskCategoryChange = { taskCategory = it },
            taskPriority = taskPriority,
            onTaskPriorityChange = { taskPriority = it },
            onDismiss = {
                showAddDialog = false
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
                    showAddDialog = false
                    taskTitle = ""
                    taskCategory = "Kuliah"
                    taskPriority = 2
                }
            }
        )
    }
}

@Composable
fun TaskStatBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = color
        )
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

@Composable
fun EmptyStateView() {
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
                Icons.Outlined.Email,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color(0xFFD1D5DB)
            )
            Text(
                text = "Tidak ada tugas",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF6B7280)
            )
            Text(
                text = "Semua tugas sudah selesai atau belum ada tugas yang ditambahkan",
                fontSize = 13.sp,
                color = Color(0xFF9CA3AF),
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun ModernTaskCard(
    task: Task,
    isCompleted: Boolean,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy", Locale("id", "ID"))
    val deadlineDate = dateFormat.format(Date(task.deadline))
    val isOverdue = task.deadline < System.currentTimeMillis()
    val daysRemaining = ((task.deadline - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)).toInt()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOverdue && !isCompleted) Color(0xFFFFF5F5) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isOverdue && !isCompleted) 0.dp else 1.dp),
        border = if (isOverdue && !isCompleted) {
            androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA))
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Priority indicator bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(if (task.description.isNotBlank()) 56.dp else 40.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        when {
                            isCompleted -> Color(0xFFD1D5DB)
                            task.priority == 3 -> Color(0xFFEF4444)
                            task.priority == 2 -> Color(0xFFF59E0B)
                            else -> Color(0xFF10B981)
                        }
                    )
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Title
                Text(
                    text = task.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isCompleted) Color(0xFF9CA3AF) else Color(0xFF111827),
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Description (if any)
                if (task.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.description,
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tags Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category chip
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isCompleted) Color(0xFFF3F4F6) else Color(0xFFEFF6FF)
                    ) {
                        Text(
                            text = task.category,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isCompleted) Color(0xFF9CA3AF) else Color(0xFF2563EB)
                        )
                    }

                    // Priority chip
                    if (!isCompleted) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when (task.priority) {
                                3 -> Color(0xFFFEF2F2)
                                2 -> Color(0xFFFFFBEB)
                                else -> Color(0xFFF0FDF4)
                            }
                        ) {
                            Text(
                                text = when (task.priority) {
                                    3 -> "Tinggi"
                                    2 -> "Sedang"
                                    else -> "Rendah"
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = when (task.priority) {
                                    3 -> Color(0xFFDC2626)
                                    2 -> Color(0xFFD97706)
                                    else -> Color(0xFF059669)
                                }
                            )
                        }
                    }

                    // Deadline chip
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when {
                            isCompleted -> Color(0xFFF3F4F6)
                            isOverdue -> Color(0xFFFEF2F2)
                            daysRemaining <= 2 -> Color(0xFFFFFBEB)
                            else -> Color(0xFFF0FDF4)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (isOverdue && !isCompleted) Icons.Outlined.Warning
                                else Icons.Outlined.DateRange,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = when {
                                    isCompleted -> Color(0xFF9CA3AF)
                                    isOverdue -> Color(0xFFDC2626)
                                    daysRemaining <= 2 -> Color(0xFFD97706)
                                    else -> Color(0xFF059669)
                                }
                            )
                            Text(
                                text = deadlineDate,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = when {
                                    isCompleted -> Color(0xFF9CA3AF)
                                    isOverdue -> Color(0xFFDC2626)
                                    daysRemaining <= 2 -> Color(0xFFD97706)
                                    else -> Color(0xFF059669)
                                }
                            )
                        }
                    }
                }

                // Overdue warning
                if (isOverdue && !isCompleted) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Terlambat ${Math.abs(daysRemaining)} hari",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFDC2626)
                    )
                }
            }

            // Action buttons
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onToggleComplete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        if (isCompleted) Icons.Outlined.KeyboardArrowLeft else Icons.Outlined.CheckCircle,
                        contentDescription = if (isCompleted) "Batal selesai" else "Selesai",
                        tint = if (isCompleted) Color(0xFF6B7280) else Color(0xFF10B981),
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
                        tint = Color(0xFFD1D5DB),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    taskTitle: String,
    onTaskTitleChange: (String) -> Unit,
    taskCategory: String,
    onTaskCategoryChange: (String) -> Unit,
    taskPriority: Int,
    onTaskPriorityChange: (Int) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "Tugas Baru",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF111827)
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = taskTitle,
                    onValueChange = onTaskTitleChange,
                    label = { Text("Nama tugas") },
                    placeholder = { Text("Masukkan nama tugas...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Priority selection
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
                        ).forEach { (priority, label) ->
                            FilterChip(
                                selected = taskPriority == priority,
                                onClick = { onTaskPriorityChange(priority) },
                                label = {
                                    Text(
                                        text = label,
                                        fontSize = 13.sp
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF2563EB),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                // Category selection
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
                        listOf("Kuliah", "UKM", "Personal").forEach { category ->
                            FilterChip(
                                selected = taskCategory == category,
                                onClick = { onTaskCategoryChange(category) },
                                label = {
                                    Text(
                                        text = category,
                                        fontSize = 13.sp
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF2563EB),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = taskTitle.isNotBlank(),
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

private fun isTaskToday(deadline: Long): Boolean {
    val today = Calendar.getInstance()
    val taskDate = Calendar.getInstance().apply { timeInMillis = deadline }
    return today.get(Calendar.YEAR) == taskDate.get(Calendar.YEAR) &&
            today.get(Calendar.DAY_OF_YEAR) == taskDate.get(Calendar.DAY_OF_YEAR)
}

private fun isTaskThisWeek(deadline: Long): Boolean {
    val daysUntilDeadline = (deadline - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)
    return daysUntilDeadline in 0..7
}