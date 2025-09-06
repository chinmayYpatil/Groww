package com.example.groww.di

import com.example.groww.data.local.database.dao.WatchlistDao
import com.example.groww.data.remote.StockApiService
import com.example.groww.data.repository.StockRepository
import com.example.groww.data.repository.WatchlistRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideStockRepository(apiService: StockApiService): StockRepository {
        return StockRepository(apiService)
    }

    @Provides
    @Singleton
    fun provideWatchlistRepository(watchlistDao: WatchlistDao): WatchlistRepository {
        return WatchlistRepository(watchlistDao)
    }
}