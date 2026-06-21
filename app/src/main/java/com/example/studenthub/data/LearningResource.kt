package com.example.studenthub.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "learning_resources",
    foreignKeys = [
        ForeignKey(
            entity = Subject::class,
            parentColumns = ["id"],
            childColumns = ["subject_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class LearningResource(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "title")
    val title: String,
    // e.g., "Kotlin Coroutines Guide"

    @ColumnInfo(name = "subject_id")
    val subjectId: Int? = null,
    // Link to subjects (nullable)

    @ColumnInfo(name = "category")
    val category: String = "WEBSITE",
    // VIDEO, PDF, ARTICLE, BOOK, GITHUB, WEBSITE

    @ColumnInfo(name = "url")
    val url: String = "",
    // Link to resource

    @ColumnInfo(name = "description")
    val description: String = "",

    @ColumnInfo(name = "difficulty")
    val difficulty: String = "INTERMEDIATE",
    // BEGINNER, INTERMEDIATE, ADVANCED

    @ColumnInfo(name = "rating")
    val rating: Float = 0f,
    // 1-5 stars (personal rating)

    @ColumnInfo(name = "notes")
    val notes: String = "",

    @ColumnInfo(name = "saved_at")
    val savedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false
)
