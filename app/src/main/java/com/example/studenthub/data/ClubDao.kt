package com.example.studenthub.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ClubDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClub(club: Club)

    @Update
    suspend fun updateClub(club: Club)

    @Delete
    suspend fun deleteClub(club: Club)

    @Query("SELECT * FROM clubs WHERE id = :clubId")
    fun getClubById(clubId: Int): Flow<Club?>

    @Query("SELECT * FROM clubs WHERE status = 'ACTIVE' ORDER BY name ASC")
    fun getAllActiveClubs(): Flow<List<Club>>

    @Query("SELECT * FROM clubs ORDER BY name ASC")
    fun getAllClubs(): Flow<List<Club>>

    @Query("SELECT * FROM clubs WHERE category = :category ORDER BY name ASC")
    fun getClubsByCategory(category: String): Flow<List<Club>>

    @Query("SELECT * FROM clubs WHERE name LIKE '%' || :searchTerm || '%'")
    fun searchClubs(searchTerm: String): Flow<List<Club>>

    @Query("DELETE FROM clubs WHERE id = :clubId")
    suspend fun deleteClubById(clubId: Int)

    @Query("SELECT COUNT(*) FROM clubs WHERE status = 'ACTIVE'")
    fun getActiveClubCount(): Flow<Int>
}
