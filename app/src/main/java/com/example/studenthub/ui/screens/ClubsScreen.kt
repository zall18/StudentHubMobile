package com.example.studenthub.ui.screens

import StudentHubViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.studenthub.data.Club

@Composable
fun ClubsScreen(viewModel: StudentHubViewModel) {
    val allClubs by viewModel.allClubs.collectAsStateWithLifecycle()
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }
    var showAddDialog by remember { mutableStateOf(false) }
    
    // Form fields
    var clubName by remember { mutableStateOf("") }
    var clubDescription by remember { mutableStateOf("") }
    var clubCategory by remember { mutableStateOf("Tech") }
    var leaderName by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    var meetingDay by remember { mutableStateOf("") }
    var meetingLocation by remember { mutableStateOf("") }
    var clubColor by remember { mutableStateOf("#FF6B6B") }

    val filteredClubs = allClubs.filter { club ->
        val matchesSearch = club.name.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategory == "ALL" || club.category == selectedCategory
        matchesSearch && matchesCategory
    }.sortedBy { it.name }

    val categories = listOf("ALL", "Tech", "Sports", "Arts", "Social", "Academic")

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF0066FF),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah UKM")
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
                    text = "Organisasi & UKM",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
            }

            // Search & Filter
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Cari UKM...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1,
                    singleLine = true
                )
            }

            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories.size) { index ->
                        FilterChip(
                            selected = selectedCategory == categories[index],
                            onClick = { selectedCategory = categories[index] },
                            label = { Text(categories[index], fontSize = 12.sp) }
                        )
                    }
                }
            }

            // Club List
            if (filteredClubs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Text(
                            text = "Belum ada UKM",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                items(filteredClubs, key = { it.id }) { club ->
                    ClubCard(
                        club = club,
                        onEdit = {},
                        onDelete = { viewModel.deleteClub(club.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Add Club Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                clubName = ""
                clubDescription = ""
                clubCategory = "Tech"
                leaderName = ""
                contactPhone = ""
                meetingDay = ""
                meetingLocation = ""
                clubColor = "#FF6B6B"
            },
            title = { Text("Tambah UKM Baru") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = clubName,
                        onValueChange = { clubName = it },
                        label = { Text("Nama UKM") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 1
                    )

                    OutlinedTextField(
                        value = clubDescription,
                        onValueChange = { clubDescription = it },
                        label = { Text("Deskripsi") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 3
                    )

                    OutlinedTextField(
                        value = clubCategory,
                        onValueChange = { clubCategory = it },
                        label = { Text("Kategori") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 1
                    )

                    OutlinedTextField(
                        value = leaderName,
                        onValueChange = { leaderName = it },
                        label = { Text("Ketua") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 1
                    )

                    OutlinedTextField(
                        value = contactPhone,
                        onValueChange = { contactPhone = it },
                        label = { Text("Nomor Kontak") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 1
                    )

                    OutlinedTextField(
                        value = meetingDay,
                        onValueChange = { meetingDay = it },
                        label = { Text("Hari Pertemuan") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 1
                    )

                    OutlinedTextField(
                        value = meetingLocation,
                        onValueChange = { meetingLocation = it },
                        label = { Text("Lokasi Pertemuan") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 1
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (clubName.isNotBlank()) {
                            viewModel.addClub(
                                name = clubName,
                                description = clubDescription,
                                category = clubCategory,
                                leaderName = leaderName,
                                contactPhone = contactPhone,
                                meetingDay = meetingDay,
                                meetingLocation = meetingLocation,
                                color = clubColor
                            )
                            showAddDialog = false
                            clubName = ""
                            clubDescription = ""
                            clubCategory = "Tech"
                            leaderName = ""
                            contactPhone = ""
                            meetingDay = ""
                            meetingLocation = ""
                            clubColor = "#FF6B6B"
                        }
                    }
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddDialog = false
                    clubName = ""
                    clubDescription = ""
                }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun ClubCard(
    club: Club,
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
                        Text(
                            text = club.name,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = Color(0xFF1A1A1A),
                            modifier = Modifier.weight(1f)
                        )

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFE3F2FD)
                        ) {
                            Text(
                                text = club.category,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                color = Color(0xFF1E88E5),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    if (club.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = club.description,
                            fontSize = 12.sp,
                            color = Color.Gray,
                            maxLines = 2
                        )
                    }
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

            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (club.leaderName.isNotBlank()) {
                    Text(
                        text = "👥 Ketua: ${club.leaderName}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                if (club.contactPhone.isNotBlank()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(
                            Icons.Default.Call,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = Color.Gray
                        )
                        Text(
                            text = club.contactPhone,
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                if (club.meetingDay.isNotBlank() && club.meetingLocation.isNotBlank()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = Color.Gray
                        )
                        Text(
                            text = "${club.meetingDay} • ${club.meetingLocation}",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}
