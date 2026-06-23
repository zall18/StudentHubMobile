package com.example.studenthub.ui.screens

import StudentHubViewModel
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.studenthub.data.Task
import java.text.SimpleDateFormat
import java.util.*

// ── Task-specific colors not covered by theme ────────────────────────
private object TaskScreenColors {
    // Priority colors
    val priorityHigh = Color(0xFFEF4444)
    val priorityHighBg = Color(0xFFFEF2F2)
    val priorityMedium = Color(0xFFF59E0B)
    val priorityMediumBg = Color(0xFFFFFBEB)
    val priorityLow = Color(0xFF10B981)
    val priorityLowBg = Color(0xFFECFDF5)

    // Status colors
    val overdue = Color(0xFFDC2626)
    val overdueBg = Color(0xFFFFF5F5)
    val overdueBorder = Color(0xFFFECACA)
    val warning = Color(0xFFD97706)
    val success = Color(0xFF059669)

    // Gradient
    val fabGradient = listOf(Color(0xFF3B82F6), Color(0xFF6366F1))
    val headerGradient = listOf(Color(0xFF3B82F6), Color(0xFF6366F1), Color(0xFF8B5CF6))

    // Accent shades (for things theme doesn't cover)
    val indigo = Color(0xFF6366F1)
    val indigoBg = Color(0xFFEEF2FF)
}

