package com.example.groww.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.groww.data.local.database.converters.TopGainersLosersConverter
import com.example.groww.data.local.database.dao.WatchlistDao
import com.example.groww.data.local.database.dao.TopGainersLosersDao
import com.example.groww.data.local.database.entities.TopGainersLosersEntity
import com.example.groww.data.local.database.entities.WatchlistEntity
import com.example.groww.data.local.database.entities.WatchlistStockEntity

@Database(entities = [WatchlistEntity::class, WatchlistStockEntity::class, TopGainersLosersEntity::class], version = 1, exportSchema = false)
@TypeConverters(TopGainersLosersConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun watchlistDao(): WatchlistDao
    abstract fun topGainersLosersDao(): TopGainersLosersDao
}