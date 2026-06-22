package com.example.studenthub

import StudentHubViewModel
import StudentHubViewModelFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.studenthub.ui.MainScreen
import com.example.studenthub.ui.theme.StudentHubTheme
import com.example.studenthub.ui.theme.ThemeManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Theme Manager
        ThemeManager.initialize(this)

        enableEdgeToEdge()
        setContent {
            // Observe theme state
            val isDarkMode by ThemeManager.isDarkMode.collectAsState()
            val useDynamicColors by ThemeManager.useDynamicColors.collectAsState()

            StudentHubTheme(
                darkTheme = isDarkMode,
                dynamicColor = useDynamicColors
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val database = StudentHubDatabase.getDatabase(applicationContext)

                    val taskDao = database.taskDao()
                    val categoryTaskDao = database.categoryTaskDao()
                    val subjectDao = database.subjectDao()
                    val clubDao = database.clubDao()
                    val learningResourceDao = database.learningResourceDao()
                    val achievementDao = database.achievementDao()
                    val goalDao = database.goalDao()
                    val goalMilestoneDao = database.goalMilestoneDao()

                    val viewModel: StudentHubViewModel = viewModel(
                        factory = StudentHubViewModelFactory(
                            taskDao,
                            categoryTaskDao,
                            subjectDao,
                            clubDao,
                            learningResourceDao,
                            achievementDao,
                            goalDao,
                            goalMilestoneDao
                        )
                    )

                    MainScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    StudentHubTheme {
        Greeting("Android")
    }
}