package com.example.groww.data.local.database.entities


import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(tableName = "watchlists")
data class WatchlistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "watchlist_stocks",
    foreignKeys = [
        ForeignKey(
            entity = WatchlistEntity::class,
            parentColumns = ["id"],
            childColumns = ["watchlistId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class WatchlistStockEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val watchlistId: Long,
    val symbol: String,
    val name: String,
    val addedAt: Long = System.currentTimeMillis()
)