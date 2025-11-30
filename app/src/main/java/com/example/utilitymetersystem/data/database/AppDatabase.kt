package com.example.utilitymetersystem.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.utilitymetersystem.data.models.UtilityReading
import com.example.utilitymetersystem.data.dao.UtilityReadingDao
import com.example.utilitymetersystem.data.converters.DateConverter

@Database(
    entities = [UtilityReading::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun utilityReadingDao(): UtilityReadingDao
}