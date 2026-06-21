package com.example.studenthub.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "code")
    val code: String,
    // e.g., "CS101", "MATH201"

    @ColumnInfo(name = "name")
    val name: String,
    // e.g., "Pemrograman Mobile"

    @ColumnInfo(name = "credits")
    val credits: Int,
    // SKS (Satuan Kredit Semester)

    @ColumnInfo(name = "semester")
    val semester: Int,
    // 1-8

    @ColumnInfo(name = "lecturer")
    val lecturer: String,
    // Dosen pengampu

    @ColumnInfo(name = "room_location")
    val roomLocation: String = "",
    // Lokasi kelas

    @ColumnInfo(name = "schedule_day")
    val scheduleDay: String = "",
    // Mon, Tue, Wed, etc

    @ColumnInfo(name = "schedule_time")
    val scheduleTime: String = "",
    // HH:mm format

    @ColumnInfo(name = "color")
    val color: String = "#0066FF",
    // Hex color untuk UI

    @ColumnInfo(name = "notes")
    val notes: String = "",
    // Personal notes

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
