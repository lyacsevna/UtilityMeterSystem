package com.example.utilitymetersystem.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "readings")
data class UtilityReading(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: UtilityType,
    val value: Double,
    val date: Date,
    val note: String = ""
)

enum class UtilityType {
    WATER, ELECTRICITY
}