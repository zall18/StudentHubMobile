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
import com.example.studenthub.data.LearningResource
import android.content.Intent
import android.net.Uri

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

    val categories = listOf("ALL", "WEBSITE", "ARTICLE", "VIDEO", "PDF", "BOOK", "GITHUB")
    val difficulties = listOf("ALL", "BEGINNER", "INTERMEDIATE", "ADVANCED")

    // Filter and search logic
    val filteredResources: List<LearningResource> = remember(allResources, favoriteResources, selectedCategory, selectedDifficulty, searchQuery) {
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

        // 2. Sort it INSIDE the remember block and return it
        searchFiltered.sortedByDescending { it: LearningResource -> it.savedAt }
    }

    Scaffold(
        containerColor = Color(0xFFF8F9FA),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF2563EB),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Tambah Resource",
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
                        text = "Learning Resources",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${allResources.size} resources • ${favoriteResources.size} favorit",
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari resource...") },
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
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Kategori",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF374151)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories) { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = {
                                    Text(
                                        text = when (cat) {
                                            "ALL" -> "Semua"
                                            "FAVORITES" -> "Favorit"
                                            "WEBSITE" -> "Website"
                                            "ARTICLE" -> "Artikel"
                                            "VIDEO" -> "Video"
                                            "PDF" -> "PDF"
                                            "BOOK" -> "Buku"
                                            "GITHUB" -> "GitHub"
                                            else -> cat
                                        },
                                        fontSize = 12.sp,
                                        fontWeight = if (selectedCategory == cat) FontWeight.Medium else FontWeight.Normal
                                    )
                                },
                                leadingIcon = if (selectedCategory == cat) {
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

            // Difficulty Filters
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Tingkat Kesulitan",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF374151)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        difficulties.forEach { diff ->
                            FilterChip(
                                selected = selectedDifficulty == diff,
                                onClick = { selectedDifficulty = diff },
                                label = {
                                    Text(
                                        text = when (diff) {
                                            "ALL" -> "Semua"
                                            "BEGINNER" -> "Pemula"
                                            "INTERMEDIATE" -> "Menengah"
                                            "ADVANCED" -> "Lanjutan"
                                            else -> diff
                                        },
                                        fontSize = 12.sp
                                    )
                                },
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

            // Resource count
            item {
                if (filteredResources.isNotEmpty()) {
                    Text(
                        text = "${filteredResources.size} resource ditemukan",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF374151)
                    )
                }
            }

            // Resource List
            if (filteredResources.isEmpty()) {
                item {
                    EmptyResourceState()
                }
            } else {
                items(filteredResources, key = { it.id }) { resource ->
                    EnhancedResourceCard(
                        resource = resource,
                        onToggleFavorite = {
                            viewModel.toggleFavoriteResource(resource.id, !resource.isFavorite)
                        },
                        onDelete = { viewModel.deleteLearningResource(resource.id) },
                        onOpenUrl = {
                            val context = LocalContext.current
                            if (resource.url.isNotBlank()) {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(resource.url))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    print(e.message)
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

    // Add Resource Dialog
    if (showAddDialog) {
        AddResourceDialog(
            title = title,
            onTitleChange = { title = it },
            category = category,
            onCategoryChange = { category = it },
            url = url,
            onUrlChange = { url = it },
            description = description,
            onDescriptionChange = { description = it },
            difficulty = difficulty,
            onDifficultyChange = { difficulty = it },
            categories = categories.filter { it != "ALL" && it != "FAVORITES" },
            difficulties = difficulties.filter { it != "ALL" },
            onDismiss = {
                showAddDialog = false
                title = ""
                category = "WEBSITE"
                url = ""
                description = ""
                difficulty = "INTERMEDIATE"
            },
            onConfirm = {
                if (title.isNotBlank()) {
                    viewModel.addLearningResource(
                        title = title,
                        category = category,
                        url = url,
                        description = description,
                        difficulty = difficulty
                    )
                    showAddDialog = false
                    title = ""
                    category = "WEBSITE"
                    url = ""
                    description = ""
                    difficulty = "INTERMEDIATE"
                }
            }
        )
    }
}

@Composable
fun EmptyResourceState() {
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
                text = "Belum ada resource",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF6B7280)
            )
            Text(
                text = "Tambahkan resource belajar untuk memulai",
                fontSize = 13.sp,
                color = Color(0xFF9CA3AF),
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun EnhancedResourceCard(
    resource: LearningResource,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit,
    onOpenUrl: @Composable () -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    val categoryColor = remember(resource.category) {
        when (resource.category) {
            "WEBSITE" -> Color(0xFF2563EB)
            "ARTICLE" -> Color(0xFF7C3AED)
            "VIDEO" -> Color(0xFFDC2626)
            "PDF" -> Color(0xFFEA580C)
            "BOOK" -> Color(0xFF059669)
            "GITHUB" -> Color(0xFF1F2937)
            else -> Color(0xFF6B7280)
        }
    }

    val difficultyColor = remember(resource.difficulty) {
        when (resource.difficulty) {
            "BEGINNER" -> Color(0xFF10B981)
            "INTERMEDIATE" -> Color(0xFFF59E0B)
            "ADVANCED" -> Color(0xFFEF4444)
            else -> Color(0xFF6B7280)
        }
    }

    val categoryIcon = remember(resource.category) {
        when (resource.category) {
            "WEBSITE" -> Icons.Outlined.Language
            "ARTICLE" -> Icons.Outlined.Article
            "VIDEO" -> Icons.Outlined.PlayCircle
            "PDF" -> Icons.Outlined.PictureAsPdf
            "BOOK" -> Icons.Outlined.MenuBook
            "GITHUB" -> Icons.Outlined.Code
            else -> Icons.Outlined.Link
        }
    }
    @Composable // <-- THIS is the magic word that fixes your error!
    fun ResourceItemCard(
        resource: LearningResource, // Replace YourModel with your actual data class name
        categoryColor: Color,
        difficultyColor: Color,
        onOpenUrl: () -> Unit
    ) {
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
                            contentDescription = resource.category,
                            modifier = Modifier.size(24.dp),
                            tint = categoryColor
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        // Title
                        Text(
                            text = resource.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF111827),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Description
                        if (resource.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = resource.description,
                                fontSize = 13.sp,
                                color = Color(0xFF6B7280),
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
                                Text(
                                    text = when (resource.category) {
                                        "WEBSITE" -> "Website"
                                        "ARTICLE" -> "Artikel"
                                        "VIDEO" -> "Video"
                                        "PDF" -> "PDF"
                                        "BOOK" -> "Buku"
                                        "GITHUB" -> "GitHub"
                                        else -> resource.category
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = categoryColor
                                )
                            }

                            // Difficulty badge
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = difficultyColor.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = when (resource.difficulty) {
                                        "BEGINNER" -> "Pemula"
                                        "INTERMEDIATE" -> "Menengah"
                                        "ADVANCED" -> "Lanjutan"
                                        else -> resource.difficulty
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = difficultyColor
                                )
                            }
                        }

                        // URL
                        if (resource.url.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .clickable { onOpenUrl() }
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.OpenInNew,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = Color(0xFF2563EB)
                                )
                                Text(
                                    text = resource.url,
                                    fontSize = 12.sp,
                                    color = Color(0xFF2563EB),
                                    textDecoration = TextDecoration.Underline,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Favorite button
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            if (resource.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = if (resource.isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (resource.isFavorite) Color(0xFFE91E63) else Color(0xFFD1D5DB),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
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
    }


    // Delete Confirmation
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    text = "Hapus Resource",
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text("Apakah kamu yakin ingin menghapus \"${resource.title}\"?")
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
fun AddResourceDialog(
    title: String,
    onTitleChange: (String) -> Unit,
    category: String,
    onCategoryChange: (String) -> Unit,
    url: String,
    onUrlChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    difficulty: String,
    onDifficultyChange: (String) -> Unit,
    categories: List<String>,
    difficulties: List<String>,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "Tambah Resource",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF111827)
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    label = { Text("Judul") },
                    placeholder = { Text("Masukkan judul resource...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // URL
                OutlinedTextField(
                    value = url,
                    onValueChange = onUrlChange,
                    label = { Text("URL") },
                    placeholder = { Text("https://...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    shape = RoundedCornerShape(12.dp)
                )

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
                        categories.forEach { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { onCategoryChange(cat) },
                                label = {
                                    Text(
                                        text = when (cat) {
                                            "WEBSITE" -> "Web"
                                            "ARTICLE" -> "Artikel"
                                            "VIDEO" -> "Video"
                                            "PDF" -> "PDF"
                                            "BOOK" -> "Buku"
                                            "GITHUB" -> "GitHub"
                                            else -> cat
                                        },
                                        fontSize = 12.sp
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF2563EB),
                                    selectedLabelColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }

                // Difficulty
                Column {
                    Text(
                        text = "Kesulitan",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF374151)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        difficulties.forEach { diff ->
                            FilterChip(
                                selected = difficulty == diff,
                                onClick = { onDifficultyChange(diff) },
                                label = {
                                    Text(
                                        text = when (diff) {
                                            "BEGINNER" -> "Pemula"
                                            "INTERMEDIATE" -> "Menengah"
                                            "ADVANCED" -> "Lanjutan"
                                            else -> diff
                                        },
                                        fontSize = 12.sp
                                    )
                                },
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
                enabled = title.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2563EB)
                )
            ) {
                Text(
                    text = "Simpan",
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