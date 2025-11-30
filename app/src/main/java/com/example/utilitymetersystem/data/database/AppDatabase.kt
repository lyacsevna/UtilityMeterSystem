package com.example.utilitymetersystem.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.example.utilitymetersystem.data.models.UtilityReading
import com.example.utilitymetersystem.data.dao.UtilityReadingDao
import com.example.utilitymetersystem.data.converters.DateConverter

@Database(
    entities = [UtilityReading::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun utilityReadingDao(): UtilityReadingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "utility_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}