package com.example.studenthub.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "goal_milestones",
    foreignKeys = [
        ForeignKey(
            entity = Goal::class,
            parentColumns = ["id"],
            childColumns = ["goal_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class GoalMilestone(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "goal_id")
    val goalId: Int,
    // FK to Goal

    @ColumnInfo(name = "title")
    val title: String,
    // Milestone checkpoint

    @ColumnInfo(name = "target_date")
    val targetDate: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "completed")
    val completed: Boolean = false,

    @ColumnInfo(name = "completed_at")
    val completedAt: Long? = null,
    // Nullable, set when milestone is completed

    @ColumnInfo(name = "progress")
    val progress: Float = 0f
    // 0-100
)
