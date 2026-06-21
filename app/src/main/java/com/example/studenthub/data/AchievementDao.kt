package com.example.studenthub.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: Achievement)

    @Update
    suspend fun updateAchievement(achievement: Achievement)

    @Delete
    suspend fun deleteAchievement(achievement: Achievement)

    @Query("SELECT * FROM achievements WHERE id = :achievementId")
    fun getAchievementById(achievementId: Int): Flow<Achievement?>

    @Query("SELECT * FROM achievements ORDER BY date_earned DESC")
    fun getAllAchievements(): Flow<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE category = :category ORDER BY date_earned DESC")
    fun getAchievementsByCategory(category: String): Flow<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE importance = :importance ORDER BY date_earned DESC")
    fun getAchievementsByImportance(importance: Int): Flow<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE title LIKE '%' || :searchTerm || '%' OR issuer LIKE '%' || :searchTerm || '%'")
    fun searchAchievements(searchTerm: String): Flow<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE expiry_date IS NOT NULL AND expiry_date > :currentTime ORDER BY expiry_date ASC")
    fun getExpiringAchievements(currentTime: Long): Flow<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE expiry_date IS NOT NULL AND expiry_date < :currentTime")
    fun getExpiredAchievements(currentTime: Long): Flow<List<Achievement>>

    @Query("DELETE FROM achievements WHERE id = :achievementId")
    suspend fun deleteAchievementById(achievementId: Int)

    @Query("SELECT COUNT(*) FROM achievements")
    fun getAchievementCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM achievements WHERE category = :category")
    fun getCategoryCount(category: String): Flow<Int>
}
