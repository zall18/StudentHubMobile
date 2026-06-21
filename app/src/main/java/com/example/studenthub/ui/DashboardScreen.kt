import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun DashboardScreen(viewModel: StudentHubViewModel) {
    // Membaca StateFlow dari ViewModel secara lifecycle-aware
    val taskList by viewModel.activeTasks.collectAsStateWithLifecycle()
    val categoryList by viewModel.categoryNames.collectAsStateWithLifecycle()


    var showDialog by remember { mutableStateOf(false) }
    var categoryName by remember { mutableStateOf("") }
    var categoryColor by remember { mutableStateOf("") }
    var categoryIcon by remember { mutableStateOf("") }
    var categoryDetail by remember { mutableStateOf("") }


        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Halo, Semangat Hari Ini!", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                Text(text = "Aplikasi Student Hub Lokal", fontSize = 14.sp, color = Color.Gray)
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0066FF))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Tugas Aktif", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${taskList.size} Agenda Menanti",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            item {
                Text(text = "Daftar Agenda & Tugas", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
            }

            // Tampilkan data asli dari database Room menggunakan items()
            items(taskList, key = { it.id }) { task ->
                TaskItemRow(
                    title = task.title,
                    category = task.category,
                    onItemClick = { viewModel.completeTask(task.id) } // Klik card buat selesaikan tugas
                )
            }

            items(categoryList, key = { it}) { categoryName ->
                Text(text = categoryName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
            }
        }


    // Dialog Pop-up buat nambah category task
    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
                categoryName = ""
                categoryColor = ""
                categoryIcon = ""
                categoryDetail = ""
            },
            title = { Text("Tambah Category Task") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = categoryName,
                        onValueChange = { categoryName = it },
                        label = { Text("Name Category") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = categoryColor,
                        onValueChange = { categoryColor = it },
                        label = { Text("Color Category") },
                        placeholder = { Text("#0066FF") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = categoryIcon,
                        onValueChange = { categoryIcon = it },
                        label = { Text("Icon Category") },
                        placeholder = { Text("ic_category_name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = categoryDetail,
                        onValueChange = { categoryDetail = it },
                        label = { Text("Detail Category") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (
                            categoryName.isNotBlank() &&
                            categoryColor.isNotBlank() &&
                            categoryIcon.isNotBlank() &&
                            categoryDetail.isNotBlank()
                        ) {
                            viewModel.addCategoryTask(
                                nameCategory = categoryName,
                                colorCategory = categoryColor,
                                iconCategory = categoryIcon,
                                detailCategory = categoryDetail
                            )
                            categoryName = ""
                            categoryColor = ""
                            categoryIcon = ""
                            categoryDetail = ""
                            showDialog = false
                        }
                    }
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                    categoryName = ""
                    categoryColor = ""
                    categoryIcon = ""
                    categoryDetail = ""
                }) { Text("Batal") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskItemRow(title: String, category: String, onItemClick: () -> Unit) {
    Card(
        onClick = onItemClick, // Klik buat hapus/selesai
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color(0xFF1A1A1A))
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = when(category) {
                        "Kuliah" -> Color(0xFFE3F2FD)
                        "GDGoC" -> Color(0xFFFFF3E0)
                        else -> Color(0xE8EAF6F5)
                    }
                ) {
                    Text(
                        text = category,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        color = when(category) {
                            "Kuliah" -> Color(0xFF1E88E5)
                            "GDGoC" -> Color(0xFFF57C00)
                            else -> Color(0xFF00796B)
                        },
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}