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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AchievementsScreen(viewModel: StudentHubViewModel) {
    val achievements by viewModel.allAchievements.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf("CERTIFICATION") }
    var showAddDialog by remember { mutableStateOf(false) }

    var title by remember { mutableStateOf("") }
    var issuer by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("CERTIFICATION") }
    var description by remember { mutableStateOf("") }
    var credentialUrl by remember { mutableStateOf("") }
    var credentialId by remember { mutableStateOf("") }
    var filePath by remember { mutableStateOf("") }
    var importance by remember { mutableStateOf("2") }
    var expiryDate by remember { mutableStateOf("") }

    val tabs = listOf("CERTIFICATION", "AWARD", "COMPETITION", "BADGE", "ALL")
    val visibleAchievements = when (selectedTab) {
        "ALL" -> achievements
        else -> achievements.filter { it.category == selectedTab }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF0066FF),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Achievement")
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
                Text(text = "Achievements & Certifications", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                Text(text = "Simpan pencapaian, sertifikat, dan award penting", fontSize = 14.sp, color = Color.Gray)
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(tabs.size) { index ->
                        val tab = tabs[index]
                        FilterChip(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            label = { Text(if (tab == "ALL") "Semua" else tab) }
                        )
                    }
                }
            }

            if (visibleAchievements.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Belum ada achievement", color = Color.Gray)
                    }
                }
            } else {
                items(visibleAchievements, key = { it.id }) { achievement ->
                    AchievementCard(
                        achievementTitle = achievement.title,
                        achievementIssuer = achievement.issuer,
                        achievementCategory = achievement.category,
                        achievementImportance = achievement.importance,
                        achievementExpiry = achievement.expiryDate,
                        onDelete = { viewModel.deleteAchievement(achievement.id) }
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
                issuer = ""
                category = "CERTIFICATION"
                description = ""
                credentialUrl = ""
                credentialId = ""
                filePath = ""
                importance = "2"
                expiryDate = ""
            },
            title = { Text("Tambah Achievement") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = issuer, onValueChange = { issuer = it }, label = { Text("Issuer") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = credentialId, onValueChange = { credentialId = it }, label = { Text("Credential ID") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = credentialUrl, onValueChange = { credentialUrl = it }, label = { Text("Credential URL") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = filePath, onValueChange = { filePath = it }, label = { Text("File Path") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = importance, onValueChange = { importance = it }, label = { Text("Importance 1-3") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = expiryDate, onValueChange = { expiryDate = it }, label = { Text("Expiry Date (epoch ms, optional)") }, modifier = Modifier.fillMaxWidth())
                    Text("Category", fontWeight = FontWeight.Medium)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf("CERTIFICATION", "AWARD", "COMPETITION", "BADGE").size) { index ->
                            val cat = listOf("CERTIFICATION", "AWARD", "COMPETITION", "BADGE")[index]
                            FilterChip(selected = category == cat, onClick = { category = cat }, label = { Text(cat) })
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (title.isNotBlank()) {
                        viewModel.addAchievement(
                            title = title,
                            issuer = issuer,
                            category = category,
                            description = description,
                            importance = importance.toIntOrNull() ?: 2,
                            credentialUrl = credentialUrl
                        )
                        showAddDialog = false
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
fun AchievementCard(
    achievementTitle: String,
    achievementIssuer: String,
    achievementCategory: String,
    achievementImportance: Int,
    achievementExpiry: Long?,
    onDelete: () -> Unit
) {
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
    val expiryText = achievementExpiry?.let { formatter.format(Date(it)) } ?: "Tidak ada expirasi"

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
                    Text(text = achievementTitle, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color(0xFF1A1A1A))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = achievementIssuer, fontSize = 12.sp, color = Color.Gray)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFD32F2F))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFE3F2FD)) {
                    Text(text = achievementCategory, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, color = Color(0xFF1E88E5))
                }
                Surface(shape = RoundedCornerShape(4.dp), color = if (achievementImportance == 3) Color(0xFFFFEBEE) else Color(0xFFF1F8E9)) {
                    Text(text = "Importance: $achievementImportance", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, color = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "Expiry: $expiryText", fontSize = 11.sp, color = Color.Gray)
        }
    }
}

