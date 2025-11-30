package com.example.utilitymetersystem.data.dao

import androidx.room.*
import com.example.utilitymetersystem.data.models.UtilityReading
import com.example.utilitymetersystem.data.models.UtilityType
import kotlinx.coroutines.flow.Flow

@Dao
interface UtilityReadingDao {
    @Query("SELECT * FROM readings ORDER BY date DESC")
    fun getAllReadings(): Flow<List<UtilityReading>>

    @Query("SELECT * FROM readings WHERE type = :type ORDER BY date DESC")
    fun getReadingsByType(type: UtilityType): Flow<List<UtilityReading>>

    @Insert
    suspend fun insertReading(reading: UtilityReading)

    @Update
    suspend fun updateReading(reading: UtilityReading)

    @Delete
    suspend fun deleteReading(reading: UtilityReading)

    @Query("SELECT * FROM readings WHERE id = :id")
    suspend fun getReadingById(id: Long): UtilityReading?
}