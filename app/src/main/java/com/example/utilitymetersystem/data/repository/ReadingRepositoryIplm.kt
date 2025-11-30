package com.example.utilitymetersystem.data.repository

import com.example.utilitymetersystem.data.dao.UtilityReadingDao
import com.example.utilitymetersystem.data.models.UtilityReading
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ReadingRepositoryImpl @Inject constructor(
    private val utilityReadingDao: UtilityReadingDao
) : ReadingRepository {

    override suspend fun addReading(reading: UtilityReading) {
        utilityReadingDao.insert(reading)
    }

    override fun getAllReadings(): Flow<List<UtilityReading>> {
        return utilityReadingDao.getAllReadings()
    }

    override suspend fun getReadingsByType(type: String): List<UtilityReading> {
        return utilityReadingDao.getReadingsByType(type) // Уберите .name, так как type уже String
    }
}