package com.example.utilitymetersystem.di

import com.example.utilitymetersystem.data.repository.ReadingRepository
import com.example.utilitymetersystem.data.repository.ReadingRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindReadingRepository(
        impl: ReadingRepositoryImpl
    ): ReadingRepository
}