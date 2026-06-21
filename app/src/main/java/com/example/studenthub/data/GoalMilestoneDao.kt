package com.example.studenthub.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalMilestoneDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestone(milestone: GoalMilestone)

    @Update
    suspend fun updateMilestone(milestone: GoalMilestone)

    @Delete
    suspend fun deleteMilestone(milestone: GoalMilestone)

    @Query("SELECT * FROM goal_milestones WHERE id = :milestoneId")
    fun getMilestoneById(milestoneId: Int): Flow<GoalMilestone?>

    @Query("SELECT * FROM goal_milestones WHERE goal_id = :goalId ORDER BY target_date ASC")
    fun getMilestonesByGoal(goalId: Int): Flow<List<GoalMilestone>>

    @Query("SELECT * FROM goal_milestones WHERE goal_id = :goalId AND completed = 0 ORDER BY target_date ASC")
    fun getPendingMilestonesByGoal(goalId: Int): Flow<List<GoalMilestone>>

    @Query("SELECT * FROM goal_milestones WHERE goal_id = :goalId AND completed = 1 ORDER BY completed_at DESC")
    fun getCompletedMilestonesByGoal(goalId: Int): Flow<List<GoalMilestone>>

    @Query("UPDATE goal_milestones SET completed = 1, completed_at = :completedTime WHERE id = :milestoneId")
    suspend fun completeMilestone(milestoneId: Int, completedTime: Long = System.currentTimeMillis())

    @Query("UPDATE goal_milestones SET progress = :progress WHERE id = :milestoneId")
    suspend fun updateMilestoneProgress(milestoneId: Int, progress: Float)

    @Query("DELETE FROM goal_milestones WHERE id = :milestoneId")
    suspend fun deleteMilestoneById(milestoneId: Int)

    @Query("DELETE FROM goal_milestones WHERE goal_id = :goalId")
    suspend fun deleteMilestonesByGoalId(goalId: Int)

    @Query("SELECT COUNT(*) FROM goal_milestones WHERE goal_id = :goalId AND completed = 1")
    fun getCompletedMilestoneCount(goalId: Int): Flow<Int>

    @Query("SELECT COUNT(*) FROM goal_milestones WHERE goal_id = :goalId")
    fun getTotalMilestoneCount(goalId: Int): Flow<Int>
}
