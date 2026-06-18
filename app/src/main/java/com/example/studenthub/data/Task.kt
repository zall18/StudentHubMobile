import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks_table")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "title")
    val title: String,
    // Contoh: "Review Proposal Transparaksi" atau "Siapin Materi Mentoring Mobile"

    @ColumnInfo(name = "deadline")
    val deadline: Long,
    // Disimpan pakai tipe data Long (Timestamp) biar gampang di-sorting

    @ColumnInfo(name = "category")
    val category: String,
    // Contoh: "Kuliah", "GDGoC", "SAG", "Lomba"

    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false
)