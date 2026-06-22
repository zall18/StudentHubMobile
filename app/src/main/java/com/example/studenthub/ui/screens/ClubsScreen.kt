package com.example.studenthub.ui.screens

import StudentHubViewModel
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.studenthub.data.Club
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.vector.ImageVector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClubsScreen(viewModel: StudentHubViewModel) {
    val allClubs by viewModel.allClubs.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingClub by remember { mutableStateOf<Club?>(null) }

    // Form fields
    var clubName by remember { mutableStateOf("") }
    var clubDescription by remember { mutableStateOf("") }
    var clubCategory by remember { mutableStateOf("Tech") }
    var leaderName by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    var meetingDay by remember { mutableStateOf("Senin") }
    var meetingLocation by remember { mutableStateOf("") }
    var clubColor by remember { mutableStateOf("#2563EB") }

    val categories = listOf(
        "ALL" to "Semua",
        "Tech" to "Teknologi",
        "Sports" to "Olahraga",
        "Arts" to "Seni",
        "Social" to "Sosial",
        "Academic" to "Akademik"
    )

    val meetingDays = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")

    val filteredClubs = remember(allClubs, searchQuery, selectedCategory) {
        allClubs.filter { club ->
            val matchesSearch = club.name.contains(searchQuery, ignoreCase = true) ||
                    club.description.contains(searchQuery, ignoreCase = true) ||
                    club.category.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == "ALL" || club.category == selectedCategory
            matchesSearch && matchesCategory
        }.sortedBy { it.name }
    }

    // Stats
    val totalClubs = allClubs.size
    val categoryCount = remember(allClubs, selectedCategory) {
        if (selectedCategory == "ALL") allClubs.map { it.category }.distinct().size
        else allClubs.count { it.category == selectedCategory }
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
                    contentDescription = "Tambah UKM",
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
                        text = "Organisasi & UKM",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$totalClubs UKM terdaftar • $categoryCount kategori",
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
                            icon = Icons.Outlined.Groups,
                            value = totalClubs.toString(),
                            label = "Total UKM"
                        )
                        SummaryItem(
                            icon = Icons.Outlined.Category,
                            value = allClubs.map { it.category }.distinct().size.toString(),
                            label = "Kategori"
                        )
                        SummaryItem(
                            icon = Icons.Outlined.People,
                            value = categoryCount.toString(),
                            label = if (selectedCategory == "ALL") "Aktif" else "Di kategori"
                        )
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari UKM...") },
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
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
            }

            // Category Filters
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Kategori",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF374151)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories.size) { index ->
                            val (categoryKey, categoryLabel) = categories[index]
                            FilterChip(
                                selected = selectedCategory == categoryKey,
                                onClick = { selectedCategory = categoryKey },
                                label = {
                                    Text(
                                        text = categoryLabel,
                                        fontSize = 12.sp,
                                        fontWeight = if (selectedCategory == categoryKey) FontWeight.Medium else FontWeight.Normal
                                    )
                                },
                                leadingIcon = if (selectedCategory == categoryKey) {
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

            // Results count
            item {
                if (filteredClubs.isNotEmpty()) {
                    Text(
                        text = "${filteredClubs.size} UKM ditemukan",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF374151)
                    )
                }
            }

            // Club List
            if (filteredClubs.isEmpty()) {
                item {
                    EmptyClubState()
                }
            } else {
                items(filteredClubs, key = { it.id }) { club ->
                    val context = LocalContext.current
                    EnhancedClubCard(
                        club = club,
                        onEdit = {
                            editingClub = club
                            clubName = club.name
                            clubDescription = club.description
                            clubCategory = club.category
                            leaderName = club.leaderName
                            contactPhone = club.contactPhone
                            meetingDay = club.meetingDay
                            meetingLocation = club.meetingLocation
                            clubColor = club.color
                            showAddDialog = true
                        },

                        onDelete = { viewModel.deleteClub(club.id) },
                        onCall = {

                            if (club.contactPhone.isNotBlank()) {
                                try {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${club.contactPhone}"))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // Handle error
                                }
                            }
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Add/Edit Club Dialog
    if (showAddDialog) {
        ClubDialog(
            isEditing = editingClub != null,
            clubName = clubName,
            onClubNameChange = { clubName = it },
            clubDescription = clubDescription,
            onClubDescriptionChange = { clubDescription = it },
            clubCategory = clubCategory,
            onClubCategoryChange = { clubCategory = it },
            leaderName = leaderName,
            onLeaderNameChange = { leaderName = it },
            contactPhone = contactPhone,
            onContactPhoneChange = { contactPhone = it },
            meetingDay = meetingDay,
            onMeetingDayChange = { meetingDay = it },
            meetingLocation = meetingLocation,
            onMeetingLocationChange = { meetingLocation = it },
            clubColor = clubColor,
            onClubColorChange = { clubColor = it },
            categories = categories.filter { it.first != "ALL" },
            meetingDays = meetingDays,
            onDismiss = {
                showAddDialog = false
                editingClub = null
                resetForm()
            },
            onConfirm = {
                if (clubName.isNotBlank()) {
                    if (editingClub != null) {
                        val updatedClub = Club(
                            id = editingClub!!.id,
                            name = clubName,
                            description = clubDescription,
                            category = clubCategory,
                            leaderName = leaderName,
                            contactPhone = contactPhone,
                            meetingDay = meetingDay,
                            meetingLocation = meetingLocation,
                            color = clubColor
                        )
                        viewModel.updateClub(
                            updatedClub
                        )
                    } else {
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
                    }
                    showAddDialog = false
                    editingClub = null
                    resetForm()
                }
            }
        )
    }
}

@Composable
fun EmptyClubState() {
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
                Icons.Outlined.Groups,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color(0xFFD1D5DB)
            )
            Text(
                text = "Belum ada UKM",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF6B7280)
            )
            Text(
                text = "Tambahkan unit kegiatan mahasiswa atau organisasi",
                fontSize = 13.sp,
                color = Color(0xFF9CA3AF),
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun EnhancedClubCard(
    club: Club,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCall: () -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    val clubColor = remember(club.color) {
        try {
            val colorHex = club.color.removePrefix("#")
            Color(colorHex.toLong(16) or 0xFF000000.toLong())
        } catch (e: Exception) {
            Color(0xFF2563EB)
        }
    }

    val categoryIcon = remember(club.category) {
        when (club.category) {
            "Tech" -> Icons.Outlined.Computer
            "Sports" -> Icons.Outlined.SportsSoccer
            "Arts" -> Icons.Outlined.Palette
            "Social" -> Icons.Outlined.Groups
            "Academic" -> Icons.Outlined.School
            else -> Icons.Outlined.Star
        }
    }

    val categoryLabel = remember(club.category) {
        when (club.category) {
            "Tech" -> "Teknologi"
            "Sports" -> "Olahraga"
            "Arts" -> "Seni"
            "Social" -> "Sosial"
            "Academic" -> "Akademik"
            else -> club.category
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
                // Club color icon
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(clubColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = club.name.take(2).uppercase(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    // Club name and category
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = club.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF111827),
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Category badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = clubColor.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                categoryIcon,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = clubColor
                            )
                            Text(
                                text = categoryLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = clubColor
                            )
                        }
                    }

                    // Description
                    if (club.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = club.description,
                            fontSize = 13.sp,
                            color = Color(0xFF6B7280),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Club details
            if (club.leaderName.isNotBlank() || club.contactPhone.isNotBlank() ||
                (club.meetingDay.isNotBlank() && club.meetingLocation.isNotBlank())) {
                Spacer(modifier = Modifier.height(16.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Leader info
                    if (club.leaderName.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Person,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFF9CA3AF)
                            )
                            Text(
                                text = "Ketua: ${club.leaderName}",
                                fontSize = 13.sp,
                                color = Color(0xFF6B7280)
                            )
                        }
                    }

                    // Meeting info
                    if (club.meetingDay.isNotBlank() && club.meetingLocation.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Outlined.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFF9CA3AF)
                            )
                            Text(
                                text = "${club.meetingDay}, ${club.meetingLocation}",
                                fontSize = 13.sp,
                                color = Color(0xFF6B7280)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Contact button
                if (club.contactPhone.isNotBlank()) {
                    TextButton(
                        onClick = onCall,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Call,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Hubungi",
                            fontSize = 13.sp
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                // Edit & Delete buttons
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Edit,
                            contentDescription = "Edit",
                            tint = Color(0xFF6B7280),
                            modifier = Modifier.size(18.dp)
                        )
                    }

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
                    text = "Hapus UKM",
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text("Apakah kamu yakin ingin menghapus \"${club.name}\"?")
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
fun ClubDialog(
    isEditing: Boolean,
    clubName: String,
    onClubNameChange: (String) -> Unit,
    clubDescription: String,
    onClubDescriptionChange: (String) -> Unit,
    clubCategory: String,
    onClubCategoryChange: (String) -> Unit,
    leaderName: String,
    onLeaderNameChange: (String) -> Unit,
    contactPhone: String,
    onContactPhoneChange: (String) -> Unit,
    meetingDay: String,
    onMeetingDayChange: (String) -> Unit,
    meetingLocation: String,
    onMeetingLocationChange: (String) -> Unit,
    clubColor: String,
    onClubColorChange: (String) -> Unit,
    categories: List<Pair<String, String>>,
    meetingDays: List<String>,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var expandedDayDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = if (isEditing) "Edit UKM" else "Tambah UKM Baru",
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
                // Club name
                OutlinedTextField(
                    value = clubName,
                    onValueChange = onClubNameChange,
                    label = { Text("Nama UKM") },
                    placeholder = { Text("Masukkan nama UKM...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Description
                OutlinedTextField(
                    value = clubDescription,
                    onValueChange = onClubDescriptionChange,
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
                        categories.forEach { (key, label) ->
                            FilterChip(
                                selected = clubCategory == key,
                                onClick = { onClubCategoryChange(key) },
                                label = {
                                    Text(
                                        text = label,
                                        fontSize = 12.sp
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

                // Leader info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = leaderName,
                        onValueChange = onLeaderNameChange,
                        label = { Text("Ketua") },
                        placeholder = { Text("Nama ketua") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = contactPhone,
                        onValueChange = onContactPhoneChange,
                        label = { Text("Kontak") },
                        placeholder = { Text("08xxx") },
                        modifier = Modifier.weight(0.7f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Meeting info
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Jadwal Pertemuan",
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
                            value = meetingDay,
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
                            meetingDays.forEach { day ->
                                DropdownMenuItem(
                                    text = { Text(day) },
                                    onClick = {
                                        onMeetingDayChange(day)
                                        expandedDayDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = meetingLocation,
                        onValueChange = onMeetingLocationChange,
                        label = { Text("Lokasi") },
                        placeholder = { Text("Ruang atau gedung...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Color
                OutlinedTextField(
                    value = clubColor,
                    onValueChange = {
                        if (it.startsWith("#") && it.length <= 7) {
                            onClubColorChange(it)
                        } else if (!it.startsWith("#") && it.length <= 6) {
                            onClubColorChange("#$it")
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
                                        val colorHex = clubColor.removePrefix("#")
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
                enabled = clubName.isNotBlank(),
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

// Helper functions
private fun resetForm() {
    // Reset handled by remember states in the composable
}

@Composable
private fun rememberScrollState(): ScrollState {
    return rememberScrollState()
}