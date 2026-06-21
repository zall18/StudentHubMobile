import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
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

@Database(
    entities = [
        Task::class,
        CategoryTask::class,
        Subject::class,
        Club::class,
        LearningResource::class,
        Achievement::class,
        Goal::class,
        GoalMilestone::class
    ],
    version = 3,
    exportSchema = false
)
abstract class StudentHubDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun categoryTaskDao(): CategoryTaskDao
    abstract fun subjectDao(): SubjectDao
    abstract fun clubDao(): ClubDao
    abstract fun learningResourceDao(): LearningResourceDao
    abstract fun achievementDao(): AchievementDao
    abstract fun goalDao(): GoalDao
    abstract fun goalMilestoneDao(): GoalMilestoneDao

    companion object {
        @Volatile
        private var INSTANCE: StudentHubDatabase? = null

        fun getDatabase(context: Context): StudentHubDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StudentHubDatabase::class.java,
                    "student_hub_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}