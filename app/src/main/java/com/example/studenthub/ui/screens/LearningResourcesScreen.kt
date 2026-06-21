package com.example.studenthub.ui.screens

import StudentHubViewModel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.studenthub.data.LearningResource
import java.net.URL

@Composable
fun LearningResourcesScreen(viewModel: StudentHubViewModel) {
    val allResources by viewModel.allResources.collectAsStateWithLifecycle()
    val favoriteResources by viewModel.favoriteResources.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf("ALL") }
    var showAddDialog by remember { mutableStateOf(false) }

    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("WEBSITE") }
    var url by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var difficulty by remember { mutableStateOf("INTERMEDIATE") }
    var notes by remember { mutableStateOf("") }

    val categories = listOf("ALL", "WEBSITE", "ARTICLE", "VIDEO", "PDF", "BOOK", "GITHUB")
    val difficulties = listOf("BEGINNER", "INTERMEDIATE", "ADVANCED")

    val visibleResources = when (selectedTab) {
        "FAVORITES" -> favoriteResources
        "WEBSITE", "ARTICLE", "VIDEO", "PDF", "BOOK", "GITHUB" -> allResources.filter { it.category == selectedTab }
        else -> allResources
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF0066FF),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Resource")
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
                    text = "Learning Resources",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Text(
                    text = "Simpan artikel, video, PDF, dan referensi penting",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = selectedTab == "ALL",
                            onClick = { selectedTab = "ALL" },
                            label = { Text("Semua") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedTab == "FAVORITES",
                            onClick = { selectedTab = "FAVORITES" },
                            label = { Text("Favorit") }
                        )
                    }
                    categories.drop(1).forEach { tab ->
                        item {
                            FilterChip(
                                selected = selectedTab == tab,
                                onClick = { selectedTab = tab },
                                label = { Text(tab) }
                            )
                        }
                    }
                }
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    difficulties.forEach { diff ->
                        item {
                            FilterChip(
                                selected = false,
                                onClick = {
                                    selectedTab = when (selectedTab) {
                                        "FAVORITES" -> "FAVORITES"
                                        else -> selectedTab
                                    }
                                    difficulty = diff
                                },
                                label = { Text("${diff.lowercase().replaceFirstChar { it.uppercase() }}") }
                            )
                        }
                    }
                }
            }

            if (visibleResources.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Belum ada resource", color = Color.Gray)
                    }
                }
            } else {
                items(visibleResources, key = { it.id }) { resource ->
                    LearningResourceCard(
                        resource = resource,
                        onToggleFavorite = { viewModel.toggleFavoriteResource(resource.id, !resource.isFavorite) },
                        onDelete = { viewModel.deleteLearningResource(resource.id) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                title = ""
                category = "WEBSITE"
                url = ""
                description = ""
                difficulty = "INTERMEDIATE"
                notes = ""
            },
            title = { Text("Tambah Learning Resource") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = url, onValueChange = { url = it }, label = { Text("URL") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())
                    Text("Category", fontWeight = FontWeight.Medium)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categories.drop(1).size) { index ->
                            val cat = categories.drop(1)[index]
                            FilterChip(selected = category == cat, onClick = { category = cat }, label = { Text(cat) })
                        }
                    }
                    Text("Difficulty", fontWeight = FontWeight.Medium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        difficulties.forEach { diff ->
                            FilterChip(selected = difficulty == diff, onClick = { difficulty = diff }, label = { Text(diff) })
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
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
                        notes = ""
                    }
                }) { Text("Simpan") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Batal") }
            }
        )
    }
}

@Composable
fun LearningResourceCard(
    resource: LearningResource,
    onToggleFavorite: () -> Unit,
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = resource.title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color(0xFF1A1A1A))
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFE3F2FD)) {
                            Text(text = resource.category, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, color = Color(0xFF1E88E5))
                        }
                        Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFF3E5F5)) {
                            Text(text = resource.difficulty, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, color = Color(0xFF6A1B9A))
                        }
                    }
                }
                Row {
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            if (resource.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (resource.isFavorite) Color(0xFFE91E63) else Color.Gray
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFD32F2F))
                    }
                }
            }
            if (resource.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = resource.description, fontSize = 12.sp, color = Color.Gray, maxLines = 2)
            }
            if (resource.url.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = resource.url, fontSize = 11.sp, color = Color(0xFF1E88E5), maxLines = 1)
            }
        }
    }
}

