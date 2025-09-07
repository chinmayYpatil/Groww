package com.example.groww.data.remote

import com.example.groww.data.model.network.*
import retrofit2.http.GET
import retrofit2.http.Query


interface StockApiService {

    @GET("query?function=TOP_GAINERS_LOSERS")
    suspend fun getTopGainersLosers(@Query("apikey") apiKey: String): TopGainersLosersResponse

    @GET("query?function=OVERVIEW")
    suspend fun getCompanyOverview(@Query("symbol") symbol: String, @Query("apikey") apiKey: String): CompanyOverviewResponse

    @GET("query?function=SYMBOL_SEARCH")
    suspend fun searchSymbol(@Query("keywords") keywords: String, @Query("apikey") apiKey: String): TickerSearchResponse

    @GET("query?function=TIME_SERIES_DAILY&outputsize=full")
    suspend fun getDailyTimeSeries(@Query("symbol") symbol: String, @Query("apikey") apiKey: String): TimeSeriesResponse

    @GET("query?function=TIME_SERIES_WEEKLY")
    suspend fun getWeeklyTimeSeries(@Query("symbol") symbol: String, @Query("apikey") apiKey: String): TimeSeriesResponse

    @GET("query?function=TIME_SERIES_MONTHLY")
    suspend fun getMonthlyTimeSeries(@Query("symbol") symbol: String, @Query("apikey") apiKey: String): TimeSeriesResponse

    @GET("query?function=NEWS_SENTIMENT")
    suspend fun getNewsSentiment(@Query("tickers") tickers: String, @Query("apikey") apiKey: String): NewsSentimentResponse

    @GET("query?function=TIME_SERIES_INTRADAY&interval=5min&outputsize=full")
    suspend fun getIntradayTimeSeries(@Query("symbol") symbol: String, @Query("apikey") apiKey: String): TimeSeriesResponse

    @GET("query?function=TIME_SERIES_MONTHLY_ADJUSTED")
    suspend fun getMonthlyAdjustedTimeSeries(@Query("symbol") symbol: String, @Query("apikey") apiKey: String): TimeSeriesResponseAdjusted
}