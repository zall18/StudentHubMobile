package com.example.studenthub.ui.screens

import StudentHubViewModel
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.studenthub.data.Achievement
import java.text.SimpleDateFormat
import java.util.*

// ── Achievement-specific colors ──────────────────────────────────────
private object AchievementColors {
    val amber = Color(0xFFF59E0B)
    val amberDark = Color(0xFFD97706)
    val amberLight = Color(0xFFFFFBEB)
    val orange = Color(0xFFF97316)

    val gradientHeader = listOf(Color(0xFFF59E0B), Color(0xFFF97316), Color(0xFFEF4444))
    val fabGradient = listOf(Color(0xFFF59E0B), Color(0xFFF97316))

    val deleteRed = Color(0xFFDC2626)
    val deleteRedBg = Color(0xFFFEF2F2)

    val importanceHigh = Color(0xFFF59E0B)
    val importanceMed = Color(0xFF6366F1)
    val importanceLow = Color(0xFF64748B)

    fun categoryColor(category: String): Color = when (category) {
        "CERTIFICATION" -> Color(0xFF2563EB)
        "AWARD" -> Color(0xFFF59E0B)
        "COMPETITION" -> Color(0xFF7C3AED)
        "BADGE" -> Color(0xFF10B981)
        else -> Color(0xFF64748B)
    }

    fun categoryIcon(category: String): ImageVector = when (category) {
        "CERTIFICATION" -> Icons.Rounded.Verified
        "AWARD" -> Icons.Rounded.EmojiEvents
        "COMPETITION" -> Icons.Rounded.Groups
        "BADGE" -> Icons.Rounded.MilitaryTech
        else -> Icons.Rounded.Star
    }

    fun categoryLabel(category: String): String = when (category) {
        "CERTIFICATION" -> "Sertifikasi"
        "AWARD" -> "Penghargaan"
        "COMPETITION" -> "Kompetisi"
        "BADGE" -> "Badge"
        else -> category
    }
}

