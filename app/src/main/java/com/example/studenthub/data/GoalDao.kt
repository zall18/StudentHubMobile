package com.example.studenthub.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: Goal)

    @Update
    suspend fun updateGoal(goal: Goal)

    @Delete
    suspend fun deleteGoal(goal: Goal)

    @Query("SELECT * FROM goals WHERE id = :goalId")
    fun getGoalById(goalId: Int): Flow<Goal?>

    @Query("SELECT * FROM goals WHERE status = 'IN_PROGRESS' ORDER BY target_date ASC")
    fun getActiveGoals(): Flow<List<Goal>>

    @Query("SELECT * FROM goals WHERE status = :status ORDER BY target_date ASC")
    fun getGoalsByStatus(status: String): Flow<List<Goal>>

    @Query("SELECT * FROM goals WHERE category = :category ORDER BY target_date ASC")
    fun getGoalsByCategory(category: String): Flow<List<Goal>>

    @Query("SELECT * FROM goals ORDER BY priority DESC, target_date ASC")
    fun getAllGoals(): Flow<List<Goal>>

    @Query("SELECT * FROM goals WHERE title LIKE '%' || :searchTerm || '%' OR description LIKE '%' || :searchTerm || '%'")
    fun searchGoals(searchTerm: String): Flow<List<Goal>>

    @Query("DELETE FROM goals WHERE id = :goalId")
    suspend fun deleteGoalById(goalId: Int)

    @Query("SELECT COUNT(*) FROM goals WHERE status = 'IN_PROGRESS'")
    fun getActiveGoalCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM goals WHERE status = 'COMPLETED'")
    fun getCompletedGoalCount(): Flow<Int>
}
