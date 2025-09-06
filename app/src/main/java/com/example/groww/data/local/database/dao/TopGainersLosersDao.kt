package com.example.groww.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.groww.data.local.database.entities.TopGainersLosersEntity

@Dao
interface TopGainersLosersDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopGainersLosers(data: TopGainersLosersEntity)

    @Query("SELECT * FROM top_gainers_losers ORDER BY timestamp DESC LIMIT 1")
    suspend fun getTopGainersLosers(): TopGainersLosersEntity?
}