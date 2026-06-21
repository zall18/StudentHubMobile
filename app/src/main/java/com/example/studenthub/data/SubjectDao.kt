package com.example.studenthub.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: Subject)

    @Update
    suspend fun updateSubject(subject: Subject)

    @Delete
    suspend fun deleteSubject(subject: Subject)

    @Query("SELECT * FROM subjects WHERE id = :subjectId")
    fun getSubjectById(subjectId: Int): Flow<Subject?>

    @Query("SELECT * FROM subjects WHERE semester = :semester ORDER BY code ASC")
    fun getSubjectsBySemester(semester: Int): Flow<List<Subject>>

    @Query("SELECT * FROM subjects ORDER BY semester ASC, code ASC")
    fun getAllSubjects(): Flow<List<Subject>>

    @Query("SELECT * FROM subjects WHERE name LIKE '%' || :searchTerm || '%' OR code LIKE '%' || :searchTerm || '%'")
    fun searchSubjects(searchTerm: String): Flow<List<Subject>>

    @Query("DELETE FROM subjects WHERE id = :subjectId")
    suspend fun deleteSubjectById(subjectId: Int)

    @Query("SELECT COUNT(*) FROM subjects WHERE semester = :semester")
    fun getSubjectCountBySemester(semester: Int): Flow<Int>
}
