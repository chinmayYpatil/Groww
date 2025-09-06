package com.example.groww.data.repository

import com.example.groww.data.local.database.dao.WatchlistDao
import com.example.groww.data.local.database.entities.WatchlistEntity
import com.example.groww.data.local.database.entities.WatchlistStockEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchlistRepository @Inject constructor(
    private val watchlistDao: WatchlistDao
) {

    fun getAllWatchlists(): Flow<List<WatchlistEntity>> {
        return watchlistDao.getAllWatchlists()
    }

    fun getStocksInWatchlist(watchlistId: Long): Flow<List<WatchlistStockEntity>> {
        return watchlistDao.getStocksInWatchlist(watchlistId)
    }

    suspend fun createWatchlist(name: String): Long {
        val watchlist = WatchlistEntity(name = name)
        return watchlistDao.insertWatchlist(watchlist)
    }

    suspend fun addStockToWatchlist(watchlistId: Long, symbol: String, name: String) {
        val stock = WatchlistStockEntity(
            watchlistId = watchlistId,
            symbol = symbol,
            name = name
        )
        watchlistDao.insertStockToWatchlist(stock)
    }

    suspend fun removeStockFromWatchlist(watchlistId: Long, symbol: String) {
        watchlistDao.removeStockFromWatchlist(watchlistId, symbol)
    }

    suspend fun deleteWatchlist(watchlist: WatchlistEntity) {
        watchlistDao.deleteWatchlist(watchlist)
    }

    suspend fun isStockInAnyWatchlist(symbol: String): Boolean {
        return watchlistDao.isStockInAnyWatchlist(symbol)
    }

    suspend fun getStockWatchlists(symbol: String): List<WatchlistStockEntity> {
        return watchlistDao.getStockWatchlists(symbol)
    }
}