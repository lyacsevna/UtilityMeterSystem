package com.example.utilitymetersystem.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "utility_readings")
data class UtilityReading(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String,
    val value: Double,
    val note: String? = null,
    val date: Long = System.currentTimeMillis()
)