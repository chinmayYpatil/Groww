package com.example.groww.di

import android.content.Context
import androidx.room.Room
import com.example.groww.data.local.database.AppDatabase
import com.example.groww.data.local.database.dao.WatchlistDao
import com.example.groww.data.local.database.dao.TopGainersLosersDao
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
            context,
            AppDatabase::class.java,
            "groww_database"
        ).build()
    }

    @Provides
    fun provideWatchlistDao(database: AppDatabase): WatchlistDao {
        return database.watchlistDao()
    }

    @Provides
    fun provideTopGainersLosersDao(database: AppDatabase): TopGainersLosersDao {
        return database.topGainersLosersDao()
    }
}