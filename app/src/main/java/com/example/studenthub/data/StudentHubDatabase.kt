import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Anotasi @Database ngasih tau Room kalau ini adalah file database utama.
// Kalau nanti ada tabel baru (misal tabel 'Jadwal Kelas'), tinggal tambahin di dalam array entities.
@Database(entities = [Task::class], version = 1, exportSchema = false)
abstract class StudentHubDatabase : RoomDatabase() {

    // Daftarin DAO yang udah kita bikin sebelumnya
    abstract fun taskDao(): TaskDao

    // Companion object ini fungsinya buat bikin Singleton.
    // Intinya: "Kalau database udah ada, pake yang itu aja. Kalau belum, baru bikin baru."
    companion object {
        @Volatile
        private var INSTANCE: StudentHubDatabase? = null

        fun getDatabase(context: Context): StudentHubDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StudentHubDatabase::class.java,
                    "student_hub_database" // Ini nama file database fisik di dalam HP nanti
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}