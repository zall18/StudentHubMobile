package com.example.studenthub.ui.screens

import StudentHubViewModel
import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.studenthub.data.Club

// ── Club-specific colors ─────────────────────────────────────────────
private object ClubColors {
    val teal = Color(0xFF0D9488)
    val tealDark = Color(0xFF0F766E)
    val tealLight = Color(0xFFF0FDFA)
    val cyan = Color(0xFF06B6D4)

    val gradientHeader = listOf(Color(0xFF0D9488), Color(0xFF06B6D4), Color(0xFF0EA5E9))
    val fabGradient = listOf(Color(0xFF0D9488), Color(0xFF06B6D4))

    val deleteRed = Color(0xFFDC2626)
    val deleteRedBg = Color(0xFFFEF2F2)

    val presetColors = listOf(
        "#2563EB", "#6366F1", "#0D9488", "#06B6D4",
        "#EC4899", "#EF4444", "#F59E0B", "#10B981"
    )

    // Category colors
    fun categoryColor(category: String): Color = when (category) {
        "Tech" -> Color(0xFF6366F1)
        "Sports" -> Color(0xFFEF4444)
        "Arts" -> Color(0xFFEC4899)
        "Social" -> Color(0xFFF59E0B)
        "Academic" -> Color(0xFF2563EB)
        else -> Color(0xFF64748B)
    }

    fun categoryIcon(category: String): ImageVector = when (category) {
        "Tech" -> Icons.Rounded.Computer
        "Sports" -> Icons.Rounded.SportsSoccer
        "Arts" -> Icons.Rounded.Palette
        "Social" -> Icons.Rounded.Groups
        "Academic" -> Icons.Rounded.School
        else -> Icons.Rounded.Star
    }

    fun categoryLabel(category: String): String = when (category) {
        "Tech" -> "Teknologi"
        "Sports" -> "Olahraga"
        "Arts" -> "Seni"
        "Social" -> "Sosial"
        "Academic" -> "Akademik"
        else -> category
    }
}

