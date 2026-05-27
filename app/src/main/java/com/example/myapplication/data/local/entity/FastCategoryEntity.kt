package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fast_category_usage")
data class FastCategoryEntity(
    @PrimaryKey val name: String,
    val icon: String,
    val superCategory: String,
    val usageCount: Int = 1,
    val lastUsedTimestamp: Long = System.currentTimeMillis()
)
