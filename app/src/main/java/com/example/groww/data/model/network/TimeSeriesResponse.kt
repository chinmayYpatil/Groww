package com.example.groww.data.model.network

import com.google.gson.annotations.SerializedName

data class TimeSeriesResponse(
    @SerializedName("Meta Data")
    val metaData: MetaData,
    @SerializedName("Time Series (Daily)")
    val timeSeriesDaily: Map<String, TimeSeriesDailyData>
)

data class MetaData(
    @SerializedName("1. Information")
    val information: String,
    @SerializedName("2. Symbol")
    val symbol: String,
    @SerializedName("3. Last Refreshed")
    val lastRefreshed: String,
    @SerializedName("4. Output Size")
    val outputSize: String,
    @SerializedName("5. Time Zone")
    val timeZone: String
)

data class TimeSeriesDailyData(
    @SerializedName("1. open")
    val open: String,
    @SerializedName("2. high")
    val high: String,
    @SerializedName("3. low")
    val low: String,
    @SerializedName("4. close")
    val close: String,
    @SerializedName("5. volume")
    val volume: String
)