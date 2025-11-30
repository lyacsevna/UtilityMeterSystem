package com.example.utilitymetersystem.di

import android.content.Context
import androidx.room.Room
import com.example.utilitymetersystem.data.database.AppDatabase
import com.example.utilitymetersystem.data.dao.UtilityReadingDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "utility_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideReadingDao(appDatabase: AppDatabase): UtilityReadingDao {
        return appDatabase.utilityReadingDao()
    }
}