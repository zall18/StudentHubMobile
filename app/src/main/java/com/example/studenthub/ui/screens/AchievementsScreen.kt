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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.studenthub.data.Achievement
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(viewModel: StudentHubViewModel) {
    val achievements by viewModel.allAchievements.collectAsStateWithLifecycle()

    var selectedCategory by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingAchievement by remember { mutableStateOf<Achievement?>(null) }

    // Form fields
    var title by remember { mutableStateOf("") }
    var issuer by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("CERTIFICATION") }
    var description by remember { mutableStateOf("") }
    var credentialUrl by remember { mutableStateOf("") }
    var credentialId by remember { mutableStateOf("") }
    var importance by remember { mutableStateOf("2") }
    var expiryDate by remember { mutableStateOf("") }

    val tabs = listOf(
        "ALL" to "Semua",
        "CERTIFICATION" to "Sertifikasi",
        "AWARD" to "Penghargaan",
        "COMPETITION" to "Kompetisi",
        "BADGE" to "Badge"
    )

    val filteredAchievements = remember(achievements, selectedCategory, searchQuery) {
        val baseList = when (selectedCategory) {
            "ALL" -> achievements
            else -> achievements.filter { it.category == selectedCategory }
        }

        if (searchQuery.isNotBlank()) {
            baseList.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.issuer.contains(searchQuery, ignoreCase = true) ||
                        it.description.contains(searchQuery, ignoreCase = true)
            }
        } else {
            baseList
        }
    }.sortedByDescending { it.importance }

    // Stats
    val totalAchievements = achievements.size
    val highImportanceCount = remember(achievements) {
        achievements.count { it.importance == 3 }
    }
    val uniqueIssuers = remember(achievements) {
        achievements.map { it.issuer }.distinct().size
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
                    contentDescription = "Tambah Achievement",
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
                        text = "Achievements",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$totalAchievements pencapaian • $highImportanceCount penting",
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }

            // Stats Summary
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        icon = Icons.Outlined.EmojiEvents,
                        value = totalAchievements.toString(),
                        label = "Total",
                        containerColor = Color(0xFFEFF6FF),
                        contentColor = Color(0xFF2563EB),
                        modifier = Modifier.weight(1f)
                    )

                    StatCard(
                        icon = Icons.Outlined.Star,
                        value = highImportanceCount.toString(),
                        label = "Penting",
                        containerColor = Color(0xFFFEF2F2),
                        contentColor = Color(0xFFDC2626),
                        modifier = Modifier.weight(1f)
                    )

                    StatCard(
                        icon = Icons.Outlined.Business,
                        value = uniqueIssuers.toString(),
                        label = "Institusi",
                        containerColor = Color(0xFFFAF5FF),
                        contentColor = Color(0xFF7C3AED),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari achievement...") },
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
                        items(tabs.size) { index ->
                            val (tabKey, tabLabel) = tabs[index]
                            FilterChip(
                                selected = selectedCategory == tabKey,
                                onClick = { selectedCategory = tabKey },
                                label = {
                                    Text(
                                        text = tabLabel,
                                        fontSize = 12.sp,
                                        fontWeight = if (selectedCategory == tabKey) FontWeight.Medium else FontWeight.Normal
                                    )
                                },
                                leadingIcon = if (selectedCategory == tabKey) {
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
                if (filteredAchievements.isNotEmpty()) {
                    Text(
                        text = "${filteredAchievements.size} achievement ditemukan",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF374151)
                    )
                }
            }

            // Achievement List
            if (filteredAchievements.isEmpty()) {
                item {
                    EmptyAchievementState()
                }
            } else {
                items(filteredAchievements, key = { it.id }) { achievement ->
                    val context = LocalContext.current
                    EnhancedAchievementCard(
                        achievement = achievement,
                        onEdit = {
                            editingAchievement = achievement
                            title = achievement.title
                            issuer = achievement.issuer
                            category = achievement.category
                            description = achievement.description
                            credentialUrl = achievement.credentialUrl
                            credentialId = achievement.credentialId
                            importance = achievement.importance.toString()
                            expiryDate = achievement.expiryDate?.toString() ?: ""
                            showAddDialog = true
                        },
                        onDelete = { viewModel.deleteAchievement(achievement.id) },
                        onOpenUrl = {
                            if (achievement.credentialUrl.isNotBlank()) {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(achievement.credentialUrl))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // Handle invalid URL
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

    // Add/Edit Achievement Dialog
    if (showAddDialog) {
        AchievementDialog(
            isEditing = editingAchievement != null,
            title = title,
            onTitleChange = { title = it },
            issuer = issuer,
            onIssuerChange = { issuer = it },
            category = category,
            onCategoryChange = { category = it },
            description = description,
            onDescriptionChange = { description = it },
            credentialUrl = credentialUrl,
            onCredentialUrlChange = { credentialUrl = it },
            credentialId = credentialId,
            onCredentialIdChange = { credentialId = it },
            importance = importance,
            onImportanceChange = { importance = it },
            onDismiss = {
                showAddDialog = false
                editingAchievement = null
                resetForm()
            },
            onConfirm = {
                if (title.isNotBlank()) {
                    if (editingAchievement != null) {
                        val updatedAchiv = Achievement (
                            id = editingAchievement!!.id,
                            title = title,
                            issuer = issuer,
                            category = category,
                            description = description,
                            importance = importance.toIntOrNull() ?: 2,
                            credentialUrl = credentialUrl,
                            credentialId = credentialId
                        )
                        viewModel.updateAchievement(
            updatedAchiv
                        )
                    } else {
                        viewModel.addAchievement(
                            title = title,
                            issuer = issuer,
                            category = category,
                            description = description,
                            importance = importance.toIntOrNull() ?: 2,
                            credentialUrl = credentialUrl,
                        )
                    }
                    showAddDialog = false
                    editingAchievement = null
                    resetForm()
                }
            }
        )
    }
}


@Composable
fun EmptyAchievementState() {
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
                Icons.Outlined.EmojiEvents,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color(0xFFD1D5DB)
            )
            Text(
                text = "Belum ada achievement",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF6B7280)
            )
            Text(
                text = "Tambahkan sertifikat, penghargaan, atau pencapaianmu",
                fontSize = 13.sp,
                color = Color(0xFF9CA3AF),
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun EnhancedAchievementCard(
    achievement: Achievement,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onOpenUrl: () -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))

    val categoryColor = remember(achievement.category) {
        when (achievement.category) {
            "CERTIFICATION" -> Color(0xFF2563EB)
            "AWARD" -> Color(0xFFF59E0B)
            "COMPETITION" -> Color(0xFF7C3AED)
            "BADGE" -> Color(0xFF10B981)
            else -> Color(0xFF6B7280)
        }
    }

    val categoryIcon = remember(achievement.category) {
        when (achievement.category) {
            "CERTIFICATION" -> Icons.Outlined.Verified
            "AWARD" -> Icons.Outlined.EmojiEvents
            "COMPETITION" -> Icons.Outlined.Groups
            "BADGE" -> Icons.Outlined.MilitaryTech
            else -> Icons.Outlined.Star
        }
    }

    val categoryLabel = remember(achievement.category) {
        when (achievement.category) {
            "CERTIFICATION" -> "Sertifikasi"
            "AWARD" -> "Penghargaan"
            "COMPETITION" -> "Kompetisi"
            "BADGE" -> "Badge"
            else -> achievement.category
        }
    }

    val isExpired = remember(achievement.expiryDate) {
        achievement.expiryDate != null && achievement.expiryDate < System.currentTimeMillis()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (achievement.importance == 3) Color(0xFFFFFBEB) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = if (achievement.importance == 3) {
            BorderStroke(1.dp, Color(0xFFFDE68A))
        } else null
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Category icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(categoryColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        categoryIcon,
                        contentDescription = achievement.category,
                        modifier = Modifier.size(24.dp),
                        tint = categoryColor
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    // Title & Importance
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = achievement.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF111827),
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Importance stars
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            repeat(3) { index ->
                                Icon(
                                    if (index < achievement.importance) Icons.Filled.Star else Icons.Outlined.Star,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = if (index < achievement.importance) {
                                        when (achievement.importance) {
                                            3 -> Color(0xFFF59E0B)
                                            2 -> Color(0xFF6B7280)
                                            else -> Color(0xFF9CA3AF)
                                        }
                                    } else {
                                        Color(0xFFD1D5DB)
                                    }
                                )
                            }
                        }
                    }

                    // Issuer
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = achievement.issuer,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF6B7280)
                    )

                    // Description
                    if (achievement.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = achievement.description,
                            fontSize = 13.sp,
                            color = Color(0xFF9CA3AF),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tags
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Category badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = categoryColor.copy(alpha = 0.1f)
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
                                    tint = categoryColor
                                )
                                Text(
                                    text = categoryLabel,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = categoryColor
                                )
                            }
                        }

                        // Credential ID
                        if (achievement.credentialId.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFF3F4F6)
                            ) {
                                Text(
                                    text = "ID: ${achievement.credentialId}",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    fontSize = 11.sp,
                                    color = Color(0xFF6B7280)
                                )
                            }
                        }

                        // Expiry badge
                        if (achievement.expiryDate != null) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isExpired) Color(0xFFFEF2F2) else Color(0xFFF0FDF4)
                            ) {
                                Text(
                                    text = if (isExpired) "Expired" else "Berlaku",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isExpired) Color(0xFFDC2626) else Color(0xFF059669)
                                )
                            }
                        }
                    }

                    // Expiry date
                    if (achievement.expiryDate != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Outlined.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = if (isExpired) Color(0xFFDC2626) else Color(0xFF9CA3AF)
                            )
                            Text(
                                text = "Berlaku hingga: ${dateFormat.format(Date(achievement.expiryDate))}",
                                fontSize = 11.sp,
                                color = if (isExpired) Color(0xFFDC2626) else Color(0xFF9CA3AF)
                            )
                        }
                    }
                }
            }

            // Credential URL
            if (achievement.credentialUrl.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenUrl() }
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF9FAFB)),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Outlined.Link,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF2563EB)
                    )
                    Text(
                        text = "Lihat Kredensial",
                        fontSize = 13.sp,
                        color = Color(0xFF2563EB),
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        Icons.Outlined.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color(0xFF2563EB)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Actions
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

    // Delete Confirmation
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    text = "Hapus Achievement",
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text("Apakah kamu yakin ingin menghapus \"${achievement.title}\"?")
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
fun AchievementDialog(
    isEditing: Boolean,
    title: String,
    onTitleChange: (String) -> Unit,
    issuer: String,
    onIssuerChange: (String) -> Unit,
    category: String,
    onCategoryChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    credentialUrl: String,
    onCredentialUrlChange: (String) -> Unit,
    credentialId: String,
    onCredentialIdChange: (String) -> Unit,
    importance: String,
    onImportanceChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val categories = listOf(
        "CERTIFICATION" to "Sertifikasi",
        "AWARD" to "Penghargaan",
        "COMPETITION" to "Kompetisi",
        "BADGE" to "Badge"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = if (isEditing) "Edit Achievement" else "Tambah Achievement",
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
                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    label = { Text("Judul") },
                    placeholder = { Text("Masukkan judul achievement...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Issuer & Credential ID
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = issuer,
                        onValueChange = onIssuerChange,
                        label = { Text("Penerbit") },
                        placeholder = { Text("Nama institusi") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = credentialId,
                        onValueChange = onCredentialIdChange,
                        label = { Text("ID Kredensial") },
                        placeholder = { Text("Opsional") },
                        modifier = Modifier.weight(0.7f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    label = { Text("Deskripsi") },
                    placeholder = { Text("Deskripsi singkat...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )

                // Credential URL
                OutlinedTextField(
                    value = credentialUrl,
                    onValueChange = onCredentialUrlChange,
                    label = { Text("URL Kredensial") },
                    placeholder = { Text("https://...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
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
                                selected = category == key,
                                onClick = { onCategoryChange(key) },
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

                // Importance
                Column {
                    Text(
                        text = "Tingkat Kepentingan",
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
                            "1" to "Biasa",
                            "2" to "Penting",
                            "3" to "Sangat Penting"
                        ).forEach { (value, label) ->
                            FilterChip(
                                selected = importance == value,
                                onClick = { onImportanceChange(value) },
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
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = title.isNotBlank() && issuer.isNotBlank(),
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

        })
}

// Helper functions
private fun resetForm() {
    // Reset handled by remember states in the composable
}

@Composable
private fun rememberScrollState(): ScrollState {
    return rememberScrollState()
}