package com.example.groww.data.repository

import com.example.groww.data.remote.StockApiService
import com.example.groww.data.model.network.CompanyOverviewResponse
import com.example.groww.data.model.network.StockInfo
import com.example.groww.data.model.network.TickerSearchResponse
import com.example.groww.data.model.network.TopGainersLosersResponse
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.days

@Singleton
class StockRepository @Inject constructor(
    private val apiService: StockApiService
) {
    // In-memory caches for each API endpoint with a 1-day expiration
    private var topGainersLosersCache: Pair<TopGainersLosersResponse, Long>? = null
    private var companyOverviewCache: MutableMap<String, Pair<CompanyOverviewResponse, Long>> = mutableMapOf()
    private var tickerSearchCache: MutableMap<String, Pair<TickerSearchResponse, Long>> = mutableMapOf()

    // Use mutex to ensure thread-safe cache access
    private val mutex = Mutex()

    private val cacheExpirationTimeMs = 1.days.inWholeMilliseconds

    private fun isCacheValid(timestamp: Long): Boolean {
        return (System.currentTimeMillis() - timestamp) < cacheExpirationTimeMs
    }

    suspend fun getTopGainersLosers(apiKey: String): TopGainersLosersResponse {
        return mutex.withLock {
            val cachedData = topGainersLosersCache
            if (cachedData != null && isCacheValid(cachedData.second)) {
                return cachedData.first
            }

            // Fetch from network if cache is invalid or non-existent
            val response = apiService.getTopGainersLosers(apiKey)
            topGainersLosersCache = Pair(response, System.currentTimeMillis())
            response
        }
    }

    // New function to get a single stock from the cached top gainers/losers list
    fun getStockInfoFromCache(symbol: String): StockInfo? {
        val cachedData = topGainersLosersCache?.first
        return cachedData?.topGainers?.firstOrNull { it.ticker == symbol }
            ?: cachedData?.topLosers?.firstOrNull { it.ticker == symbol }
    }

    suspend fun getCompanyOverview(symbol: String, apiKey: String): CompanyOverviewResponse? {
        return mutex.withLock {
            val cachedData = companyOverviewCache[symbol]
            if (cachedData != null && isCacheValid(cachedData.second)) {
                return cachedData.first
            }

            // Fetch from network
            val response = apiService.getCompanyOverview(symbol, apiKey)
            if (response.name.isNullOrEmpty()) {
                // The API returned an empty response, so don't cache it
                return null
            }
            companyOverviewCache[symbol] = Pair(response, System.currentTimeMillis())
            response
        }
    }

    suspend fun searchSymbol(keywords: String, apiKey: String): TickerSearchResponse {
        return mutex.withLock {
            val cachedData = tickerSearchCache[keywords]
            if (cachedData != null && isCacheValid(cachedData.second)) {
                return cachedData.first
            }

            // Fetch from network
            val response = apiService.searchSymbol(keywords, apiKey)
            tickerSearchCache[keywords] = Pair(response, System.currentTimeMillis())
            response
        }
    }
}