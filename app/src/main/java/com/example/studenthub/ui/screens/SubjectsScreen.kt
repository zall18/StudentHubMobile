package com.example.studenthub.ui.screens

import StudentHubViewModel
import androidx.compose.animation.*
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.studenthub.data.Subject
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectsScreen(viewModel: StudentHubViewModel) {
    val allSubjects by viewModel.allSubjects.collectAsStateWithLifecycle()

    var selectedSemester by remember { mutableStateOf(1) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingSubject by remember { mutableStateOf<Subject?>(null) }

    // Form fields
    var code by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var credits by remember { mutableStateOf("3") }
    var lecturer by remember { mutableStateOf("") }
    var roomLocation by remember { mutableStateOf("") }
    var scheduleDay by remember { mutableStateOf("Senin") }
    var scheduleTime by remember { mutableStateOf("08:00") }
    var color by remember { mutableStateOf("#2563EB") }

    val subjectsBySemester = remember(allSubjects, selectedSemester) {
        allSubjects.filter { it.semester == selectedSemester }
            .sortedBy { it.code }
    }

    // Stats
    val totalCredits = remember(subjectsBySemester) {
        subjectsBySemester.sumOf { it.credits }
    }

    Scaffold(
        containerColor = Color(0xFFF8F9FA),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    resetForm()
                    showAddDialog = true
                },
                containerColor = Color(0xFF2563EB),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Tambah Mata Kuliah",
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
                        text = "Mata Kuliah",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Kelola jadwal kuliah kamu",
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }

            // Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2563EB)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        SummaryItem(
                            icon = Icons.Outlined.MenuBook,
                            value = subjectsBySemester.size.toString(),
                            label = "Matkul"
                        )
                        SummaryItem(
                            icon = Icons.Outlined.School,
                            value = totalCredits.toString(),
                            label = "Total SKS"
                        )
                        SummaryItem(
                            icon = Icons.Outlined.CalendarMonth,
                            value = subjectsBySemester.map { it.scheduleDay }.distinct().size.toString(),
                            label = "Hari Kuliah"
                        )
                    }
                }
            }

            // Semester Selector
            item {
                Column {
                    Text(
                        text = "Semester",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF111827)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        (1..8).forEach { sem ->
                            FilterChip(
                                selected = selectedSemester == sem,
                                onClick = { selectedSemester = sem },
                                label = {
                                    Text(
                                        text = "Sem $sem",
                                        fontSize = 13.sp,
                                        fontWeight = if (selectedSemester == sem) FontWeight.Medium else FontWeight.Normal
                                    )
                                },
                                leadingIcon = if (selectedSemester == sem) {
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
            }

            // Subjects count
            item {
                if (subjectsBySemester.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${subjectsBySemester.size} mata kuliah",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF374151)
                        )

                        // Schedule overview
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color(0xFF6B7280)
                            )
                            Text(
                                text = "$totalCredits SKS",
                                fontSize = 13.sp,
                                color = Color(0xFF6B7280),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Subject List
            if (subjectsBySemester.isEmpty()) {
                item {
                    EmptySubjectState()
                }
            } else {
                items(subjectsBySemester, key = { it.id }) { subject ->
                    EnhancedSubjectCard(
                        subject = subject,
                        onEdit = {
                            editingSubject = subject
                            code = subject.code
                            name = subject.name
                            credits = subject.credits.toString()
                            lecturer = subject.lecturer
                            roomLocation = subject.roomLocation
                            scheduleDay = subject.scheduleDay
                            scheduleTime = subject.scheduleTime
                            color = subject.color
                            showAddDialog = true
                        },
                        onDelete = { viewModel.deleteSubject(subject.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Add/Edit Subject Dialog
    if (showAddDialog) {
        SubjectDialog(
            isEditing = editingSubject != null,
            code = code,
            onCodeChange = { code = it },
            name = name,
            onNameChange = { name = it },
            credits = credits,
            onCreditsChange = { credits = it },
            lecturer = lecturer,
            onLecturerChange = { lecturer = it },
            roomLocation = roomLocation,
            onRoomLocationChange = { roomLocation = it },
            scheduleDay = scheduleDay,
            onScheduleDayChange = { scheduleDay = it },
            scheduleTime = scheduleTime,
            onScheduleTimeChange = { scheduleTime = it },
            color = color,
            onColorChange = { color = it },
            onDismiss = {
                showAddDialog = false
                editingSubject = null
                resetForm()
            },
            onConfirm = {
                if (code.isNotBlank() && name.isNotBlank()) {
                    if (editingSubject != null) {
                        val updatedSubject = Subject(
                            id = editingSubject!!.id,
                            code = code,
                            name = name,
                            credits = credits.toIntOrNull() ?: 3,
                            semester = selectedSemester,
                            lecturer = lecturer,
                            roomLocation = roomLocation,
                            scheduleDay = scheduleDay,
                            scheduleTime = scheduleTime,
                            color = color
                        )
                        viewModel.updateSubject(updatedSubject)
                    } else {
                        viewModel.addSubject(
                            code = code,
                            name = name,
                            credits = credits.toIntOrNull() ?: 3,
                            semester = selectedSemester,
                            lecturer = lecturer,
                            roomLocation = roomLocation,
                            scheduleDay = scheduleDay,
                            scheduleTime = scheduleTime,
                            color = color
                        )
                    }
                    showAddDialog = false
                    editingSubject = null
                    resetForm()
                }
            }
        )
    }
}

@Composable
fun SummaryItem(
    icon: ImageVector,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = Color.White.copy(alpha = 0.8f)
        )
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun EmptySubjectState() {
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
                Icons.Outlined.MenuBook,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color(0xFFD1D5DB)
            )
            Text(
                text = "Belum ada mata kuliah",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF6B7280)
            )
            Text(
                text = "Tambahkan mata kuliah untuk semester ini",
                fontSize = 13.sp,
                color = Color(0xFF9CA3AF),
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun EnhancedSubjectCard(
    subject: Subject,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    val subjectColor = remember(subject.color) {
        try {
            val colorHex = subject.color.removePrefix("#")
            Color(colorHex.toLong(16) or 0xFF000000.toLong())
        } catch (e: Exception) {
            Color(0xFF2563EB)
        }
    }

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
                verticalAlignment = Alignment.Top
            ) {
                // Subject color icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(subjectColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = subject.code.take(2).uppercase(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    // Code & Credits
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = subjectColor.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = subject.code,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = subjectColor
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFF3F4F6)
                        ) {
                            Text(
                                text = "${subject.credits} SKS",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF6B7280)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Subject name
                    Text(
                        text = subject.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF111827),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Lecturer
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
                            text = subject.lecturer.ifBlank { "Belum ada dosen" },
                            fontSize = 13.sp,
                            color = Color(0xFF6B7280)
                        )
                    }

                    // Schedule & Room
                    if (subject.scheduleDay.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color(0xFF9CA3AF)
                            )
                            Text(
                                text = buildString {
                                    append(subject.scheduleDay)
                                    if (subject.scheduleTime.isNotBlank()) {
                                        append(", ${subject.scheduleTime}")
                                    }
                                    if (subject.roomLocation.isNotBlank()) {
                                        append(" • ${subject.roomLocation}")
                                    }
                                },
                                fontSize = 13.sp,
                                color = Color(0xFF6B7280)
                            )
                        }
                    }
                }
            }

            // Action buttons
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onEdit,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(
                        Icons.Outlined.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Edit",
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                TextButton(
                    onClick = { showDeleteConfirmation = true },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFFDC2626)
                    )
                ) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Hapus",
                        fontSize = 13.sp
                    )
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    text = "Hapus Mata Kuliah",
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text("Apakah kamu yakin ingin menghapus ${subject.name}?")
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
fun SubjectDialog(
    isEditing: Boolean,
    code: String,
    onCodeChange: (String) -> Unit,
    name: String,
    onNameChange: (String) -> Unit,
    credits: String,
    onCreditsChange: (String) -> Unit,
    lecturer: String,
    onLecturerChange: (String) -> Unit,
    roomLocation: String,
    onRoomLocationChange: (String) -> Unit,
    scheduleDay: String,
    onScheduleDayChange: (String) -> Unit,
    scheduleTime: String,
    onScheduleTimeChange: (String) -> Unit,
    color: String,
    onColorChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val days = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu")
    var expandedDayDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = if (isEditing) "Edit Mata Kuliah" else "Tambah Mata Kuliah",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF111827)
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Code & Credits in one row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = onCodeChange,
                        label = { Text("Kode") },
                        placeholder = { Text("IF1234") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = credits,
                        onValueChange = onCreditsChange,
                        label = { Text("SKS") },
                        placeholder = { Text("3") },
                        modifier = Modifier.weight(0.5f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Subject name
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Nama Mata Kuliah") },
                    placeholder = { Text("Pemrograman Mobile") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Lecturer
                OutlinedTextField(
                    value = lecturer,
                    onValueChange = onLecturerChange,
                    label = { Text("Dosen Pengampu") },
                    placeholder = { Text("Nama dosen") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Schedule
                Text(
                    text = "Jadwal Kuliah",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF374151)
                )

                // Day dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedDayDropdown,
                    onExpandedChange = { expandedDayDropdown = it }
                ) {
                    OutlinedTextField(
                        value = scheduleDay,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Hari") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDayDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedDayDropdown,
                        onDismissRequest = { expandedDayDropdown = false }
                    ) {
                        days.forEach { day ->
                            DropdownMenuItem(
                                text = { Text(day) },
                                onClick = {
                                    onScheduleDayChange(day)
                                    expandedDayDropdown = false
                                }
                            )
                        }
                    }
                }

                // Time & Room
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = scheduleTime,
                        onValueChange = onScheduleTimeChange,
                        label = { Text("Waktu") },
                        placeholder = { Text("08:00") },
                        modifier = Modifier.weight(0.6f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = roomLocation,
                        onValueChange = onRoomLocationChange,
                        label = { Text("Ruangan") },
                        placeholder = { Text("Ruang 4.2") },
                        modifier = Modifier.weight(0.4f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Color
                OutlinedTextField(
                    value = color,
                    onValueChange = {
                        if (it.startsWith("#") && it.length <= 7) {
                            onColorChange(it)
                        } else if (!it.startsWith("#")) {
                            onColorChange("#$it")
                        }
                    },
                    label = { Text("Warna (Hex)") },
                    placeholder = { Text("#2563EB") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(
                                    try {
                                        val colorHex = color.removePrefix("#")
                                        Color(colorHex.toLong(16) or 0xFF000000.toLong())
                                    } catch (e: Exception) {
                                        Color(0xFF2563EB)
                                    }
                                )
                        )
                    },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = code.isNotBlank() && name.isNotBlank() && credits.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2563EB)
                )
            ) {
                Text(
                    text = if (isEditing) "Simpan" else "Tambah",
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

// Helper function to reset form
private fun resetForm() {
    // This function is used as reference only, actual reset is done with remember states
}

@Composable
private fun rememberScrollState(): ScrollState {
    return rememberScrollState()
}