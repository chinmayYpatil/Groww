package com.example.groww.data.local.database.dao

import androidx.room.*
import com.example.groww.data.local.database.entities.WatchlistEntity
import com.example.groww.data.local.database.entities.WatchlistStockEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {

    @Query("SELECT * FROM watchlists ORDER BY createdAt DESC")
    fun getAllWatchlists(): Flow<List<WatchlistEntity>>

    @Query("SELECT * FROM watchlist_stocks WHERE watchlistId = :watchlistId")
    fun getStocksInWatchlist(watchlistId: Long): Flow<List<WatchlistStockEntity>>

    @Query("SELECT * FROM watchlist_stocks WHERE symbol = :symbol")
    suspend fun getStockWatchlists(symbol: String): List<WatchlistStockEntity>

    @Insert
    suspend fun insertWatchlist(watchlist: WatchlistEntity): Long

    @Insert
    suspend fun insertStockToWatchlist(stock: WatchlistStockEntity)

    @Delete
    suspend fun deleteWatchlist(watchlist: WatchlistEntity)

    @Query("DELETE FROM watchlist_stocks WHERE watchlistId = :watchlistId AND symbol = :symbol")
    suspend fun removeStockFromWatchlist(watchlistId: Long, symbol: String)

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist_stocks WHERE symbol = :symbol)")
    suspend fun isStockInAnyWatchlist(symbol: String): Boolean
}