// ══════════════════════════════════════════════════════════════════════
// ── MAIN CLUBS SCREEN ────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════
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
    var clubColor by remember { mutableStateOf("#0D9488") }

    fun resetFormFields() {
        clubName = ""
        clubDescription = ""
        clubCategory = "Tech"
        leaderName = ""
        contactPhone = ""
        meetingDay = "Senin"
        meetingLocation = ""
        clubColor = "#0D9488"
    }

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

    val totalClubs = allClubs.size
    val distinctCategories = allClubs.map { it.category }.distinct().size

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = { ClubGradientFab(onClick = { resetFormFields(); showAddDialog = true }) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // ── Header ───────────────────────────────────────────
            item { ClubScreenHeader(totalClubs = totalClubs, categoryCount = distinctCategories) }

            // ── Summary Card ─────────────────────────────────────
            item {
                ClubGradientSummaryCard(
                    totalClubs = totalClubs,
                    categoryCount = distinctCategories,
                    filteredCount = filteredClubs.size,
                    selectedCategory = selectedCategory
                )
            }

            // ── Search (reuse from TasksListScreen) ──────────────
            item {
                PremiumSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onClear = { searchQuery = "" }
                )
            }

            // ── Category Filters ─────────────────────────────────
            item {
                ClubCategoryFilter(
                    categories = categories,
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it }
                )
            }

            // ── Result count ─────────────────────────────────────
            if (filteredClubs.isNotEmpty()) {
                item { ClubResultBar(count = filteredClubs.size) }
            }

            // ── Club list ────────────────────────────────────────
            if (filteredClubs.isEmpty()) {
                item { PremiumEmptyClubState() }
            } else {
                items(filteredClubs, key = { it.id }) { club ->
                    val context = LocalContext.current
                    PremiumClubCard(
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
                                } catch (_: Exception) {}
                            }
                        }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    // ── Dialog ───────────────────────────────────────────────────
    if (showAddDialog) {
        PremiumClubDialog(
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
                resetFormFields()
            },
            onConfirm = {
                if (clubName.isNotBlank()) {
                    if (editingClub != null) {
                        viewModel.updateClub(
                            Club(
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
private fun ClubScreenHeader(totalClubs: Int, categoryCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Organisasi & UKM",
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
                    color = ClubColors.tealLight
                ) {
                    Text(
                        text = "$totalClubs UKM",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ClubColors.teal
                    )
                }
                Text("•", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = "$categoryCount kategori",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(
                    brush = Brush.linearGradient(ClubColors.gradientHeader),
                    shape = RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.Groups,
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
private fun ClubGradientFab(onClick: () -> Unit) {
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
                ambientColor = ClubColors.teal.copy(alpha = 0.3f),
                spotColor = ClubColors.teal.copy(alpha = 0.4f)
            )
            .background(
                brush = Brush.linearGradient(ClubColors.fabGradient),
                shape = CircleShape
            )
    ) {
        Icon(
            Icons.Rounded.Add,
            contentDescription = "Tambah UKM",
            modifier = Modifier.size(28.dp)
        )
    }
}

// ══════════════════════════════════════════════════════════════════════
// ── GRADIENT SUMMARY CARD ────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════
@Composable
private fun ClubGradientSummaryCard(
    totalClubs: Int,
    categoryCount: Int,
    filteredCount: Int,
    selectedCategory: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = ClubColors.teal.copy(alpha = 0.15f),
                spotColor = ClubColors.teal.copy(alpha = 0.2f)
            ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = ClubColors.gradientHeader,
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
                // Reuse PremiumSummaryItem from SubjectsScreen
                PremiumSummaryItem(
                    icon = Icons.Rounded.Groups,
                    value = totalClubs.toString(),
                    label = "Total UKM"
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(56.dp)
                        .background(Color.White.copy(alpha = 0.2f))
                )
                PremiumSummaryItem(
                    icon = Icons.Rounded.Category,
                    value = categoryCount.toString(),
                    label = "Kategori"
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(56.dp)
                        .background(Color.White.copy(alpha = 0.2f))
                )
                PremiumSummaryItem(
                    icon = Icons.Rounded.Visibility,
                    value = filteredCount.toString(),
                    label = if (selectedCategory == "ALL") "Aktif" else "Tampil"
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
// ── CATEGORY FILTER ──────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════
@Composable
private fun ClubCategoryFilter(
    categories: List<Pair<String, String>>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
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
                        color = ClubColors.teal.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Category,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = ClubColors.teal
                )
            }
            Text(
                text = "Kategori",
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
            categories.forEach { (key, label) ->
                val isSelected = selectedCategory == key
                val catColor = if (key == "ALL") ClubColors.teal else ClubColors.categoryColor(key)
                val catIcon = if (key == "ALL") Icons.Rounded.ViewList else ClubColors.categoryIcon(key)

                Surface(
                    onClick = { onCategorySelected(key) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) catColor else MaterialTheme.colorScheme.surface,
                    border = if (!isSelected) {
                        androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        )
                    } else null,
                    shadowElevation = if (isSelected) 2.dp else 0.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            catIcon,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (isSelected) Color.White
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (isSelected) Color.White
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
// ── RESULT BAR ───────────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════
@Composable
private fun ClubResultBar(count: Int) {
    Text(
        text = "$count UKM ditemukan",
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
    )
}

// ══════════════════════════════════════════════════════════════════════
// ── EMPTY STATE ──────────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════
@Composable
private fun PremiumEmptyClubState() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = ClubColors.teal.copy(alpha = 0.05f)
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
                        color = ClubColors.teal.copy(alpha = 0.08f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Groups,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = ClubColors.teal.copy(alpha = 0.4f)
                )
            }
            Text(
                text = "Belum ada UKM",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Tambahkan organisasi atau UKM\ndengan tombol + di bawah",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                lineHeight = 19.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
// ── PREMIUM CLUB CARD ────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════
@Composable
fun PremiumClubCard(
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
            ClubColors.teal
        }
    }

    val catColor = ClubColors.categoryColor(club.category)
    val catIcon = ClubColors.categoryIcon(club.category)
    val catLabel = ClubColors.categoryLabel(club.category)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = clubColor.copy(alpha = 0.06f),
                spotColor = clubColor.copy(alpha = 0.1f)
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
                // Gradient club icon
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            brush = Brush.linearGradient(
                                listOf(clubColor, clubColor.copy(alpha = 0.7f))
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = club.name.take(2).uppercase(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    // Club name
                    Text(
                        text = club.name,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        letterSpacing = (-0.2).sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Category badge with icon
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = catColor.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                catIcon,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = catColor
                            )
                            Text(
                                text = catLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = catColor
                            )
                        }
                    }

                    // Description
                    if (club.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = club.description,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Detail info rows
            if (club.leaderName.isNotBlank() || club.contactPhone.isNotBlank() ||
                club.meetingDay.isNotBlank() || club.meetingLocation.isNotBlank()
            ) {
                Spacer(modifier = Modifier.height(14.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (club.leaderName.isNotBlank()) {
                        ClubInfoRow(
                            icon = Icons.Rounded.Person,
                            text = "Ketua: ${club.leaderName}",
                            accentColor = clubColor
                        )
                    }

                    if (club.meetingDay.isNotBlank()) {
                        ClubInfoRow(
                            icon = Icons.Rounded.CalendarToday,
                            text = buildString {
                                append(club.meetingDay)
                                if (club.meetingLocation.isNotBlank()) append(" • ${club.meetingLocation}")
                            },
                            accentColor = clubColor
                        )
                    } else if (club.meetingLocation.isNotBlank()) {
                        ClubInfoRow(
                            icon = Icons.Rounded.LocationOn,
                            text = club.meetingLocation,
                            accentColor = clubColor
                        )
                    }

                    if (club.contactPhone.isNotBlank()) {
                        ClubInfoRow(
                            icon = Icons.Rounded.Phone,
                            text = club.contactPhone,
                            accentColor = clubColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                thickness = 1.dp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Call button
                if (club.contactPhone.isNotBlank()) {
                    Surface(
                        onClick = onCall,
                        shape = RoundedCornerShape(10.dp),
                        color = ClubColors.tealLight
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Phone,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp),
                                tint = ClubColors.teal
                            )
                            Text(
                                text = "Hubungi",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ClubColors.teal
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Edit
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

                    // Delete
                    Surface(
                        onClick = { showDeleteConfirmation = true },
                        shape = RoundedCornerShape(10.dp),
                        color = ClubColors.deleteRedBg
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
                                tint = ClubColors.deleteRed
                            )
                            Text(
                                text = "Hapus",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ClubColors.deleteRed
                            )
                        }
                    }
                }
            }
        }
    }

    // Delete dialog
    if (showDeleteConfirmation) {
        ClubDeleteDialog(
            clubName = club.name,
            onConfirm = { onDelete(); showDeleteConfirmation = false },
            onDismiss = { showDeleteConfirmation = false }
        )
    }
}

@Composable
private fun ClubInfoRow(
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
private fun ClubDeleteDialog(
    clubName: String,
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
                    .background(color = ClubColors.deleteRedBg, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.DeleteOutline,
                    contentDescription = null,
                    modifier = Modifier.size(26.dp),
                    tint = ClubColors.deleteRed
                )
            }
        },
        title = {
            Text(
                text = "Hapus UKM?",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Text(
                text = "\"$clubName\" akan dihapus secara permanen. Tindakan ini tidak bisa dibatalkan.",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                lineHeight = 20.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = ClubColors.deleteRed),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Icon(Icons.Rounded.DeleteOutline, null, Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Hapus", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
            }
        }
    )
}

// ══════════════════════════════════════════════════════════════════════
// ── PREMIUM CLUB DIALOG ──────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumClubDialog(
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

    val currentColor = remember(clubColor) {
        try {
            val hex = clubColor.removePrefix("#")
            Color(hex.toLong(16) or 0xFF000000.toLong())
        } catch (e: Exception) {
            ClubColors.teal
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
                            brush = Brush.linearGradient(ClubColors.fabGradient),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isEditing) Icons.Rounded.Edit else Icons.Rounded.GroupAdd,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = if (isEditing) "Edit UKM" else "Tambah UKM Baru",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = (-0.3).sp
                    )
                    Text(
                        text = if (isEditing) "Ubah informasi UKM" else "Isi detail organisasi",
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
                // Section: Info Dasar — reuse DialogSectionLabel from SubjectsScreen
                DialogSectionLabel("Informasi Dasar")

                // Reuse PremiumTextField from SubjectsScreen
                PremiumTextField(
                    value = clubName,
                    onValueChange = onClubNameChange,
                    label = "Nama UKM",
                    placeholder = "Masukkan nama UKM"
                )

                OutlinedTextField(
                    value = clubDescription,
                    onValueChange = onClubDescriptionChange,
                    label = { Text("Deskripsi") },
                    placeholder = {
                        Text(
                            "Deskripsi singkat...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        focusedBorderColor = ClubColors.teal,
                        focusedLabelColor = ClubColors.teal,
                        cursorColor = ClubColors.teal
                    )
                )

                // Section: Kategori
                DialogSectionLabel("Kategori")

                // Category grid (2 rows × 3 columns)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    categories.chunked(3).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowItems.forEach { (key, label) ->
                                val isSelected = clubCategory == key
                                val catColor = ClubColors.categoryColor(key)
                                val catIcon = ClubColors.categoryIcon(key)

                                Surface(
                                    onClick = { onClubCategoryChange(key) },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) catColor.copy(alpha = 0.12f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    border = if (isSelected)
                                        androidx.compose.foundation.BorderStroke(1.5.dp, catColor.copy(alpha = 0.5f))
                                    else null,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            catIcon,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = if (isSelected) catColor
                                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                        )
                                        Text(
                                            text = label,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) catColor
                                            else MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                            // Fill remaining space if row has < 3 items
                            repeat(3 - rowItems.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                // Section: Pengurus
                DialogSectionLabel("Pengurus & Kontak")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PremiumTextField(
                        value = leaderName,
                        onValueChange = onLeaderNameChange,
                        label = "Ketua",
                        placeholder = "Nama ketua",
                        modifier = Modifier.weight(1f),
                        leadingIcon = Icons.Rounded.Person
                    )
                    PremiumTextField(
                        value = contactPhone,
                        onValueChange = onContactPhoneChange,
                        label = "Kontak",
                        placeholder = "08xxx",
                        modifier = Modifier.weight(0.7f),
                        leadingIcon = Icons.Rounded.Phone
                    )
                }

                // Section: Jadwal
                DialogSectionLabel("Jadwal Pertemuan")

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
                            focusedBorderColor = ClubColors.teal
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expandedDayDropdown,
                        onDismissRequest = { expandedDayDropdown = false }
                    ) {
                        meetingDays.forEach { day ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        day,
                                        fontWeight = if (day == meetingDay) FontWeight.Bold else FontWeight.Normal,
                                        color = if (day == meetingDay) ClubColors.teal
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = { onMeetingDayChange(day); expandedDayDropdown = false },
                                leadingIcon = {
                                    if (day == meetingDay) {
                                        Icon(
                                            Icons.Rounded.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = ClubColors.teal
                                        )
                                    }
                                }
                            )
                        }
                    }
                }

                PremiumTextField(
                    value = meetingLocation,
                    onValueChange = onMeetingLocationChange,
                    label = "Lokasi",
                    placeholder = "Ruang atau gedung...",
                    leadingIcon = Icons.Rounded.LocationOn
                )

                // Section: Warna
                DialogSectionLabel("Warna Identitas")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ClubColors.presetColors.forEach { preset ->
                        val presetColor = try {
                            val hex = preset.removePrefix("#")
                            Color(hex.toLong(16) or 0xFF000000.toLong())
                        } catch (e: Exception) {
                            ClubColors.teal
                        }
                        val isSelected = clubColor.equals(preset, ignoreCase = true)

                        Surface(
                            onClick = { onClubColorChange(preset) },
                            shape = CircleShape,
                            color = presetColor,
                            modifier = Modifier
                                .size(32.dp)
                                .then(
                                    if (isSelected) Modifier.border(2.5.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                    else Modifier
                                )
                        ) {
                            if (isSelected) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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

                OutlinedTextField(
                    value = clubColor,
                    onValueChange = {
                        if (it.startsWith("#") && it.length <= 7) onClubColorChange(it)
                        else if (!it.startsWith("#") && it.length <= 6) onClubColorChange("#$it")
                    },
                    label = { Text("Kode Warna (Hex)") },
                    placeholder = { Text("#0D9488") },
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
                        focusedBorderColor = ClubColors.teal
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = clubName.isNotBlank(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ClubColors.teal),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Icon(
                    if (isEditing) Icons.Rounded.Check else Icons.Rounded.GroupAdd,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    if (isEditing) "Simpan" else "Tambah",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
            }
        }
    )
}