package com.example.studenthub.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks_table")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "deadline")
    val deadline: Long,

    @ColumnInfo(name = "category")
    val category: String,

    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false,

    @ColumnInfo(name = "description")
    val description: String = "",

    @ColumnInfo(name = "priority")
    val priority: Int = 2,
    // 1: Low, 2: Medium, 3: High

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "attachment_path")
    val attachmentPath: String = "",

    @ColumnInfo(name = "notes")
    val notes: String = ""
)