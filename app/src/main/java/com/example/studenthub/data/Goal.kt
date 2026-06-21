package com.example.studenthub.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "title")
    val title: String,
    // e.g., "Learn Jetpack Compose"

    @ColumnInfo(name = "description")
    val description: String = "",

    @ColumnInfo(name = "category")
    val category: String = "ACADEMIC",
    // ACADEMIC, PERSONAL, PROFESSIONAL

    @ColumnInfo(name = "target_date")
    val targetDate: Long = System.currentTimeMillis(),
    // Target completion date

    @ColumnInfo(name = "progress")
    val progress: Float = 0f,
    // 0-100 percentage

    @ColumnInfo(name = "status")
    val status: String = "NOT_STARTED",
    // NOT_STARTED, IN_PROGRESS, COMPLETED, ABANDONED

    @ColumnInfo(name = "priority")
    val priority: Int = 2,
    // 1: Low, 2: Medium, 3: High

    @ColumnInfo(name = "notes")
    val notes: String = "",

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
