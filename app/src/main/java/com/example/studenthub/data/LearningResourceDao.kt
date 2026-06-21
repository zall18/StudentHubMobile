package com.example.studenthub.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LearningResourceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResource(resource: LearningResource)

    @Update
    suspend fun updateResource(resource: LearningResource)

    @Delete
    suspend fun deleteResource(resource: LearningResource)

    @Query("SELECT * FROM learning_resources WHERE id = :resourceId")
    fun getResourceById(resourceId: Int): Flow<LearningResource?>

    @Query("SELECT * FROM learning_resources ORDER BY saved_at DESC")
    fun getAllResources(): Flow<List<LearningResource>>

    @Query("SELECT * FROM learning_resources WHERE is_favorite = 1 ORDER BY saved_at DESC")
    fun getFavoriteResources(): Flow<List<LearningResource>>

    @Query("SELECT * FROM learning_resources WHERE category = :category ORDER BY saved_at DESC")
    fun getResourcesByCategory(category: String): Flow<List<LearningResource>>

    @Query("SELECT * FROM learning_resources WHERE difficulty = :difficulty ORDER BY saved_at DESC")
    fun getResourcesByDifficulty(difficulty: String): Flow<List<LearningResource>>

    @Query("SELECT * FROM learning_resources WHERE subject_id = :subjectId ORDER BY saved_at DESC")
    fun getResourcesBySubject(subjectId: Int): Flow<List<LearningResource>>

    @Query("SELECT * FROM learning_resources WHERE title LIKE '%' || :searchTerm || '%' OR description LIKE '%' || :searchTerm || '%'")
    fun searchResources(searchTerm: String): Flow<List<LearningResource>>

    @Query("UPDATE learning_resources SET is_favorite = :isFavorite WHERE id = :resourceId")
    suspend fun updateFavoriteStatus(resourceId: Int, isFavorite: Boolean)

    @Query("UPDATE learning_resources SET rating = :rating WHERE id = :resourceId")
    suspend fun updateRating(resourceId: Int, rating: Float)

    @Query("DELETE FROM learning_resources WHERE id = :resourceId")
    suspend fun deleteResourceById(resourceId: Int)

    @Query("SELECT COUNT(*) FROM learning_resources WHERE is_favorite = 1")
    fun getFavoriteResourceCount(): Flow<Int>
}
