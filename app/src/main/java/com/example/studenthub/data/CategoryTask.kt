package com.example.studenthub.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("category_task")
data class CategoryTask(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo("name_category")
    val name_category: String,

    @ColumnInfo("color_category")
    val color_category: String,

    @ColumnInfo("icon_category")
    val icon_category: String,

    @ColumnInfo("detail_category")
    val detail_category: String
)
