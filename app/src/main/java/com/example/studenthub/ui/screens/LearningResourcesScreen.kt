package com.example.studenthub.ui.screens

import StudentHubViewModel
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.studenthub.data.LearningResource

// ── Resource-specific colors ─────────────────────────────────────────────
private object ResourceColors {
    val primary = Color(0xFF6366F1) // Indigo theme for Learning
    val primaryLight = Color(0xFFEEF2FF)

    val gradientHeader = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
    val fabGradient = listOf(Color(0xFF6366F1), Color(0xFF4F46E5))

    val deleteRed = Color(0xFFDC2626)
    val deleteRedBg = Color(0xFFFEF2F2)
    val favoritePink = Color(0xFFEC4899)

    fun categoryColor(category: String): Color = when (category) {
        "WEBSITE" -> Color(0xFF2563EB)
        "ARTICLE" -> Color(0xFF7C3AED)
        "VIDEO" -> Color(0xFFDC2626)
        "PDF" -> Color(0xFFEA580C)
        "BOOK" -> Color(0xFF059669)
        "GITHUB" -> Color(0xFF1F2937)
        "FAVORITES" -> favoritePink
        else -> Color(0xFF6B7280)
    }

    fun categoryIcon(category: String): ImageVector = when (category) {
        "WEBSITE" -> Icons.Rounded.Language
        "ARTICLE" -> Icons.Rounded.Article
        "VIDEO" -> Icons.Rounded.PlayCircle
        "PDF" -> Icons.Rounded.PictureAsPdf
        "BOOK" -> Icons.Rounded.MenuBook
        "GITHUB" -> Icons.Rounded.Code
        "FAVORITES" -> Icons.Rounded.Favorite
        "ALL" -> Icons.Rounded.Dashboard
        else -> Icons.Rounded.Link
    }

    fun categoryLabel(category: String): String = when (category) {
        "ALL" -> "Semua"
        "FAVORITES" -> "Favorit"
        "WEBSITE" -> "Website"
        "ARTICLE" -> "Artikel"
        "VIDEO" -> "Video"
        "PDF" -> "PDF"
        "BOOK" -> "Buku"
        "GITHUB" -> "GitHub"
        else -> category
    }

    fun difficultyColor(diff: String): Color = when (diff) {
        "BEGINNER" -> Color(0xFF10B981) // Emerald
        "INTERMEDIATE" -> Color(0xFFF59E0B) // Amber
        "ADVANCED" -> Color(0xFFEF4444) // Red
        else -> Color(0xFF6B7280)
    }

    fun difficultyLabel(diff: String): String = when (diff) {
        "ALL" -> "Semua"
        "BEGINNER" -> "Pemula"
        "INTERMEDIATE" -> "Menengah"
        "ADVANCED" -> "Lanjutan"
        else -> diff
    }
}

