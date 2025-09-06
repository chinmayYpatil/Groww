package com.example.groww.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.example.groww.data.local.database.dao.WatchlistDao
import com.example.groww.data.local.database.entities.WatchlistEntity
import com.example.groww.data.local.database.entities.WatchlistStockEntity

@Database(
    entities = [WatchlistEntity::class, WatchlistStockEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun watchlistDao(): WatchlistDao
}