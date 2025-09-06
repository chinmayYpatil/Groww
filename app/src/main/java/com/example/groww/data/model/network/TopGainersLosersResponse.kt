package com.example.groww.data.model.network


import com.google.gson.annotations.SerializedName

data class TopGainersLosersResponse(
    @SerializedName("metadata")
    val metadata: String,
    @SerializedName("last_updated")
    val lastUpdated: String,
    @SerializedName("top_gainers")
    val topGainers: List<StockInfo>,
    @SerializedName("top_losers")
    val topLosers: List<StockInfo>,
    @SerializedName("most_actively_traded")
    val mostActivelyTraded: List<StockInfo>
)

data class StockInfo(
    @SerializedName("ticker")
    val ticker: String,
    @SerializedName("price")
    val price: String,
    @SerializedName("change_amount")
    val changeAmount: String,
    @SerializedName("change_percentage")
    val changePercentage: String,
    @SerializedName("volume")
    val volume: String
)