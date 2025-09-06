package com.example.groww.data.local.database.entities


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.groww.data.model.network.StockInfo
import com.example.groww.data.model.network.TopGainersLosersResponse

@Entity(tableName = "top_gainers_losers")
data class TopGainersLosersEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val metadata: String,
    val lastUpdated: String,
    val topGainers: List<StockInfo>,
    val topLosers: List<StockInfo>,
    val mostActivelyTraded: List<StockInfo>,
    val timestamp: Long
)