// ══════════════════════════════════════════════════════════════════════
// ── MAIN TASKS SCREEN ────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════
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
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = { TaskGradientFab(onClick = { showAddDialog = true }) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Header ───────────────────────────────────────────
            item { TaskScreenHeader(activeCount = activeTasks.size, completedCount = completedTasks.size) }

            // ── Search ───────────────────────────────────────────
            item {
                PremiumSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onClear = { searchQuery = "" }
                )
            }

            // ── Filters + Sort ───────────────────────────────────
            item {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    PremiumFilterRow(
                        selectedFilter = selectedFilter,
                        onFilterSelected = { selectedFilter = it }
                    )
                    PremiumSortRow(
                        selectedSort = selectedSort,
                        onSortSelected = { selectedSort = it }
                    )
                }
            }

            // ── Result count + stats ─────────────────────────────
            item {
                TaskResultBar(
                    count = sortedTasks.size,
                    showStats = selectedFilter == "ALL",
                    overdueCount = activeTasks.count { it.deadline < System.currentTimeMillis() },
                    todayCount = activeTasks.count { isTaskToday(it.deadline) }
                )
            }

            // ── Task list ────────────────────────────────────────
            if (sortedTasks.isEmpty()) {
                item { PremiumEmptyState() }
            } else {
                items(sortedTasks, key = { it.id }) { task ->
                    PremiumTaskListCard(
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

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    // ── Dialog ───────────────────────────────────────────────────
    if (showAddDialog) {
        PremiumAddTaskDialog(
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

// ══════════════════════════════════════════════════════════════════════
// ── HEADER ───────────────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════
@Composable
fun TaskScreenHeader(activeCount: Int, completedCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Daftar Tugas",
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
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "$activeCount aktif",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = "•",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$completedCount selesai",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Header icon
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(
                    brush = Brush.linearGradient(TaskScreenColors.headerGradient),
                    shape = RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.Assignment,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
// ── GRADIENT FAB ─────────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════
@Composable
fun TaskGradientFab(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        shape = CircleShape,
        containerColor = Color.Transparent,
        contentColor = Color.White,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 8.dp,
            pressedElevation = 12.dp
        ),
        modifier = Modifier
            .background(
                brush = Brush.linearGradient(TaskScreenColors.fabGradient),
                shape = CircleShape
            )
    ) {
        Icon(
            Icons.Rounded.Add,
            contentDescription = "Tambah Tugas",
            modifier = Modifier.size(28.dp)
        )
    }
}

// ══════════════════════════════════════════════════════════════════════
// ── SEARCH BAR ───────────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════
@Composable
fun PremiumSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                "Cari tugas, kategori...",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        },
        leadingIcon = {
            Icon(
                Icons.Rounded.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(22.dp)
            )
        },
        trailingIcon = {
            if (query.isNotBlank()) {
                IconButton(onClick = onClear) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = "Clear",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            cursorColor = MaterialTheme.colorScheme.primary
        )
    )
}

// ══════════════════════════════════════════════════════════════════════
// ── FILTER ROW ───────────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════
@Composable
fun PremiumFilterRow(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    data class FilterDef(
        val key: String,
        val label: String,
        val icon: ImageVector
    )

    val filters = listOf(
        FilterDef("ALL", "Semua", Icons.Rounded.ViewList),
        FilterDef("TODAY", "Hari Ini", Icons.Rounded.Today),
        FilterDef("WEEK", "Minggu Ini", Icons.Rounded.DateRange),
        FilterDef("HIGH_PRIORITY", "Prioritas", Icons.Rounded.PriorityHigh),
        FilterDef("OVERDUE", "Terlambat", Icons.Rounded.Warning),
        FilterDef("COMPLETED", "Selesai", Icons.Rounded.CheckCircle)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.forEach { filter ->
            val isSelected = selectedFilter == filter.key

            Surface(
                onClick = { onFilterSelected(filter.key) },
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surface,
                border = if (!isSelected)
                    androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    )
                else null,
                shadowElevation = if (isSelected) 2.dp else 0.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        filter.icon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (isSelected)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Text(
                        text = filter.label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (isSelected)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
// ── SORT ROW ─────────────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════
@Composable
fun PremiumSortRow(
    selectedSort: String,
    onSortSelected: (String) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(6.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.SwapVert,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = "Urut:",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )

        listOf(
            "DEADLINE" to "Deadline",
            "PRIORITY" to "Prioritas",
            "CATEGORY" to "Kategori",
            "TITLE" to "Judul"
        ).forEach { (sortKey, sortLabel) ->
            val isSelected = selectedSort == sortKey

            Surface(
                onClick = { onSortSelected(sortKey) },
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    Color.Transparent
            ) {
                Text(
                    text = sortLabel,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
// ── RESULT BAR ───────────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════
@Composable
fun TaskResultBar(
    count: Int,
    showStats: Boolean,
    overdueCount: Int,
    todayCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$count tugas ditemukan",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        if (showStats) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Overdue badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = TaskScreenColors.priorityHighBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = TaskScreenColors.overdue
                        )
                        Text(
                            text = "$overdueCount",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TaskScreenColors.overdue
                        )
                    }
                }

                // Today badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = TaskScreenColors.priorityMediumBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Today,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = TaskScreenColors.warning
                        )
                        Text(
                            text = "$todayCount",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TaskScreenColors.warning
                        )
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
fun PremiumEmptyState() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Inbox,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            }
            Text(
                text = "Tidak ada tugas",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Semua tugas sudah selesai atau\nbelum ada tugas yang ditambahkan",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                lineHeight = 19.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
// ── PREMIUM TASK LIST CARD ───────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════
@Composable
fun PremiumTaskListCard(
    task: Task,
    isCompleted: Boolean,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("EEE, dd MMM", Locale("id", "ID"))
    val deadlineDate = dateFormat.format(Date(task.deadline))
    val isOverdue = task.deadline < System.currentTimeMillis() && !isCompleted
    val daysRemaining = ((task.deadline - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)).toInt()

    val priorityColor = when {
        isCompleted -> MaterialTheme.colorScheme.outlineVariant
        task.priority == 3 -> TaskScreenColors.priorityHigh
        task.priority == 2 -> TaskScreenColors.priorityMedium
        else -> TaskScreenColors.priorityLow
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isOverdue) 0.dp else 3.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = if (isOverdue)
                    TaskScreenColors.overdue.copy(alpha = 0.06f)
                else
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.04f),
                spotColor = if (isOverdue)
                    TaskScreenColors.overdue.copy(alpha = 0.08f)
                else
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOverdue)
                TaskScreenColors.overdueBg
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = if (isOverdue) {
            androidx.compose.foundation.BorderStroke(1.dp, TaskScreenColors.overdueBorder)
        } else null
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Gradient priority strip
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .heightIn(min = 80.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(priorityColor, priorityColor.copy(alpha = 0.3f))
                        ),
                        shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                    )
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        // Title
                        Text(
                            text = task.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isCompleted)
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            else
                                MaterialTheme.colorScheme.onSurface,
                            textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 21.sp
                        )

                        // Description
                        if (task.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = task.description,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    // Action buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        IconButton(
                            onClick = onToggleComplete,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .background(
                                        color = if (isCompleted)
                                            MaterialTheme.colorScheme.surfaceVariant
                                        else
                                            TaskScreenColors.priorityLowBg,
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    if (isCompleted)
                                        Icons.Rounded.Undo
                                    else
                                        Icons.Rounded.Check,
                                    contentDescription = if (isCompleted) "Batal selesai" else "Selesai",
                                    tint = if (isCompleted)
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    else
                                        TaskScreenColors.priorityLow,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Outlined.Delete,
                                    contentDescription = "Hapus",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Chips row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category chip
                    TaskChip(
                        label = task.category,
                        bgColor = if (isCompleted)
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        else
                            TaskScreenColors.indigoBg,
                        textColor = if (isCompleted)
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        else
                            TaskScreenColors.indigo
                    )

                    // Priority chip (active only)
                    if (!isCompleted) {
                        val (prBg, prFg, prLabel) = when (task.priority) {
                            3 -> Triple(TaskScreenColors.priorityHighBg, TaskScreenColors.priorityHigh, "Tinggi")
                            2 -> Triple(TaskScreenColors.priorityMediumBg, TaskScreenColors.priorityMedium, "Sedang")
                            else -> Triple(TaskScreenColors.priorityLowBg, TaskScreenColors.success, "Rendah")
                        }
                        TaskChipWithDot(label = prLabel, bgColor = prBg, dotColor = prFg, textColor = prFg)
                    }

                    // Deadline chip
                    val (dlBg, dlFg) = when {
                        isCompleted -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) to
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        isOverdue -> TaskScreenColors.priorityHighBg to TaskScreenColors.overdue
                        daysRemaining <= 2 -> TaskScreenColors.priorityMediumBg to TaskScreenColors.warning
                        else -> TaskScreenColors.priorityLowBg to TaskScreenColors.success
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = dlBg
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (isOverdue) Icons.Rounded.Warning else Icons.Rounded.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = dlFg
                            )
                            Text(
                                text = deadlineDate,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = dlFg
                            )
                        }
                    }
                }

                // Overdue warning
                if (isOverdue) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = TaskScreenColors.priorityHighBg
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(TaskScreenColors.overdue)
                            )
                            Text(
                                text = "Terlambat ${Math.abs(daysRemaining)} hari",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TaskScreenColors.overdue
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Chip helpers ─────────────────────────────────────────────────────
@Composable
fun TaskChip(label: String, bgColor: Color, textColor: Color) {
    Surface(shape = RoundedCornerShape(8.dp), color = bgColor) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

@Composable
fun TaskChipWithDot(label: String, bgColor: Color, dotColor: Color, textColor: Color) {
    Surface(shape = RoundedCornerShape(8.dp), color = bgColor) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
// ── PREMIUM ADD TASK DIALOG ──────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumAddTaskDialog(
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
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            brush = Brush.linearGradient(TaskScreenColors.fabGradient),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = "Tugas Baru",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = (-0.3).sp
                    )
                    Text(
                        text = "Tambahkan tugas ke daftar",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                // Title field
                OutlinedTextField(
                    value = taskTitle,
                    onValueChange = onTaskTitleChange,
                    label = { Text("Nama tugas") },
                    placeholder = { Text("Masukkan nama tugas...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )

                // Priority selection
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Prioritas",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        data class PriorityOption(val value: Int, val label: String, val color: Color, val bg: Color)
                        listOf(
                            PriorityOption(1, "Rendah", TaskScreenColors.priorityLow, TaskScreenColors.priorityLowBg),
                            PriorityOption(2, "Sedang", TaskScreenColors.priorityMedium, TaskScreenColors.priorityMediumBg),
                            PriorityOption(3, "Tinggi", TaskScreenColors.priorityHigh, TaskScreenColors.priorityHighBg)
                        ).forEach { option ->
                            val isSelected = taskPriority == option.value

                            Surface(
                                onClick = { onTaskPriorityChange(option.value) },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) option.bg else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                border = if (isSelected)
                                    androidx.compose.foundation.BorderStroke(1.5.dp, option.color.copy(alpha = 0.5f))
                                else null,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) option.color else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                                    )
                                    Text(
                                        text = option.label,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) option.color else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Category selection
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Kategori",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        data class CatOption(val key: String, val icon: ImageVector)
                        listOf(
                            CatOption("Kuliah", Icons.Rounded.School),
                            CatOption("UKM", Icons.Rounded.Groups),
                            CatOption("Personal", Icons.Rounded.Person)
                        ).forEach { cat ->
                            val isSel = taskCategory == cat.key

                            Surface(
                                onClick = { onTaskCategoryChange(cat.key) },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSel)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                border = if (isSel)
                                    androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                                else null,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        cat.icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = if (isSel)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                    )
                                    Text(
                                        text = cat.key,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSel)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = taskTitle.isNotBlank(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Icon(
                    Icons.Rounded.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Simpan",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Batal",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    )
}

// ── Helpers ──────────────────────────────────────────────────────────
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