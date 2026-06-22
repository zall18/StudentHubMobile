import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.studenthub.data.Achievement
import com.example.studenthub.data.AchievementDao
import com.example.studenthub.data.CategoryTask
import com.example.studenthub.data.CategoryTaskDao
import com.example.studenthub.data.Club
import com.example.studenthub.data.ClubDao
import com.example.studenthub.data.Goal
import com.example.studenthub.data.GoalDao
import com.example.studenthub.data.GoalMilestone
import com.example.studenthub.data.GoalMilestoneDao
import com.example.studenthub.data.LearningResource
import com.example.studenthub.data.LearningResourceDao
import com.example.studenthub.data.Subject
import com.example.studenthub.data.SubjectDao
import com.example.studenthub.data.Task
import com.example.studenthub.data.TaskDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StudentHubViewModel(
    private val taskDao: TaskDao,
    private val categoryTaskDao: CategoryTaskDao,
    private val subjectDao: SubjectDao,
    private val clubDao: ClubDao,
    private val learningResourceDao: LearningResourceDao,
    private val achievementDao: AchievementDao,
    private val goalDao: GoalDao,
    private val goalMilestoneDao: GoalMilestoneDao
) : ViewModel() {

    // ====== TASK STATE FLOWS ======
    val activeTasks: StateFlow<List<Task>> = taskDao.getActiveTasks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val activeTasksByPriority: StateFlow<List<Task>> = taskDao.getActiveTasksByPriority()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val completedTasks: StateFlow<List<Task>> = taskDao.getCompletedTasks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val upcomingTasks: StateFlow<List<Task>> = taskDao.getUpcomingTasks(5)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // ====== CATEGORY STATE FLOWS ======
    val categoryNames: StateFlow<List<String>> = categoryTaskDao.getAllCategoryNames()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // ====== SUBJECT STATE FLOWS ======
    val allSubjects: StateFlow<List<Subject>> = subjectDao.getAllSubjects()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // ====== CLUB STATE FLOWS ======
    val allClubs: StateFlow<List<Club>> = clubDao.getAllActiveClubs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // ====== LEARNING RESOURCES STATE FLOWS ======
    val allResources: StateFlow<List<LearningResource>> = learningResourceDao.getAllResources()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val favoriteResources: StateFlow<List<LearningResource>> = learningResourceDao.getFavoriteResources()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // ====== ACHIEVEMENT STATE FLOWS ======
    val allAchievements: StateFlow<List<Achievement>> = achievementDao.getAllAchievements()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // ====== GOALS STATE FLOWS ======
    val activeGoals: StateFlow<List<Goal>> = goalDao.getActiveGoals()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allGoals: StateFlow<List<Goal>> = goalDao.getAllGoals()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun goalMilestonesForGoal(goalId: Int) = goalMilestoneDao.getMilestonesByGoal(goalId)

    fun updateGoalStatus(goalId: Int, newStatus: String) {
        viewModelScope.launch {
            goalDao.updateGoalStatus(goalId, newStatus)
        }
    }
    // ====== TASK FUNCTIONS ======
    fun addTask(
        title: String,
        category: String,
        description: String = "",
        priority: Int = 2,
        deadline: Long = System.currentTimeMillis() + (24 * 60 * 60 * 1000)
    ) {
        viewModelScope.launch {
            val newTask = Task(
                title = title,
                category = category,
                description = description,
                priority = priority,
                deadline = deadline
            )
            taskDao.insertTask(newTask)
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            taskDao.updateTask(task.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    fun completeTask(taskId: Int) {
        viewModelScope.launch {
            taskDao.markTaskAsCompleted(taskId)
        }
    }

    fun uncompleteTask(taskId: Int) {
        viewModelScope.launch {
            taskDao.markTaskAsUncompleted(taskId)
        }
    }

    fun deleteTask(taskId: Int) {
        viewModelScope.launch {
            taskDao.deleteTaskById(taskId)
        }
    }

    // ====== CATEGORY FUNCTIONS ======
    fun addCategoryTask(
        nameCategory: String,
        colorCategory: String,
        iconCategory: String,
        detailCategory: String
    ) {
        viewModelScope.launch {
            val newCategoryTask = CategoryTask(
                name_category = nameCategory,
                color_category = colorCategory,
                icon_category = iconCategory,
                detail_category = detailCategory
            )
            categoryTaskDao.insertCategoryTask(newCategoryTask)
        }
    }

    // ====== SUBJECT FUNCTIONS ======
    fun addSubject(
        code: String,
        name: String,
        credits: Int,
        semester: Int,
        lecturer: String,
        roomLocation: String = "",
        scheduleDay: String = "",
        scheduleTime: String = "",
        color: String = "#0066FF"
    ) {
        viewModelScope.launch {
            val newSubject = Subject(
                code = code,
                name = name,
                credits = credits,
                semester = semester,
                lecturer = lecturer,
                roomLocation = roomLocation,
                scheduleDay = scheduleDay,
                scheduleTime = scheduleTime,
                color = color
            )
            subjectDao.insertSubject(newSubject)
        }
    }

    fun updateSubject(subject: Subject) {
        viewModelScope.launch {
            subjectDao.updateSubject(subject)
        }
    }

    fun deleteSubject(subjectId: Int) {
        viewModelScope.launch {
            subjectDao.deleteSubjectById(subjectId)
        }
    }

    // ====== CLUB FUNCTIONS ======
    fun addClub(
        name: String,
        description: String = "",
        category: String = "",
        leaderName: String = "",
        contactPhone: String = "",
        meetingDay: String = "",
        meetingLocation: String = "",
        color: String = "#FF6B6B"
    ) {
        viewModelScope.launch {
            val newClub = Club(
                name = name,
                description = description,
                category = category,
                leaderName = leaderName,
                contactPhone = contactPhone,
                meetingDay = meetingDay,
                meetingLocation = meetingLocation,
                color = color
            )
            clubDao.insertClub(newClub)
        }
    }

    fun updateClub(club: Club) {
        viewModelScope.launch {
            clubDao.updateClub(club)
        }
    }

    fun deleteClub(clubId: Int) {
        viewModelScope.launch {
            clubDao.deleteClubById(clubId)
        }
    }

    // ====== LEARNING RESOURCE FUNCTIONS ======
    fun addLearningResource(
        title: String,
        category: String = "WEBSITE",
        url: String = "",
        description: String = "",
        difficulty: String = "INTERMEDIATE",
        subjectId: Int? = null
    ) {
        viewModelScope.launch {
            val newResource = LearningResource(
                title = title,
                category = category,
                url = url,
                description = description,
                difficulty = difficulty,
                subjectId = subjectId
            )
            learningResourceDao.insertResource(newResource)
        }
    }

    fun updateLearningResource(resource: LearningResource) {
        viewModelScope.launch {
            learningResourceDao.updateResource(resource)
        }
    }

    fun toggleFavoriteResource(resourceId: Int, isFavorite: Boolean) {
        viewModelScope.launch {
            learningResourceDao.updateFavoriteStatus(resourceId, isFavorite)
        }
    }

    fun deleteLearningResource(resourceId: Int) {
        viewModelScope.launch {
            learningResourceDao.deleteResourceById(resourceId)
        }
    }

    // ====== ACHIEVEMENT FUNCTIONS ======
    fun addAchievement(
        title: String,
        issuer: String = "",
        dateEarned: Long = System.currentTimeMillis(),
        category: String = "CERTIFICATION",
        description: String = "",
        importance: Int = 2,
        credentialUrl: String = ""
    ) {
        viewModelScope.launch {
            val newAchievement = Achievement(
                title = title,
                issuer = issuer,
                dateEarned = dateEarned,
                category = category,
                description = description,
                importance = importance,
                credentialUrl = credentialUrl
            )
            achievementDao.insertAchievement(newAchievement)
        }
    }

    fun updateAchievement(achievement: Achievement) {
        viewModelScope.launch {
            achievementDao.updateAchievement(achievement)
        }
    }

    fun deleteAchievement(achievementId: Int) {
        viewModelScope.launch {
            achievementDao.deleteAchievementById(achievementId)
        }
    }

    // ====== GOAL FUNCTIONS ======
    fun addGoal(
        title: String,
        description: String = "",
        category: String = "ACADEMIC",
        targetDate: Long = System.currentTimeMillis() + (30 * 24 * 60 * 60 * 1000),
        priority: Int = 2
    ) {
        viewModelScope.launch {
            val newGoal = Goal(
                title = title,
                description = description,
                category = category,
                targetDate = targetDate,
                priority = priority
            )
            goalDao.insertGoal(newGoal)
        }
    }

    fun updateGoal(goal: Goal) {
        viewModelScope.launch {
            goalDao.updateGoal(goal)
        }
    }

    fun deleteGoal(goalId: Int) {
        viewModelScope.launch {
            goalDao.deleteGoalById(goalId)
        }
    }

    // ====== GOAL MILESTONE FUNCTIONS ======
    fun addGoalMilestone(
        goalId: Int,
        title: String,
        targetDate: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            val newMilestone = GoalMilestone(
                goalId = goalId,
                title = title,
                targetDate = targetDate
            )
            goalMilestoneDao.insertMilestone(newMilestone)
        }
    }

    fun completeMilestone(milestoneId: Int) {
        viewModelScope.launch {
            goalMilestoneDao.completeMilestone(milestoneId)
        }
    }

    fun updateMilestoneProgress(milestoneId: Int, progress: Float) {
        viewModelScope.launch {
            goalMilestoneDao.updateMilestoneProgress(milestoneId, progress)
        }
    }

    fun deleteGoalMilestone(milestoneId: Int) {
        viewModelScope.launch {
            goalMilestoneDao.deleteMilestoneById(milestoneId)
        }
    }
}

// ====== FACTORY ======
class StudentHubViewModelFactory(
    private val taskDao: TaskDao,
    private val categoryTaskDao: CategoryTaskDao,
    private val subjectDao: SubjectDao,
    private val clubDao: ClubDao,
    private val learningResourceDao: LearningResourceDao,
    private val achievementDao: AchievementDao,
    private val goalDao: GoalDao,
    private val goalMilestoneDao: GoalMilestoneDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StudentHubViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StudentHubViewModel(
                taskDao,
                categoryTaskDao,
                subjectDao,
                clubDao,
                learningResourceDao,
                achievementDao,
                goalDao,
                goalMilestoneDao
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

