package com.example.utilitymetersystem.data.repository

import com.example.utilitymetersystem.data.dao.UtilityReadingDao
import com.example.utilitymetersystem.data.models.UtilityReading
import com.example.utilitymetersystem.data.models.UtilityType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UtilityRepository @Inject constructor(
    private val utilityReadingDao: UtilityReadingDao
) {
    fun getAllReadings(): Flow<List<UtilityReading>> {
        return utilityReadingDao.getAllReadings()
    }

    fun getReadingsByType(type: UtilityType): Flow<List<UtilityReading>> {
        return utilityReadingDao.getReadingsByType(type)
    }

    suspend fun insertReading(reading: UtilityReading) {
        utilityReadingDao.insertReading(reading)
    }

    suspend fun updateReading(reading: UtilityReading) {
        utilityReadingDao.updateReading(reading)
    }

    suspend fun deleteReading(reading: UtilityReading) {
        utilityReadingDao.deleteReading(reading)
    }

    suspend fun getReadingById(id: Long): UtilityReading? {
        return utilityReadingDao.getReadingById(id)
    }
}