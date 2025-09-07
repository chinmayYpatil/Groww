package com.example.groww.data.remote

import com.example.groww.data.model.network.CompanyOverviewResponse
import com.example.groww.data.model.network.TickerSearchResponse
import com.example.groww.data.model.network.TopGainersLosersResponse
import com.example.groww.data.model.network.TimeSeriesResponse
import retrofit2.http.GET
import retrofit2.http.Query


interface StockApiService {

    @GET("query?function=TOP_GAINERS_LOSERS")
    suspend fun getTopGainersLosers(@Query("apikey") apiKey: String): TopGainersLosersResponse

    @GET("query?function=OVERVIEW")
    suspend fun getCompanyOverview(@Query("symbol") symbol: String, @Query("apikey") apiKey: String): CompanyOverviewResponse

    @GET("query?function=SYMBOL_SEARCH")
    suspend fun searchSymbol(@Query("keywords") keywords: String, @Query("apikey") apiKey: String): TickerSearchResponse

    @GET("query?function=TIME_SERIES_DAILY")
    suspend fun getDailyTimeSeries(@Query("symbol") symbol: String, @Query("apikey") apiKey: String): TimeSeriesResponse
}