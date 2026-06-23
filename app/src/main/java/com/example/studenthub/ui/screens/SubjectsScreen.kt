package com.example.studenthub.ui.screens

import StudentHubViewModel
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.studenthub.data.Subject
import java.util.*

// ── Subject-specific colors ──────────────────────────────────────────
private object SubjectColors {
    val gradientHeader = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6), Color(0xFFA855F7))
    val fabGradient = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
    val indigo = Color(0xFF6366F1)
    val violet = Color(0xFF8B5CF6)
    val deleteRed = Color(0xFFDC2626)
    val deleteRedBg = Color(0xFFFEF2F2)

    val presetColors = listOf(
        "#2563EB", "#6366F1", "#8B5CF6", "#EC4899",
        "#EF4444", "#F59E0B", "#10B981", "#06B6D4"
    )
}

// ══════════════════════════════════════════════════════════════════════
// ── MAIN SUBJECTS SCREEN ─────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════
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

    fun resetFormFields() {
        code = ""
        name = ""
        credits = "3"
        lecturer = ""
        roomLocation = ""
        scheduleDay = "Senin"
        scheduleTime = "08:00"
        color = "#2563EB"
    }

    val subjectsBySemester = remember(allSubjects, selectedSemester) {
        allSubjects.filter { it.semester == selectedSemester }.sortedBy { it.code }
    }

    val totalCredits = remember(subjectsBySemester) {
        subjectsBySemester.sumOf { it.credits }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = { SubjectGradientFab(onClick = { resetFormFields(); showAddDialog = true }) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // ── Header ───────────────────────────────────────────
            item { SubjectScreenHeader() }

            // ── Summary Card ─────────────────────────────────────
            item {
                GradientSummaryCard(
                    subjectCount = subjectsBySemester.size,
                    totalCredits = totalCredits,
                    activeDays = subjectsBySemester.map { it.scheduleDay }.distinct().size
                )
            }

            // ── Semester Selector ────────────────────────────────
            item {
                PremiumSemesterSelector(
                    selectedSemester = selectedSemester,
                    onSemesterSelected = { selectedSemester = it }
                )
            }

            // ── Count bar ────────────────────────────────────────
            if (subjectsBySemester.isNotEmpty()) {
                item {
                    SubjectCountBar(
                        count = subjectsBySemester.size,
                        totalCredits = totalCredits
                    )
                }
            }

            // ── Subject List ─────────────────────────────────────
            if (subjectsBySemester.isEmpty()) {
                item { PremiumEmptySubjectState() }
            } else {
                items(subjectsBySemester, key = { it.id }) { subject ->
                    PremiumSubjectCard(
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

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    // ── Dialog ───────────────────────────────────────────────────
    if (showAddDialog) {
        PremiumSubjectDialog(
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
                resetFormFields()
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
                    resetFormFields()
                }
            }
        )
    }
}

// ══════════════════════════════════════════════════════════════════════
// ── HEADER ───────────────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════
@Composable
private fun SubjectScreenHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Mata Kuliah",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Kelola jadwal & informasi perkuliahan",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            )
        }
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(
                    brush = Brush.linearGradient(SubjectColors.gradientHeader),
                    shape = RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.School,
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
private fun SubjectGradientFab(onClick: () -> Unit) {
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
            .shadow(
                elevation = 10.dp,
                shape = CircleShape,
                ambientColor = SubjectColors.indigo.copy(alpha = 0.3f),
                spotColor = SubjectColors.indigo.copy(alpha = 0.4f)
            )
            .background(
                brush = Brush.linearGradient(SubjectColors.fabGradient),
                shape = CircleShape
            )
    ) {
        Icon(
            Icons.Rounded.Add,
            contentDescription = "Tambah Mata Kuliah",
            modifier = Modifier.size(28.dp)
        )
    }
}