// ══════════════════════════════════════════════════════════════════════
// ── MAIN ACHIEVEMENTS SCREEN ─────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════
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

    fun resetFormFields() {
        title = ""; issuer = ""; category = "CERTIFICATION"
        description = ""; credentialUrl = ""; credentialId = ""
        importance = "2"; expiryDate = ""
    }

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
        } else baseList
    }.sortedByDescending { it.importance }

    val totalAchievements = achievements.size
    val highImportanceCount = remember(achievements) { achievements.count { it.importance == 3 } }
    val uniqueIssuers = remember(achievements) { achievements.map { it.issuer }.distinct().size }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = { AchievementGradientFab(onClick = { resetFormFields(); showAddDialog = true }) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // ── Header ───────────────────────────────────────────
            item { AchievementScreenHeader(total = totalAchievements, important = highImportanceCount) }

            // ── Stat Cards ───────────────────────────────────────
            item {
                PremiumStatRow(
                    total = totalAchievements,
                    highCount = highImportanceCount,
                    issuers = uniqueIssuers
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

            // ── Category Filter ──────────────────────────────────
            item {
                AchievementCategoryFilter(
                    tabs = tabs,
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it }
                )
            }

            // ── Result count ─────────────────────────────────────
            if (filteredAchievements.isNotEmpty()) {
                item { AchievementResultBar(count = filteredAchievements.size) }
            }

            // ── Achievement List ─────────────────────────────────
            if (filteredAchievements.isEmpty()) {
                item { PremiumEmptyAchievementState() }
            } else {
                items(filteredAchievements, key = { it.id }) { achievement ->
                    val context = LocalContext.current
                    PremiumAchievementCard(
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
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse(achievement.credentialUrl))
                                    )
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
        PremiumAchievementDialog(
            isEditing = editingAchievement != null,
            title = title, onTitleChange = { title = it },
            issuer = issuer, onIssuerChange = { issuer = it },
            category = category, onCategoryChange = { category = it },
            description = description, onDescriptionChange = { description = it },
            credentialUrl = credentialUrl, onCredentialUrlChange = { credentialUrl = it },
            credentialId = credentialId, onCredentialIdChange = { credentialId = it },
            importance = importance, onImportanceChange = { importance = it },
            onDismiss = {
                showAddDialog = false
                editingAchievement = null
                resetFormFields()
            },
            onConfirm = {
                if (title.isNotBlank()) {
                    if (editingAchievement != null) {
                        viewModel.updateAchievement(
                            Achievement(
                                id = editingAchievement!!.id,
                                title = title,
                                issuer = issuer,
                                category = category,
                                description = description,
                                importance = importance.toIntOrNull() ?: 2,
                                credentialUrl = credentialUrl,
                                credentialId = credentialId
                            )
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
private fun AchievementScreenHeader(total: Int, important: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Achievements",
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
                    color = AchievementColors.amberLight
                ) {
                    Text(
                        text = "$total pencapaian",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AchievementColors.amberDark
                    )
                }
                Text("•", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = "$important penting",
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
                    brush = Brush.linearGradient(AchievementColors.gradientHeader),
                    shape = RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.EmojiEvents,
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
private fun AchievementGradientFab(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        shape = CircleShape,
        containerColor = Color.Transparent,
        contentColor = Color.White,
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp, pressedElevation = 12.dp),
        modifier = Modifier
            .shadow(10.dp, CircleShape, ambientColor = AchievementColors.amber.copy(alpha = 0.3f), spotColor = AchievementColors.amber.copy(alpha = 0.4f))
            .background(brush = Brush.linearGradient(AchievementColors.fabGradient), shape = CircleShape)
    ) {
        Icon(Icons.Rounded.Add, contentDescription = "Tambah Achievement", modifier = Modifier.size(28.dp))
    }
}

// ══════════════════════════════════════════════════════════════════════
// ── PREMIUM STAT ROW ─────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════
@Composable
private fun PremiumStatRow(total: Int, highCount: Int, issuers: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PremiumStatCard(
            icon = Icons.Rounded.EmojiEvents,
            value = total.toString(),
            label = "Total",
            gradientColors = listOf(Color(0xFF2563EB), Color(0xFF3B82F6)),
            modifier = Modifier.weight(1f)
        )
        PremiumStatCard(
            icon = Icons.Rounded.Star,
            value = highCount.toString(),
            label = "Penting",
            gradientColors = listOf(Color(0xFFF59E0B), Color(0xFFFBBF24)),
            modifier = Modifier.weight(1f)
        )
        PremiumStatCard(
            icon = Icons.Rounded.Business,
            value = issuers.toString(),
            label = "Institusi",
            gradientColors = listOf(Color(0xFF7C3AED), Color(0xFF8B5CF6)),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PremiumStatCard(
    icon: ImageVector,
    value: String,
    label: String,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.shadow(
            elevation = 6.dp,
            shape = RoundedCornerShape(18.dp),
            ambientColor = gradientColors.first().copy(alpha = 0.1f),
            spotColor = gradientColors.first().copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        brush = Brush.linearGradient(gradientColors),
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
            }
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
// ── CATEGORY FILTER ──────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════
@Composable
private fun AchievementCategoryFilter(
    tabs: List<Pair<String, String>>,
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
                    .background(color = AchievementColors.amber.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Category, contentDescription = null, modifier = Modifier.size(16.dp), tint = AchievementColors.amber)
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
            tabs.forEach { (key, label) ->
                val isSelected = selectedCategory == key
                val catColor = if (key == "ALL") AchievementColors.amber else AchievementColors.categoryColor(key)
                val catIcon = if (key == "ALL") Icons.Rounded.ViewList else AchievementColors.categoryIcon(key)

                Surface(
                    onClick = { onCategorySelected(key) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) catColor else MaterialTheme.colorScheme.surface,
                    border = if (!isSelected) {
                        androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    } else null,
                    shadowElevation = if (isSelected) 2.dp else 0.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            catIcon, contentDescription = null, modifier = Modifier.size(16.dp),
                            tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Text(
                            text = label, fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
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
private fun AchievementResultBar(count: Int) {
    Text(
        text = "$count achievement ditemukan",
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
    )
}

// ══════════════════════════════════════════════════════════════════════
// ── EMPTY STATE ──────────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════
@Composable
private fun PremiumEmptyAchievementState() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp), ambientColor = AchievementColors.amber.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(color = AchievementColors.amber.copy(alpha = 0.08f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.EmojiEvents, contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = AchievementColors.amber.copy(alpha = 0.4f)
                )
            }
            Text(
                text = "Belum ada achievement",
                fontSize = 17.sp, fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Tambahkan sertifikat, penghargaan,\natau pencapaianmu",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                lineHeight = 19.sp, textAlign = TextAlign.Center
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
// ── PREMIUM ACHIEVEMENT CARD ─────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════
@Composable
fun PremiumAchievementCard(
    achievement: Achievement,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onOpenUrl: () -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))

    val catColor = AchievementColors.categoryColor(achievement.category)
    val catIcon = AchievementColors.categoryIcon(achievement.category)
    val catLabel = AchievementColors.categoryLabel(achievement.category)

    val isExpired = remember(achievement.expiryDate) {
        achievement.expiryDate != null && achievement.expiryDate < System.currentTimeMillis()
    }

    val isHighImportance = achievement.importance == 3
    val cardBorderColor = if (isHighImportance) AchievementColors.amber.copy(alpha = 0.4f) else Color.Transparent

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isHighImportance) 6.dp else 4.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = if (isHighImportance) AchievementColors.amber.copy(alpha = 0.1f)
                else catColor.copy(alpha = 0.06f),
                spotColor = if (isHighImportance) AchievementColors.amber.copy(alpha = 0.15f)
                else catColor.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighImportance)
                AchievementColors.amberLight
            else MaterialTheme.colorScheme.surface
        ),
        border = if (isHighImportance)
            androidx.compose.foundation.BorderStroke(1.dp, cardBorderColor)
        else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Gradient category icon
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(
                            brush = Brush.linearGradient(
                                listOf(catColor, catColor.copy(alpha = 0.7f))
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        catIcon, contentDescription = null,
                        modifier = Modifier.size(26.dp),
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    // Title + importance stars
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = achievement.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 21.sp,
                            letterSpacing = (-0.2).sp
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Importance stars
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when (achievement.importance) {
                                3 -> AchievementColors.importanceHigh.copy(alpha = 0.12f)
                                2 -> AchievementColors.importanceMed.copy(alpha = 0.1f)
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(1.dp)
                            ) {
                                repeat(3) { index ->
                                    val starColor = when {
                                        index < achievement.importance && achievement.importance == 3 -> AchievementColors.importanceHigh
                                        index < achievement.importance && achievement.importance == 2 -> AchievementColors.importanceMed
                                        index < achievement.importance -> AchievementColors.importanceLow
                                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                    }
                                    Icon(
                                        if (index < achievement.importance) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = starColor
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Issuer
                    AchievementInfoRow(
                        icon = Icons.Rounded.Business,
                        text = achievement.issuer,
                        accentColor = catColor
                    )

                    // Description
                    if (achievement.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = achievement.description,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tags row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = catColor.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(catIcon, contentDescription = null, modifier = Modifier.size(12.dp), tint = catColor)
                        Text(text = catLabel, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = catColor)
                    }
                }

                // Credential ID badge
                if (achievement.credentialId.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Tag, contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = achievement.credentialId, fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Expiry badge
                if (achievement.expiryDate != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isExpired) AchievementColors.deleteRedBg else Color(0xFFF0FDF4)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                if (isExpired) Icons.Rounded.Warning else Icons.Rounded.CheckCircle,
                                contentDescription = null, modifier = Modifier.size(12.dp),
                                tint = if (isExpired) AchievementColors.deleteRed else Color(0xFF059669)
                            )
                            Text(
                                text = if (isExpired) "Expired" else "Berlaku",
                                fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                                color = if (isExpired) AchievementColors.deleteRed else Color(0xFF059669)
                            )
                        }
                    }
                }
            }

            // Expiry date detail
            if (achievement.expiryDate != null) {
                Spacer(modifier = Modifier.height(8.dp))
                AchievementInfoRow(
                    icon = Icons.Rounded.CalendarToday,
                    text = "Berlaku hingga: ${dateFormat.format(Date(achievement.expiryDate))}",
                    accentColor = if (isExpired) AchievementColors.deleteRed else catColor
                )
            }

            // Credential URL
            if (achievement.credentialUrl.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    onClick = onOpenUrl,
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF2563EB).copy(alpha = 0.06f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(
                                    color = Color(0xFF2563EB).copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.Link, contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFF2563EB)
                            )
                        }
                        Text(
                            text = "Lihat Kredensial",
                            fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2563EB),
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            Icons.Rounded.OpenInNew, contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF2563EB).copy(alpha = 0.6f)
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
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                        Icon(Icons.Rounded.Edit, null, Modifier.size(15.dp), tint = MaterialTheme.colorScheme.primary)
                        Text("Edit", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Delete
                Surface(
                    onClick = { showDeleteConfirmation = true },
                    shape = RoundedCornerShape(10.dp),
                    color = AchievementColors.deleteRedBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(Icons.Rounded.DeleteOutline, null, Modifier.size(15.dp), tint = AchievementColors.deleteRed)
                        Text("Hapus", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AchievementColors.deleteRed)
                    }
                }
            }
        }
    }

    // Delete dialog
    if (showDeleteConfirmation) {
        AchievementDeleteDialog(
            title = achievement.title,
            onConfirm = { onDelete(); showDeleteConfirmation = false },
            onDismiss = { showDeleteConfirmation = false }
        )
    }
}

@Composable
private fun AchievementInfoRow(
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
                .background(color = accentColor.copy(alpha = 0.08f), shape = RoundedCornerShape(7.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, Modifier.size(14.dp), tint = accentColor.copy(alpha = 0.7f))
        }
        Text(
            text = text, fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
            maxLines = 1, overflow = TextOverflow.Ellipsis
        )
    }
}

// ══════════════════════════════════════════════════════════════════════
// ── DELETE DIALOG ────────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════
@Composable
private fun AchievementDeleteDialog(
    title: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        icon = {
            Box(
                modifier = Modifier.size(52.dp).background(AchievementColors.deleteRedBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.DeleteOutline, null, Modifier.size(26.dp), tint = AchievementColors.deleteRed)
            }
        },
        title = {
            Text("Hapus Achievement?", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        },
        text = {
            Text(
                "\"$title\" akan dihapus secara permanen. Tindakan ini tidak bisa dibatalkan.",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                lineHeight = 20.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = AchievementColors.deleteRed),
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
// ── PREMIUM ACHIEVEMENT DIALOG ───────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumAchievementDialog(
    isEditing: Boolean,
    title: String, onTitleChange: (String) -> Unit,
    issuer: String, onIssuerChange: (String) -> Unit,
    category: String, onCategoryChange: (String) -> Unit,
    description: String, onDescriptionChange: (String) -> Unit,
    credentialUrl: String, onCredentialUrlChange: (String) -> Unit,
    credentialId: String, onCredentialIdChange: (String) -> Unit,
    importance: String, onImportanceChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val categories = listOf(
        "CERTIFICATION" to "Sertifikasi",
        "AWARD" to "Penghargaan",
        "COMPETITION" to "Kompetisi",
        "BADGE" to "Badge"
    )

    val importanceLevels = listOf(
        "1" to "Biasa",
        "2" to "Penting",
        "3" to "Sangat Penting"
    )

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
                        .background(brush = Brush.linearGradient(AchievementColors.fabGradient), shape = RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isEditing) Icons.Rounded.Edit else Icons.Rounded.EmojiEvents,
                        contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = if (isEditing) "Edit Achievement" else "Tambah Achievement",
                        fontSize = 19.sp, fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface, letterSpacing = (-0.3).sp
                    )
                    Text(
                        text = if (isEditing) "Ubah informasi pencapaian" else "Catat pencapaianmu",
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
                // Section: Identitas — reuse DialogSectionLabel from SubjectsScreen
                DialogSectionLabel("Informasi Utama")

                // Reuse PremiumTextField from SubjectsScreen
                PremiumTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    label = "Judul Achievement",
                    placeholder = "Masukkan judul..."
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PremiumTextField(
                        value = issuer,
                        onValueChange = onIssuerChange,
                        label = "Penerbit",
                        placeholder = "Nama institusi",
                        modifier = Modifier.weight(1f),
                        leadingIcon = Icons.Rounded.Business
                    )
                    PremiumTextField(
                        value = credentialId,
                        onValueChange = onCredentialIdChange,
                        label = "ID Kredensial",
                        placeholder = "Opsional",
                        modifier = Modifier.weight(0.7f),
                        leadingIcon = Icons.Rounded.Tag
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    label = { Text("Deskripsi") },
                    placeholder = {
                        Text("Deskripsi singkat...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        focusedBorderColor = AchievementColors.amber,
                        focusedLabelColor = AchievementColors.amber,
                        cursorColor = AchievementColors.amber
                    )
                )

                // Section: Kredensial
                DialogSectionLabel("Kredensial")

                OutlinedTextField(
                    value = credentialUrl,
                    onValueChange = onCredentialUrlChange,
                    label = { Text("URL Kredensial") },
                    placeholder = {
                        Text("https://...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                    },
                    leadingIcon = {
                        Icon(Icons.Rounded.Link, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        focusedBorderColor = AchievementColors.amber,
                        focusedLabelColor = AchievementColors.amber,
                        cursorColor = AchievementColors.amber
                    )
                )

                // Section: Kategori
                DialogSectionLabel("Kategori")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { (key, label) ->
                        val isSelected = category == key
                        val catColor = AchievementColors.categoryColor(key)
                        val catIcon = AchievementColors.categoryIcon(key)

                        Surface(
                            onClick = { onCategoryChange(key) },
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
                                    catIcon, null, Modifier.size(20.dp),
                                    tint = if (isSelected) catColor
                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                )
                                Text(
                                    text = label, fontSize = 10.sp, maxLines = 1,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) catColor else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Section: Importance
                DialogSectionLabel("Tingkat Kepentingan")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    importanceLevels.forEach { (value, label) ->
                        val isSelected = importance == value
                        val levelColor = when (value) {
                            "3" -> AchievementColors.importanceHigh
                            "2" -> AchievementColors.importanceMed
                            else -> AchievementColors.importanceLow
                        }

                        Surface(
                            onClick = { onImportanceChange(value) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) levelColor.copy(alpha = 0.12f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = if (isSelected)
                                androidx.compose.foundation.BorderStroke(1.5.dp, levelColor.copy(alpha = 0.5f))
                            else null,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Star visual
                                Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                                    repeat(value.toInt()) {
                                        Icon(
                                            Icons.Rounded.Star, null, Modifier.size(14.dp),
                                            tint = if (isSelected) levelColor
                                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                        )
                                    }
                                }
                                Text(
                                    text = label, fontSize = 10.sp, maxLines = 1,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) levelColor else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = title.isNotBlank() && issuer.isNotBlank(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AchievementColors.amber),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Icon(
                    if (isEditing) Icons.Rounded.Check else Icons.Rounded.EmojiEvents,
                    null, Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    if (isEditing) "Simpan" else "Tambah",
                    fontSize = 14.sp, fontWeight = FontWeight.SemiBold
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