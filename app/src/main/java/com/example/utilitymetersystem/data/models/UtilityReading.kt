package com.example.utilitymetersystem.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.util.Date
import java.util.UUID

@Entity(tableName = "utility_readings")

data class UtilityReading(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val type: UtilityType,
    val value: Double,
    val date: Date = Date(),
    val note: String = "",
    val imageUri: String? = null
) {
    fun isValidForType(): Boolean {
        return when (type) {
            UtilityType.WATER -> value >= 0
            UtilityType.ELECTRICITY -> value >= 0
        }
    }
}


