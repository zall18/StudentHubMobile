package com.example.studenthub.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("SELECT * FROM tasks_table WHERE is_completed = 0 ORDER BY deadline ASC")
    fun getActiveTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks_table WHERE is_completed = 0 ORDER BY priority DESC, deadline ASC")
    fun getActiveTasksByPriority(): Flow<List<Task>>

    @Query("SELECT * FROM tasks_table WHERE is_completed = 0 ORDER BY deadline ASC")
    fun getActiveTasksByDeadline(): Flow<List<Task>>

    @Query("SELECT * FROM tasks_table WHERE is_completed = 1 ORDER BY updated_at DESC")
    fun getCompletedTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks_table WHERE is_completed = 0 ORDER BY updated_at DESC")
    fun getUncompletedTasks(): Flow<List<Task>>


    @Query("SELECT * FROM tasks_table WHERE category = :category AND is_completed = 0 ORDER BY deadline ASC")
    fun getActiveTasksByCategory(category: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks_table WHERE deadline BETWEEN :startDate AND :endDate AND is_completed = 0 ORDER BY deadline ASC")
    fun getTasksByDateRange(startDate: Long, endDate: Long): Flow<List<Task>>

    @Query("SELECT * FROM tasks_table WHERE priority = :priority AND is_completed = 0 ORDER BY deadline ASC")
    fun getTasksByPriority(priority: Int): Flow<List<Task>>

    @Query("SELECT * FROM tasks_table WHERE title LIKE '%' || :searchTerm || '%' OR description LIKE '%' || :searchTerm || '%'")
    fun searchTasks(searchTerm: String): Flow<List<Task>>

    @Query("UPDATE tasks_table SET is_completed = 1, updated_at = :updatedTime WHERE id = :taskId")
    suspend fun markTaskAsCompleted(taskId: Int, updatedTime: Long = System.currentTimeMillis())

    @Query("UPDATE tasks_table SET is_completed = 0, updated_at = :updatedTime WHERE id = :taskId")
    suspend fun markTaskAsUncompleted(taskId: Int, updatedTime: Long = System.currentTimeMillis())


    @Query("DELETE FROM tasks_table WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: Int)

    @Query("SELECT COUNT(*) FROM tasks_table WHERE is_completed = 0")
    fun getActiveTaskCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM tasks_table WHERE priority = 3 AND is_completed = 0")
    fun getHighPriorityTaskCount(): Flow<Int>

    @Query("SELECT * FROM tasks_table WHERE is_completed = 0 ORDER BY deadline ASC LIMIT :limit")
    fun getUpcomingTasks(limit: Int = 5): Flow<List<Task>>
}
