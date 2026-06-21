package com.example.studenthub.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clubs")
data class Club(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "name")
    val name: String,
    // e.g., "Google Developer Group"

    @ColumnInfo(name = "description")
    val description: String = "",

    @ColumnInfo(name = "category")
    val category: String = "",
    // e.g., "Tech", "Sports", "Arts"

    @ColumnInfo(name = "leader_name")
    val leaderName: String = "",

    @ColumnInfo(name = "contact_phone")
    val contactPhone: String = "",

    @ColumnInfo(name = "meeting_day")
    val meetingDay: String = "",
    // Mon, Tue, Wed, etc

    @ColumnInfo(name = "meeting_location")
    val meetingLocation: String = "",

    @ColumnInfo(name = "color")
    val color: String = "#FF6B6B",
    // Hex color untuk UI

    @ColumnInfo(name = "icon")
    val icon: String = "",
    // Resource name

    @ColumnInfo(name = "status")
    val status: String = "ACTIVE",
    // ACTIVE, INACTIVE

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