// ══════════════════════════════════════════════════════════════════════
// ── GRADIENT SUMMARY CARD ────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════
@Composable
private fun GradientSummaryCard(
    subjectCount: Int,
    totalCredits: Int,
    activeDays: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = SubjectColors.indigo.copy(alpha = 0.15f),
                spotColor = SubjectColors.indigo.copy(alpha = 0.2f)
            ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = SubjectColors.gradientHeader,
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
        ) {
            // Decorative circles
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .offset(x = 280.dp, y = (-20).dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.07f))
            )
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .offset(x = 240.dp, y = 50.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PremiumSummaryItem(
                    icon = Icons.Rounded.MenuBook,
                    value = subjectCount.toString(),
                    label = "Matkul"
                )
                // Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(56.dp)
                        .background(Color.White.copy(alpha = 0.2f))
                )
                PremiumSummaryItem(
                    icon = Icons.Rounded.Star,
                    value = totalCredits.toString(),
                    label = "Total SKS"
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(56.dp)
                        .background(Color.White.copy(alpha = 0.2f))
                )
                PremiumSummaryItem(
                    icon = Icons.Rounded.CalendarMonth,
                    value = activeDays.toString(),
                    label = "Hari Kuliah"
                )
            }
        }
    }
}

@Composable
public fun PremiumSummaryItem(
    icon: ImageVector,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    color = Color.White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = Color.White
            )
        }
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            letterSpacing = (-0.5).sp
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.75f),
            fontWeight = FontWeight.Medium
        )
    }
}

