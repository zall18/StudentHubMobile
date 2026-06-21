package com.example.studenthub.ui.screens

import StudentHubViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.studenthub.data.Subject

@Composable
fun SubjectsScreen(viewModel: StudentHubViewModel) {
    val allSubjects by viewModel.allSubjects.collectAsStateWithLifecycle()
    
    var selectedSemester by remember { mutableStateOf(1) }
    var showAddDialog by remember { mutableStateOf(false) }
    
    // Form fields
    var code by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var credits by remember { mutableStateOf("3") }
    var lecturer by remember { mutableStateOf("") }
    var roomLocation by remember { mutableStateOf("") }
    var scheduleDay by remember { mutableStateOf("Monday") }
    var scheduleTime by remember { mutableStateOf("08:00") }
    var color by remember { mutableStateOf("#0066FF") }

    val subjectsBySemester = allSubjects.filter { it.semester == selectedSemester }
        .sortedBy { it.code }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF0066FF),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Matkul")
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
                    text = "Mata Kuliah",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
            }

            // Semester Selector
            item {
                Text(
                    text = "Pilih Semester:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (sem in 1..8) {
                        FilterChip(
                            selected = selectedSemester == sem,
                            onClick = { selectedSemester = sem },
                            label = { Text("$sem", fontSize = 12.sp) }
                        )
                    }
                }
            }

            // Subject List
            if (subjectsBySemester.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Text(
                            text = "Belum ada mata kuliah di semester ini",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                items(subjectsBySemester, key = { it.id }) { subject ->
                    SubjectCard(
                        subject = subject,
                        onEdit = {
                            // TODO: Implement edit
                        },
                        onDelete = { viewModel.deleteSubject(subject.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Add Subject Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                code = ""
                name = ""
                credits = "3"
                lecturer = ""
                roomLocation = ""
                scheduleDay = "Monday"
                scheduleTime = "08:00"
                color = "#0066FF"
            },
            title = { Text("Tambah Mata Kuliah") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Kode Matkul") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 1
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nama Mata Kuliah") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 1
                    )

                    OutlinedTextField(
                        value = credits,
                        onValueChange = { credits = it },
                        label = { Text("SKS") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 1
                    )

                    OutlinedTextField(
                        value = lecturer,
                        onValueChange = { lecturer = it },
                        label = { Text("Dosen") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 1
                    )

                    OutlinedTextField(
                        value = roomLocation,
                        onValueChange = { roomLocation = it },
                        label = { Text("Lokasi Ruangan") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 1
                    )

                    OutlinedTextField(
                        value = scheduleDay,
                        onValueChange = { scheduleDay = it },
                        label = { Text("Hari") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 1
                    )

                    OutlinedTextField(
                        value = scheduleTime,
                        onValueChange = { scheduleTime = it },
                        label = { Text("Waktu (HH:mm)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 1
                    )

                    OutlinedTextField(
                        value = color,
                        onValueChange = { color = it },
                        label = { Text("Warna (hex)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 1
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (code.isNotBlank() && name.isNotBlank()) {
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
                            showAddDialog = false
                            code = ""
                            name = ""
                            credits = "3"
                            lecturer = ""
                            roomLocation = ""
                            scheduleDay = "Monday"
                            scheduleTime = "08:00"
                            color = "#0066FF"
                        }
                    }
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddDialog = false
                    code = ""
                    name = ""
                    credits = "3"
                    lecturer = ""
                    roomLocation = ""
                    scheduleDay = "Monday"
                    scheduleTime = "08:00"
                    color = "#0066FF"
                }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun rememberScrollState(): androidx.compose.foundation.ScrollState {
    return androidx.compose.foundation.rememberScrollState()
}

@Composable
fun SubjectCard(
    subject: Subject,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
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
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = try {
                                Color(subject.color.ifBlank { "#0066FF" }.toLong(16) or 0xFF000000)
                            } catch (e: Exception) {
                                Color(0xFF0066FF)
                            }
                        ) {
                            Text(
                                text = subject.code,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Text(
                            text = "${subject.credits} SKS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray,
                            modifier = Modifier.align(androidx.compose.ui.Alignment.CenterVertically)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = subject.name,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Color(0xFF1A1A1A)
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Dosen: ${subject.lecturer}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color(0xFF1E88E5),
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

            if (subject.scheduleDay.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "📅 ${subject.scheduleDay} • ${subject.scheduleTime} • ${subject.roomLocation}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
