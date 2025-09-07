package com.example.groww.data.repository

import android.util.Log
import com.example.groww.data.remote.StockApiService
import com.example.groww.data.model.network.CompanyOverviewResponse
import com.example.groww.data.model.network.StockInfo
import com.example.groww.data.model.network.TickerSearchResponse
import com.example.groww.data.model.network.TopGainersLosersResponse
import com.example.groww.data.model.network.TimeSeriesResponse
import com.example.groww.data.model.network.NewsSentimentResponse
import com.example.groww.data.local.database.dao.TopGainersLosersDao
import com.example.groww.data.local.database.entities.TopGainersLosersEntity
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException
import java.net.UnknownHostException
import java.net.SocketTimeoutException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

@Singleton
class StockRepository @Inject constructor(
    private val apiService: StockApiService,
    private val topGainersLosersDao: TopGainersLosersDao
) {
    companion object {
        private const val TAG = "StockRepository"
    }

    // In-memory caches for each API endpoint with a 1-day expiration
    private var topGainersLosersCache: Pair<TopGainersLosersResponse, Long>? = null
    private var companyOverviewCache: MutableMap<String, Pair<CompanyOverviewResponse, Long>> = mutableMapOf()
    private var tickerSearchCache: MutableMap<String, Pair<TickerSearchResponse, Long>> = mutableMapOf()
    private var timeSeriesCache: MutableMap<String, Pair<TimeSeriesResponse, Long>> = mutableMapOf()
    private var newsSentimentCache: Pair<NewsSentimentResponse, Long>? = null

    // Use mutex to ensure thread-safe cache access
    private val mutex = Mutex()

    private val cacheExpirationTimeMs = 1.days.inWholeMilliseconds
    private val newsCacheExpirationTimeMs = 15.minutes.inWholeMilliseconds

    private fun isCacheValid(timestamp: Long, expiration: Long): Boolean {
        return (System.currentTimeMillis() - timestamp) < expiration
    }

    suspend fun getTopGainersLosers(apiKey: String): TopGainersLosersResponse {
        return mutex.withLock {
            Log.d(TAG, "Attempting to fetch top gainers/losers data")

            val cachedData = topGainersLosersDao.getTopGainersLosers()
            if (cachedData != null && isCacheValid(cachedData.timestamp, cacheExpirationTimeMs)) {
                Log.d(TAG, "Using cached data from database")
                val response = TopGainersLosersResponse(
                    metadata = cachedData.metadata,
                    lastUpdated = cachedData.lastUpdated,
                    topGainers = cachedData.topGainers,
                    topLosers = cachedData.topLosers,
                    mostActivelyTraded = cachedData.mostActivelyTraded
                )
                // Populate the in-memory cache here
                topGainersLosersCache = Pair(response, cachedData.timestamp)
                return response
            }

            // Fetch from network if cache is invalid or non-existent
            Log.d(TAG, "Fetching fresh data from API")
            try {
                val response = apiService.getTopGainersLosers(apiKey)
                Log.d(TAG, "API call successful, caching response")

                val entity = TopGainersLosersEntity(
                    metadata = response.metadata,
                    lastUpdated = response.lastUpdated,
                    topGainers = response.topGainers,
                    topLosers = response.topLosers,
                    mostActivelyTraded = response.mostActivelyTraded,
                    timestamp = System.currentTimeMillis()
                )
                topGainersLosersDao.insertTopGainersLosers(entity)
                // Populate the in-memory cache with the new data
                topGainersLosersCache = Pair(response, System.currentTimeMillis())
                response
            } catch (e: Exception) {
                Log.e(TAG, "API call failed", e)
                handleNetworkError(e)
            }
        }
    }

    suspend fun getNewsSentiment(tickers: String, apiKey: String): NewsSentimentResponse {
        return mutex.withLock {
            Log.d(TAG, "Attempting to fetch news sentiment data for $tickers")

            val cachedData = newsSentimentCache
            if (cachedData != null && isCacheValid(cachedData.second, newsCacheExpirationTimeMs)) {
                Log.d(TAG, "Using cached news data")
                return cachedData.first
            }

            try {
                Log.d(TAG, "Fetching fresh news data from API")
                val response = apiService.getNewsSentiment(tickers, apiKey)
                Log.d(TAG, "News API call successful, caching response")
                newsSentimentCache = Pair(response, System.currentTimeMillis())
                response
            } catch (e: Exception) {
                Log.e(TAG, "News API call failed", e)
                handleNetworkError(e)
            }
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
            Log.d(TAG, "Fetching company overview for symbol: $symbol")

            val cachedData = companyOverviewCache[symbol]
            if (cachedData != null && isCacheValid(cachedData.second, cacheExpirationTimeMs)) {
                Log.d(TAG, "Using cached company overview for $symbol")
                return cachedData.first
            }

            // Fetch from network
            try {
                Log.d(TAG, "Fetching fresh company overview from API for $symbol")
                val response = apiService.getCompanyOverview(symbol, apiKey)
                if (response.name.isNullOrEmpty()) {
                    Log.w(TAG, "API returned empty response for symbol: $symbol")
                    // The API returned an empty response, so don't cache it
                    return null
                }
                Log.d(TAG, "Company overview API call successful for $symbol")
                companyOverviewCache[symbol] = Pair(response, System.currentTimeMillis())
                response
            } catch (e: Exception) {
                Log.e(TAG, "Company overview API call failed for $symbol", e)
                handleNetworkError(e)
            }
        }
    }

    suspend fun searchSymbol(keywords: String, apiKey: String): TickerSearchResponse {
        return mutex.withLock {
            Log.d(TAG, "Searching for symbol: $keywords")

            val cachedData = tickerSearchCache[keywords]
            if (cachedData != null && isCacheValid(cachedData.second, cacheExpirationTimeMs)) {
                Log.d(TAG, "Using cached search results for: $keywords")
                return cachedData.first
            }

            // Fetch from network
            try {
                Log.d(TAG, "Fetching fresh search results from API for: $keywords")
                val response = apiService.searchSymbol(keywords, apiKey)
                if (response.information != null) {
                    // Throw a specific exception for API limit reached
                    throw Exception("API limit reached.")
                }
                Log.d(TAG, "Search API call successful for: $keywords")
                tickerSearchCache[keywords] = Pair(response, System.currentTimeMillis())
                response
            } catch (e: Exception) {
                Log.e(TAG, "Search API call failed for: $keywords", e)
                handleNetworkError(e)
            }
        }
    }

    suspend fun getDailyTimeSeries(symbol: String, apiKey: String): TimeSeriesResponse? {
        return mutex.withLock {
            Log.d(TAG, "Fetching daily time series for symbol: $symbol")

            val cachedData = timeSeriesCache[symbol]
            if (cachedData != null && isCacheValid(cachedData.second, cacheExpirationTimeMs)) {
                Log.d(TAG, "Using cached time series data for $symbol")
                return cachedData.first
            }

            // Fetch from network
            try {
                Log.d(TAG, "Fetching fresh time series data from API for $symbol")
                val response = apiService.getDailyTimeSeries(symbol, apiKey)
                if (response.timeSeriesDaily.isNullOrEmpty()) {
                    Log.w(TAG, "API returned empty time series response for symbol: $symbol")
                    return null
                }
                Log.d(TAG, "Time series API call successful for $symbol")
                timeSeriesCache[symbol] = Pair(response, System.currentTimeMillis())
                response
            } catch (e: Exception) {
                Log.e(TAG, "Time series API call failed for $symbol", e)
                handleNetworkError(e)
            }
        }
    }

    private fun handleNetworkError(e: Exception): Nothing {
        val message = when (e) {
            is UnknownHostException -> {
                Log.e(TAG, "DNS resolution failed - check internet connection")
                "Unable to connect to server. Please check your internet connection."
            }
            is SocketTimeoutException -> {
                Log.e(TAG, "Request timed out")
                "Request timed out. Please try again."
            }
            is IOException -> {
                Log.e(TAG, "Network IO error: ${e.message}")
                "Network error occurred. Please check your connection."
            }
            else -> {
                Log.e(TAG, "Unexpected error: ${e.message}")
                "An unexpected error occurred: ${e.message}"
            }
        }
        throw Exception(message, e)
    }
}