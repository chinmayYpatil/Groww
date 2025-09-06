package com.example.groww.data.repository

import com.example.groww.data.remote.StockApiService
import com.example.groww.data.model.network.CompanyOverviewResponse
import com.example.groww.data.model.network.TickerSearchResponse
import com.example.groww.data.model.network.TopGainersLosersResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockRepository @Inject constructor(
    private val apiService: StockApiService
) {
    suspend fun getTopGainersLosers(apiKey: String): TopGainersLosersResponse {
        return apiService.getTopGainersLosers(apiKey)
    }

    suspend fun getCompanyOverview(symbol: String, apiKey: String): CompanyOverviewResponse {
        return apiService.getCompanyOverview(symbol, apiKey)
    }

    suspend fun searchSymbol(keywords: String, apiKey: String): TickerSearchResponse {
        return apiService.searchSymbol(keywords, apiKey)
    }
}