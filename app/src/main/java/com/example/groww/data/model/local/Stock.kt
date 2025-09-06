package com.example.groww.data.model.local

data class Stock(
    val symbol: String,
    val name: String,
    val price: String,
    val changeAmount: String,
    val changePercentage: String,
    val volume: String = "",
    val marketCap: String = "",
    val sector: String = "",
    val industry: String = "",
    val isInWatchlist: Boolean = false
)