// ══════════════════════════════════════════════════════════════════════
// ── SEMESTER SELECTOR ────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════
@Composable
private fun PremiumSemesterSelector(
    selectedSemester: Int,
    onSemesterSelected: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(
                        color = SubjectColors.indigo.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = SubjectColors.indigo
                )
            }
            Text(
                text = "Semester",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = (-0.3).sp
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            (1..8).forEach { sem ->
                val isSelected = selectedSemester == sem

                Surface(
                    onClick = { onSemesterSelected(sem) },
                    shape = RoundedCornerShape(12.dp),
                    color = when {
                        isSelected -> SubjectColors.indigo
                        else -> MaterialTheme.colorScheme.surface
                    },
                    border = if (!isSelected) {
                        androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        )
                    } else null,
                    shadowElevation = if (isSelected) 3.dp else 0.dp
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "$sem",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White
                            else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Sem",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isSelected) Color.White.copy(alpha = 0.8f)
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
// ── COUNT BAR ────────────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════
@Composable
private fun SubjectCountBar(count: Int, totalCredits: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$count mata kuliah",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = SubjectColors.indigo.copy(alpha = 0.1f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    Icons.Rounded.Star,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = SubjectColors.indigo
                )
                Text(
                    text = "$totalCredits SKS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SubjectColors.indigo
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
// ── EMPTY STATE ──────────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════
@Composable
private fun PremiumEmptySubjectState() {
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
                        color = SubjectColors.indigo.copy(alpha = 0.08f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.MenuBook,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = SubjectColors.indigo.copy(alpha = 0.4f)
                )
            }
            Text(
                text = "Belum ada mata kuliah",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Tambahkan mata kuliah untuk\nsemester ini dengan tombol +",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                lineHeight = 19.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
// ── PREMIUM SUBJECT CARD ─────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════
@Composable
fun PremiumSubjectCard(
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
            Color(0xFF6366F1)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = subjectColor.copy(alpha = 0.06f),
                spotColor = subjectColor.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Gradient subject icon
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(
                            brush = Brush.linearGradient(
                                listOf(subjectColor, subjectColor.copy(alpha = 0.7f))
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = subject.code.take(2).uppercase(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    // Code + Credits chips
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = subjectColor.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = subject.code,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = subjectColor
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = "${subject.credits} SKS",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Subject name
                    Text(
                        text = subject.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 21.sp,
                        letterSpacing = (-0.2).sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Info pills
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Lecturer
                SubjectInfoRow(
                    icon = Icons.Rounded.Person,
                    text = subject.lecturer.ifBlank { "Belum ada dosen" },
                    accentColor = subjectColor
                )

                // Schedule & Room
                if (subject.scheduleDay.isNotBlank()) {
                    SubjectInfoRow(
                        icon = Icons.Rounded.Schedule,
                        text = buildString {
                            append(subject.scheduleDay)
                            if (subject.scheduleTime.isNotBlank()) append(", ${subject.scheduleTime}")
                        },
                        accentColor = subjectColor
                    )
                }

                if (subject.roomLocation.isNotBlank()) {
                    SubjectInfoRow(
                        icon = Icons.Rounded.LocationOn,
                        text = subject.roomLocation,
                        accentColor = subjectColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Divider
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                thickness = 1.dp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Edit button
                Surface(
                    onClick = onEdit,
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Edit",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Delete button
                Surface(
                    onClick = { showDeleteConfirmation = true },
                    shape = RoundedCornerShape(10.dp),
                    color = SubjectColors.deleteRedBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            Icons.Rounded.DeleteOutline,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = SubjectColors.deleteRed
                        )
                        Text(
                            text = "Hapus",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SubjectColors.deleteRed
                        )
                    }
                }
            }
        }
    }

    // Delete confirmation
    if (showDeleteConfirmation) {
        PremiumDeleteDialog(
            subjectName = subject.name,
            onConfirm = {
                onDelete()
                showDeleteConfirmation = false
            },
            onDismiss = { showDeleteConfirmation = false }
        )
    }
}

@Composable
private fun SubjectInfoRow(
    icon: ImageVector,
    text: String,
    accentColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .background(
                    color = accentColor.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(7.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = accentColor.copy(alpha = 0.7f)
            )
        }
        Text(
            text = text,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ══════════════════════════════════════════════════════════════════════
// ── DELETE DIALOG ────────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════
@Composable
private fun PremiumDeleteDialog(
    subjectName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        icon = {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        color = SubjectColors.deleteRedBg,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.DeleteOutline,
                    contentDescription = null,
                    modifier = Modifier.size(26.dp),
                    tint = SubjectColors.deleteRed
                )
            }
        },
        title = {
            Text(
                text = "Hapus Mata Kuliah?",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Text(
                text = "\"$subjectName\" akan dihapus secara permanen. Tindakan ini tidak bisa dibatalkan.",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                lineHeight = 20.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = SubjectColors.deleteRed),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Icon(
                    Icons.Rounded.DeleteOutline,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Hapus", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "Batal",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    )
}

// ══════════════════════════════════════════════════════════════════════
// ── PREMIUM SUBJECT DIALOG ───────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumSubjectDialog(
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

    val currentColor = remember(color) {
        try {
            val colorHex = color.removePrefix("#")
            Color(colorHex.toLong(16) or 0xFF000000.toLong())
        } catch (e: Exception) {
            Color(0xFF6366F1)
        }
    }

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
                        .size(42.dp)
                        .background(
                            brush = Brush.linearGradient(SubjectColors.fabGradient),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isEditing) Icons.Rounded.Edit else Icons.Rounded.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = if (isEditing) "Edit Mata Kuliah" else "Tambah Mata Kuliah",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = (-0.3).sp
                    )
                    Text(
                        text = if (isEditing) "Ubah informasi matkul" else "Isi detail mata kuliah",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Section: Identitas
                DialogSectionLabel("Identitas Matkul")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PremiumTextField(
                        value = code,
                        onValueChange = onCodeChange,
                        label = "Kode",
                        placeholder = "IF1234",
                        modifier = Modifier.weight(1f)
                    )
                    PremiumTextField(
                        value = credits,
                        onValueChange = onCreditsChange,
                        label = "SKS",
                        placeholder = "3",
                        modifier = Modifier.weight(0.5f),
                        keyboardType = KeyboardType.Number
                    )
                }

                PremiumTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = "Nama Mata Kuliah",
                    placeholder = "Pemrograman Mobile"
                )

                PremiumTextField(
                    value = lecturer,
                    onValueChange = onLecturerChange,
                    label = "Dosen Pengampu",
                    placeholder = "Nama dosen",
                    leadingIcon = Icons.Rounded.Person
                )

                // Section: Jadwal
                DialogSectionLabel("Jadwal Kuliah")

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
                        leadingIcon = {
                            Icon(
                                Icons.Rounded.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            focusedBorderColor = SubjectColors.indigo
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expandedDayDropdown,
                        onDismissRequest = { expandedDayDropdown = false }
                    ) {
                        days.forEach { day ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        day,
                                        fontWeight = if (day == scheduleDay) FontWeight.Bold else FontWeight.Normal,
                                        color = if (day == scheduleDay) SubjectColors.indigo
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    onScheduleDayChange(day)
                                    expandedDayDropdown = false
                                },
                                leadingIcon = {
                                    if (day == scheduleDay) {
                                        Icon(
                                            Icons.Rounded.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = SubjectColors.indigo
                                        )
                                    }
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PremiumTextField(
                        value = scheduleTime,
                        onValueChange = onScheduleTimeChange,
                        label = "Waktu",
                        placeholder = "08:00",
                        modifier = Modifier.weight(0.55f),
                        leadingIcon = Icons.Rounded.Schedule
                    )
                    PremiumTextField(
                        value = roomLocation,
                        onValueChange = onRoomLocationChange,
                        label = "Ruangan",
                        placeholder = "Ruang 4.2",
                        modifier = Modifier.weight(0.45f),
                        leadingIcon = Icons.Rounded.LocationOn
                    )
                }

                // Section: Warna
                DialogSectionLabel("Warna Identitas")

                // Color presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SubjectColors.presetColors.forEach { preset ->
                        val presetColor = try {
                            val hex = preset.removePrefix("#")
                            Color(hex.toLong(16) or 0xFF000000.toLong())
                        } catch (e: Exception) {
                            Color(0xFF6366F1)
                        }
                        val isSelected = color.equals(preset, ignoreCase = true)

                        Surface(
                            onClick = { onColorChange(preset) },
                            shape = CircleShape,
                            color = presetColor,
                            modifier = Modifier
                                .size(32.dp)
                                .then(
                                    if (isSelected) Modifier.border(
                                        width = 2.5.dp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        shape = CircleShape
                                    ) else Modifier
                                )
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Rounded.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                // Custom color input
                OutlinedTextField(
                    value = color,
                    onValueChange = {
                        if (it.startsWith("#") && it.length <= 7) {
                            onColorChange(it)
                        } else if (!it.startsWith("#")) {
                            onColorChange("#$it")
                        }
                    },
                    label = { Text("Kode Warna (Hex)") },
                    placeholder = { Text("#6366F1") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(currentColor)
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        focusedBorderColor = SubjectColors.indigo
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = code.isNotBlank() && name.isNotBlank() && credits.isNotBlank(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SubjectColors.indigo
                ),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Icon(
                    if (isEditing) Icons.Rounded.Check else Icons.Rounded.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isEditing) "Simpan" else "Tambah",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "Batal",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    )
}

// ── Dialog helpers ───────────────────────────────────────────────────
@Composable
public fun DialogSectionLabel(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(16.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(SubjectColors.indigo)
        )
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            letterSpacing = (-0.2).sp
        )
    }
}

@Composable
public fun PremiumTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = {
            Text(
                placeholder,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
        },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        leadingIcon = leadingIcon?.let {
            {
                Icon(
                    it,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            focusedBorderColor = SubjectColors.indigo,
            focusedLabelColor = SubjectColors.indigo,
            cursorColor = SubjectColors.indigo
        )
    )
}