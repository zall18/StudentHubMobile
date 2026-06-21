package com.example.studenthub.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryTaskDao {

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertCategoryTask(categoryTask: CategoryTask)

    @Query("SELECT * FROM category_task")
    fun getAllCategoryTasks(): List<CategoryTask>

    @Query("SELECT name_category FROM category_task")
    fun getAllCategoryNames(): Flow<List<String>>

    @Query("SELECT * FROM category_task WHERE name_category = :categoryName")
    fun getCategoryByName(categoryName: String): CategoryTask?

    @Query("DELETE FROM category_task WHERE name_category = :categoryName")
    fun deleteCategoryByName(categoryName: String)

    @Query("UPDATE category_task SET name_category = :newCategoryName WHERE name_category = :oldCategoryName")
    fun updateCategoryName(oldCategoryName: String, newCategoryName: String)
}