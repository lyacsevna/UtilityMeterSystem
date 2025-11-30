package com.example.utilitymetersystem.data.repository

import com.example.utilitymetersystem.data.models.UtilityReading
import kotlinx.coroutines.flow.Flow

interface ReadingRepository {
    suspend fun addReading(reading: UtilityReading)
    fun getAllReadings(): Flow<List<UtilityReading>>
    suspend fun getReadingsByType(type: String): List<UtilityReading>
}