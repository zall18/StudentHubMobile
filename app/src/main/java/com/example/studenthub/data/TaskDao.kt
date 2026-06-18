import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    // Fungsi untuk nambah data. Pake suspend karena ini proses async (gak boleh ngeblok UI)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    // Ambil semua tugas yang belum selesai, diurutkan dari deadline paling dekat.
    // Pakai Flow biar UI otomatis reaktif kalau ada data baru masuk.
    @Query("SELECT * FROM tasks_table WHERE is_completed = 0 ORDER BY deadline ASC")
    fun getActiveTasks(): Flow<List<Task>>

    // Fungsi untuk update status tugas kalau udah dikerjain
    @Query("UPDATE tasks_table SET is_completed = 1 WHERE id = :taskId")
    suspend fun markTaskAsCompleted(taskId: Int)
}