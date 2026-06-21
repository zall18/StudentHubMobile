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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.studenthub.data.Goal
import com.example.studenthub.data.GoalMilestone
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GoalsScreen(viewModel: StudentHubViewModel) {
    val allGoals by viewModel.allGoals.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf("IN_PROGRESS") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showMilestoneDialog by remember { mutableStateOf(false) }
    var selectedGoalId by remember { mutableStateOf<Int?>(null) }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("ACADEMIC") }
    var targetDate by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(2) }

    var milestoneTitle by remember { mutableStateOf("") }
    var milestoneTargetDate by remember { mutableStateOf("") }

    val tabs = listOf("IN_PROGRESS", "COMPLETED", "ABANDONED", "ALL")
    val visibleGoals = when (selectedTab) {
        "ALL" -> allGoals
        else -> allGoals.filter { it.status == selectedTab }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF0066FF),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Goal")
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
                Text(text = "Goals Tracker", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                Text(text = "Lacak tujuan akademik, personal, dan profesional", fontSize = 14.sp, color = Color.Gray)
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(tabs.size) { index ->
                        val tab = tabs[index]
                        FilterChip(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            label = { Text(if (tab == "ALL") "Semua" else tab.replace("_", " ")) }
                        )
                    }
                }
            }

            if (visibleGoals.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Belum ada goals", color = Color.Gray)
                    }
                }
            } else {
                items(visibleGoals, key = { it.id }) { goal ->
                    val milestones by viewModel.goalMilestonesForGoal(goal.id).collectAsStateWithLifecycle(initialValue = emptyList<GoalMilestone>())
                    GoalCard(
                        goal = goal,
                        milestoneCount = milestones.size,
                        onAddMilestone = {
                            selectedGoalId = goal.id
                            showMilestoneDialog = true
                        }
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
                description = ""
                category = "ACADEMIC"
                targetDate = ""
                priority = 2
            },
            title = { Text("Tambah Goal") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = targetDate, onValueChange = { targetDate = it }, label = { Text("Target Date (epoch ms, optional)") }, modifier = Modifier.fillMaxWidth())
                    Text("Category", fontWeight = FontWeight.Medium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("ACADEMIC", "PERSONAL", "PROFESSIONAL").forEach { item ->
                            FilterChip(selected = category == item, onClick = { category = item }, label = { Text(item) })
                        }
                    }
                    Text("Priority", fontWeight = FontWeight.Medium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(1 to "Low", 2 to "Medium", 3 to "High").forEach { (value, label) ->
                            FilterChip(selected = priority == value, onClick = { priority = value }, label = { Text(label) })
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (title.isNotBlank()) {
                        viewModel.addGoal(
                            title = title,
                            description = description,
                            category = category,
                            targetDate = targetDate.toLongOrNull() ?: System.currentTimeMillis() + (30L * 24L * 60L * 60L * 1000L),
                            priority = priority
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

    if (showMilestoneDialog && selectedGoalId != null) {
        AlertDialog(
            onDismissRequest = {
                showMilestoneDialog = false
                milestoneTitle = ""
                milestoneTargetDate = ""
                selectedGoalId = null
            },
            title = { Text("Tambah Milestone") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = milestoneTitle, onValueChange = { milestoneTitle = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = milestoneTargetDate, onValueChange = { milestoneTargetDate = it }, label = { Text("Target Date (epoch ms, optional)") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(onClick = {
                    val goalId = selectedGoalId
                    if (goalId != null && milestoneTitle.isNotBlank()) {
                        viewModel.addGoalMilestone(
                            goalId = goalId,
                            title = milestoneTitle,
                            targetDate = milestoneTargetDate.toLongOrNull() ?: System.currentTimeMillis()
                        )
                        showMilestoneDialog = false
                        milestoneTitle = ""
                        milestoneTargetDate = ""
                        selectedGoalId = null
                    }
                }) { Text("Simpan") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showMilestoneDialog = false
                    milestoneTitle = ""
                    milestoneTargetDate = ""
                    selectedGoalId = null
                }) { Text("Batal") }
            }
        )
    }
}

@Composable
fun GoalCard(
    goal: Goal,
    milestoneCount: Int,
    onAddMilestone: () -> Unit
) {
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
    val targetText = formatter.format(Date(goal.targetDate))

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
                    Text(text = goal.title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color(0xFF1A1A1A))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = goal.description, fontSize = 12.sp, color = Color.Gray, maxLines = 2)
                }
                Surface(shape = RoundedCornerShape(4.dp), color = when (goal.category) {
                    "ACADEMIC" -> Color(0xFFE3F2FD)
                    "PERSONAL" -> Color(0xFFF3E5F5)
                    else -> Color(0xFFFFF3E0)
                }) {
                    Text(text = goal.category, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, color = Color(0xFF1E88E5))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { (goal.progress.coerceIn(0f, 100f)) / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = Color(0xFF4CAF50),
                trackColor = Color(0xFFE0E0E0)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Target: $targetText", fontSize = 11.sp, color = Color.Gray)
                Text(text = "${goal.progress.toInt()}%", fontSize = 11.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Milestones: $milestoneCount", fontSize = 11.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = true, onClick = onAddMilestone, label = { Text("Tambah Milestone") })
            }
        }
    }
}