// ══════════════════════════════════════════════════════════════════════
// ── MAIN SCREEN ──────────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningResourcesScreen(viewModel: StudentHubViewModel) {
    val allResources by viewModel.allResources.collectAsStateWithLifecycle()
    val favoriteResources by viewModel.favoriteResources.collectAsStateWithLifecycle()

    var selectedCategory by remember { mutableStateOf("ALL") }
    var selectedDifficulty by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    // Form fields
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("WEBSITE") }
    var url by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var difficulty by remember { mutableStateOf("INTERMEDIATE") }

    fun resetFormFields() {
        title = ""; category = "WEBSITE"; url = ""; description = ""; difficulty = "INTERMEDIATE"
    }

    val categories = listOf("ALL", "FAVORITES", "WEBSITE", "ARTICLE", "VIDEO", "PDF", "BOOK", "GITHUB")
    val difficulties = listOf("ALL", "BEGINNER", "INTERMEDIATE", "ADVANCED")

    // Filter logic
    val filteredResources = remember(allResources, favoriteResources, selectedCategory, selectedDifficulty, searchQuery) {
        val baseResources = when (selectedCategory) {
            "FAVORITES" -> favoriteResources
            "ALL" -> allResources
            else -> allResources.filter { it.category == selectedCategory }
        }

        val difficultyFiltered = if (selectedDifficulty != "ALL") {
            baseResources.filter { it.difficulty == selectedDifficulty }
        } else {
            baseResources
        }

        val searchFiltered = if (searchQuery.isNotBlank()) {
            difficultyFiltered.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.description.contains(searchQuery, ignoreCase = true) ||
                        it.category.contains(searchQuery, ignoreCase = true)
            }
        } else {
            difficultyFiltered
        }

        searchFiltered.sortedByDescending { it.savedAt }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = { ResourceGradientFab(onClick = { resetFormFields(); showAddDialog = true }) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header
            item { ResourceScreenHeader(total = allResources.size, favorites = favoriteResources.size) }

            // Search Bar
            item { ResourceSearchBar(query = searchQuery, onQueryChange = { searchQuery = it }) }

            // Category Filters
            item {
                ResourceFilterSection(
                    title = "Kategori",
                    items = categories,
                    selectedItem = selectedCategory,
                    onItemSelected = { selectedCategory = it },
                    getLabel = { ResourceColors.categoryLabel(it) },
                    getIcon = { ResourceColors.categoryIcon(it) },
                    getColor = { if (it == "ALL") ResourceColors.primary else ResourceColors.categoryColor(it) }
                )
            }

            // Difficulty Filters
            item {
                ResourceFilterSection(
                    title = "Tingkat Kesulitan",
                    items = difficulties,
                    selectedItem = selectedDifficulty,
                    onItemSelected = { selectedDifficulty = it },
                    getLabel = { ResourceColors.difficultyLabel(it) },
                    getIcon = { ResourceColors.categoryIcon(it) },
                    getColor = { if (it == "ALL") ResourceColors.primary else ResourceColors.difficultyColor(it) }
                )
            }

            // Result Count
            item {
                if (filteredResources.isNotEmpty()) {
                    Text(
                        text = "${filteredResources.size} resource ditemukan",
                        fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            }

            // Resource List
            if (filteredResources.isEmpty()) {
                item { PremiumEmptyResourceState(isSearch = searchQuery.isNotBlank()) }
            } else {

                items(filteredResources, key = { it.id }) { resource ->
                    val context = LocalContext.current
                    PremiumResourceCard(
                        resource = resource,
                        onToggleFavorite = { viewModel.toggleFavoriteResource(resource.id, !resource.isFavorite) },
                        onDelete = { viewModel.deleteLearningResource(resource.id) },
                        onOpenUrl = {
                            if (resource.url.isNotBlank()) {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(resource.url))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    if (showAddDialog) {
        PremiumAddResourceDialog(
            title = title, onTitleChange = { title = it },
            category = category, onCategoryChange = { category = it },
            url = url, onUrlChange = { url = it },
            description = description, onDescriptionChange = { description = it },
            difficulty = difficulty, onDifficultyChange = { difficulty = it },
            categories = categories.filter { it != "ALL" && it != "FAVORITES" },
            difficulties = difficulties.filter { it != "ALL" },
            onDismiss = { showAddDialog = false; resetFormFields() },
            onConfirm = {
                if (title.isNotBlank()) {
                    viewModel.addLearningResource(
                        title = title, category = category,
                        url = url, description = description, difficulty = difficulty
                    )
                    showAddDialog = false; resetFormFields()
                }
            }
        )
    }
}

// ══════════════════════════════════════════════════════════════════════
// ── HEADER ───────────────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════
@Composable
private fun ResourceScreenHeader(total: Int, favorites: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Learning Resources",
                fontSize = 26.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(shape = RoundedCornerShape(6.dp), color = ResourceColors.primaryLight) {
                    Text(
                        text = "$total resources",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                        color = ResourceColors.primary
                    )
                }
                Text("•", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = "$favorites favorit",
                    fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        Box(
            modifier = Modifier.size(44.dp)
                .background(Brush.linearGradient(ResourceColors.gradientHeader), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.MenuBook, null, tint = Color.White, modifier = Modifier.size(22.dp))
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
// ── SEARCH BAR ───────────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ResourceSearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query, onValueChange = onQueryChange,
        placeholder = { Text("Cari judul, deskripsi, atau kategori...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), fontSize = 14.sp) },
        leadingIcon = { Icon(Icons.Rounded.Search, null, tint = ResourceColors.primary) },
        trailingIcon = {
            if (query.isNotBlank()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Rounded.Close, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp), ambientColor = ResourceColors.primary.copy(0.05f)),
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = ResourceColors.primary.copy(alpha = 0.5f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            cursorColor = ResourceColors.primary
        )
    )
}

// ══════════════════════════════════════════════════════════════════════
// ── FILTER SECTION ───────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════
@Composable
private fun ResourceFilterSection(
    title: String, items: List<String>, selectedItem: String,
    onItemSelected: (String) -> Unit, getLabel: (String) -> String,
    getIcon: ((String) -> ImageVector)? = null, getColor: (String) -> Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground, letterSpacing = (-0.3).sp
        )
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items.forEach { item ->
                val isSelected = selectedItem == item
                val itemColor = getColor(item)

                Surface(
                    onClick = { onItemSelected(item) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) itemColor else MaterialTheme.colorScheme.surface,
                    border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)) else null,
                    shadowElevation = if (isSelected) 2.dp else 0.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (getIcon != null) {
                            val icon = getIcon(item)
                            Icon(icon, null, Modifier.size(16.dp), tint = if (isSelected) Color.White else itemColor.copy(alpha = 0.8f))
                        }
                        Text(
                            text = getLabel(item), fontSize = 12.sp,
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
// ── PREMIUM RESOURCE CARD ────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════
@Composable
fun PremiumResourceCard(
    resource: LearningResource,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit,
    onOpenUrl: () -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    val catColor = ResourceColors.categoryColor(resource.category)
    val catIcon = ResourceColors.categoryIcon(resource.category)
    val catLabel = ResourceColors.categoryLabel(resource.category)

    val diffColor = ResourceColors.difficultyColor(resource.difficulty)
    val diffLabel = ResourceColors.difficultyLabel(resource.difficulty)

    Card(
        modifier = Modifier.fillMaxWidth().shadow(
            elevation = 4.dp, shape = RoundedCornerShape(18.dp),
            ambientColor = catColor.copy(alpha = 0.06f), spotColor = catColor.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                // Category Icon
                Box(
                    modifier = Modifier.size(52.dp).background(
                        brush = Brush.linearGradient(listOf(catColor, catColor.copy(alpha = 0.7f))),
                        shape = RoundedCornerShape(14.dp)
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(catIcon, null, Modifier.size(26.dp), tint = Color.White)
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    // Title
                    Text(
                        text = resource.title,
                        fontSize = 16.sp, fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2, overflow = TextOverflow.Ellipsis,
                        lineHeight = 21.sp, letterSpacing = (-0.2).sp
                    )

                    // Description
                    if (resource.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = resource.description, fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tags row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(shape = RoundedCornerShape(6.dp), color = catColor.copy(alpha = 0.1f)) {
                            Text(
                                text = catLabel, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = catColor
                            )
                        }
                        Surface(shape = RoundedCornerShape(6.dp), color = diffColor.copy(alpha = 0.1f)) {
                            Text(
                                text = diffLabel, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = diffColor
                            )
                        }
                    }

                    // URL
                    if (resource.url.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.clip(RoundedCornerShape(6.dp)).clickable(onClick = onOpenUrl).padding(vertical = 4.dp, horizontal = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Rounded.OpenInNew, null, Modifier.size(14.dp), tint = ResourceColors.primary)
                            Text(
                                text = resource.url.take(40) + if (resource.url.length > 40) "..." else "",
                                fontSize = 12.sp, fontWeight = FontWeight.Medium,
                                color = ResourceColors.primary, textDecoration = TextDecoration.Underline,
                                maxLines = 1, overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Favorite button
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(40.dp).offset(x = 8.dp, y = (-8).dp)
                ) {
                    Icon(
                        if (resource.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (resource.isFavorite) ResourceColors.favoritePink else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = { showDeleteConfirmation = true },
                    shape = RoundedCornerShape(10.dp),
                    color = ResourceColors.deleteRedBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(Icons.Rounded.DeleteOutline, null, Modifier.size(15.dp), tint = ResourceColors.deleteRed)
                        Text("Hapus", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ResourceColors.deleteRed)
                    }
                }
            }
        }
    }

    if (showDeleteConfirmation) {
        ResourceDeleteDialog(
            title = resource.title,
            onConfirm = { onDelete(); showDeleteConfirmation = false },
            onDismiss = { showDeleteConfirmation = false }
        )
    }
}

// ══════════════════════════════════════════════════════════════════════
// ── EMPTY STATE ──────────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════
@Composable
private fun PremiumEmptyResourceState(isSearch: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(20.dp), ambientColor = ResourceColors.primary.copy(alpha = 0.05f)),
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
                modifier = Modifier.size(72.dp).background(ResourceColors.primary.copy(alpha = 0.08f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isSearch) Icons.Rounded.SearchOff else Icons.Rounded.MenuBook, null,
                    Modifier.size(36.dp), tint = ResourceColors.primary.copy(alpha = 0.4f)
                )
            }
            Text(
                text = if (isSearch) "Resource tidak ditemukan" else "Belum ada resource",
                fontSize = 17.sp, fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (isSearch) "Coba gunakan kata kunci lain\natau ubah filter kategori" else "Tambahkan materi belajar\nuntuk memulai",
                fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                lineHeight = 19.sp, textAlign = TextAlign.Center
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
// ── GRADIENT FAB ─────────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════
@Composable
private fun ResourceGradientFab(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick, shape = CircleShape,
        containerColor = Color.Transparent, contentColor = Color.White,
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp, pressedElevation = 12.dp),
        modifier = Modifier
            .shadow(10.dp, CircleShape, ambientColor = ResourceColors.primary.copy(alpha = 0.3f), spotColor = ResourceColors.primary.copy(alpha = 0.4f))
            .background(brush = Brush.linearGradient(ResourceColors.fabGradient), shape = CircleShape)
    ) {
        Icon(Icons.Rounded.Add, "Tambah Resource", Modifier.size(28.dp))
    }
}

// ══════════════════════════════════════════════════════════════════════
// ── DELETE DIALOG ────────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════
@Composable
private fun ResourceDeleteDialog(title: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        icon = {
            Box(Modifier.size(52.dp).background(ResourceColors.deleteRedBg, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.DeleteOutline, null, Modifier.size(26.dp), tint = ResourceColors.deleteRed)
            }
        },
        title = {
            Text("Hapus Resource?", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        },
        text = {
            Text(
                "\"$title\" akan dihapus secara permanen dari daftar belajarmu.",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), lineHeight = 20.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = ResourceColors.deleteRed),
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
// ── ADD RESOURCE DIALOG ──────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumAddResourceDialog(
    title: String, onTitleChange: (String) -> Unit,
    category: String, onCategoryChange: (String) -> Unit,
    url: String, onUrlChange: (String) -> Unit,
    description: String, onDescriptionChange: (String) -> Unit,
    difficulty: String, onDifficultyChange: (String) -> Unit,
    categories: List<String>, difficulties: List<String>,
    onDismiss: () -> Unit, onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier.size(42.dp)
                        .background(Brush.linearGradient(ResourceColors.fabGradient), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.MenuBook, null, tint = Color.White, modifier = Modifier.size(22.dp))
                }
                Column {
                    Text("Tambah Resource", fontSize = 19.sp, fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface, letterSpacing = (-0.3).sp)
                    Text("Simpan materi belajarmu", fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Reuse from SubjectsScreen
                DialogSectionLabel("Detail Resource")

                // Reuse from SubjectsScreen
                PremiumTextField(
                    value = title, onValueChange = onTitleChange,
                    label = "Judul Resource", placeholder = "Masukkan judul..."
                )

                OutlinedTextField(
                    value = url, onValueChange = onUrlChange,
                    label = { Text("URL Link (Opsional)") },
                    placeholder = { Text("https://...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f)) },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        focusedBorderColor = ResourceColors.primary,
                        focusedLabelColor = ResourceColors.primary,
                        cursorColor = ResourceColors.primary
                    )
                )

                OutlinedTextField(
                    value = description, onValueChange = onDescriptionChange,
                    label = { Text("Deskripsi (Opsional)") },
                    placeholder = { Text("Catatan singkat...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f)) },
                    modifier = Modifier.fillMaxWidth(), maxLines = 3,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        focusedBorderColor = ResourceColors.primary,
                        focusedLabelColor = ResourceColors.primary,
                        cursorColor = ResourceColors.primary
                    )
                )

                DialogSectionLabel("Kategori")
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = category == cat
                        val catColor = ResourceColors.categoryColor(cat)
                        Surface(
                            onClick = { onCategoryChange(cat) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) catColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = if (isSelected) BorderStroke(1.5.dp, catColor.copy(alpha = 0.5f)) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(ResourceColors.categoryIcon(cat), null, Modifier.size(16.dp),
                                    tint = if (isSelected) catColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                                Text(ResourceColors.categoryLabel(cat), fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) catColor else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                DialogSectionLabel("Tingkat Kesulitan")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    difficulties.forEach { diff ->
                        val isSelected = difficulty == diff
                        val diffColor = ResourceColors.difficultyColor(diff)
                        Surface(
                            onClick = { onDifficultyChange(diff) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) diffColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = if (isSelected) BorderStroke(1.5.dp, diffColor.copy(alpha = 0.5f)) else null,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(ResourceColors.difficultyLabel(diff), fontSize = 11.sp, maxLines = 1,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) diffColor else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm, enabled = title.isNotBlank(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ResourceColors.primary),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Icon(Icons.Rounded.Save, null, Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Simpan", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
            }
        }
    )
}