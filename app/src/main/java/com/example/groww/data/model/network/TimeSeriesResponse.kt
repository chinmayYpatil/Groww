package com.example.groww.data.model.network

import com.google.gson.annotations.SerializedName

data class TimeSeriesResponse(
    @SerializedName("Meta Data")
    val metaData: MetaData,
    @SerializedName("Time Series (Daily)")
    val timeSeriesDaily: Map<String, TimeSeriesDailyData>?,
    @SerializedName("Weekly Time Series")
    val timeSeriesWeekly: Map<String, TimeSeriesDailyData>?,
    @SerializedName("Monthly Time Series")
    val timeSeriesMonthly: Map<String, TimeSeriesDailyData>?,
    @SerializedName("Time Series (5min)")
    val timeSeriesIntraday: Map<String, TimeSeriesDailyData>?
)

data class TimeSeriesResponseAdjusted(
    @SerializedName("Meta Data")
    val metaData: MetaData,
    @SerializedName("Monthly Adjusted Time Series")
    val timeSeriesMonthlyAdjusted: Map<String, TimeSeriesDailyDataAdjusted>?
) {
    fun toTimeSeriesResponse(): TimeSeriesResponse {
        val timeSeriesMonthly = timeSeriesMonthlyAdjusted?.mapValues {
            TimeSeriesDailyData(
                open = it.value.open,
                high = it.value.high,
                low = it.value.low,
                close = it.value.close,
                volume = it.value.volume
            )
        }
        return TimeSeriesResponse(
            metaData = this.metaData,
            timeSeriesDaily = null,
            timeSeriesWeekly = null,
            timeSeriesMonthly = timeSeriesMonthly,
            timeSeriesIntraday = null
        )
    }
}

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

data class TimeSeriesDailyDataAdjusted(
    @SerializedName("1. open")
    val open: String,
    @SerializedName("2. high")
    val high: String,
    @SerializedName("3. low")
    val low: String,
    @SerializedName("4. close")
    val close: String,
    @SerializedName("5. adjusted close")
    val adjustedClose: String,
    @SerializedName("6. volume")
    val volume: String,
    @SerializedName("7. dividend amount")
    val dividendAmount: String
)