package com.example.studenthub.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "title")
    val title: String,
    // e.g., "Android Developer Certification"

    @ColumnInfo(name = "issuer")
    val issuer: String = "",
    // e.g., "Google", "Coursera"

    @ColumnInfo(name = "date_earned")
    val dateEarned: Long = System.currentTimeMillis(),
    // Timestamp

    @ColumnInfo(name = "expiry_date")
    val expiryDate: Long? = null,
    // Nullable (null if no expiry)

    @ColumnInfo(name = "credential_id")
    val credentialId: String = "",
    // ID dari sertifikat

    @ColumnInfo(name = "credential_url")
    val credentialUrl: String = "",
    // Link to verify

    @ColumnInfo(name = "category")
    val category: String = "CERTIFICATION",
    // CERTIFICATION, AWARD, COMPETITION, BADGE

    @ColumnInfo(name = "description")
    val description: String = "",

    @ColumnInfo(name = "file_path")
    val filePath: String = "",
    // Path to certificate file

    @ColumnInfo(name = "importance")
    val importance: Int = 2,
    // 1: Low, 2: Medium, 3: High

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
