package com.example.utilitymetersystem.data.dao

import androidx.room.*
import com.example.utilitymetersystem.data.models.UtilityReading
import kotlinx.coroutines.flow.Flow

@Dao
interface UtilityReadingDao {

    @Insert
    suspend fun insert(reading: UtilityReading)

    @Query("SELECT * FROM utility_readings ORDER BY date DESC")
    fun getAllReadings(): Flow<List<UtilityReading>>

    @Query("SELECT * FROM utility_readings WHERE type = :type ORDER BY date DESC")
    suspend fun getReadingsByType(type: String): List<UtilityReading>
}