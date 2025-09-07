package com.example.groww.ui.watchlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.groww.data.local.database.entities.WatchlistStockEntity
import com.example.groww.data.model.local.Stock
import com.example.groww.data.repository.StockRepository
import com.example.groww.data.repository.WatchlistRepository
import com.example.groww.domain.usecase.RemoveStockFromWatchlistUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WatchlistDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val watchlistRepository: WatchlistRepository,
    private val stockRepository: StockRepository,
    private val removeStockFromWatchlistUseCase: RemoveStockFromWatchlistUseCase
) : ViewModel() {

    private val _stocks = MutableLiveData<List<WatchlistStockEntity>>()
    val stocks: LiveData<List<WatchlistStockEntity>> = _stocks

    private val _stockDetails = MutableLiveData<Map<String, Stock>>()
    val stockDetails: LiveData<Map<String, Stock>> = _stockDetails

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    val watchlistId: Long = savedStateHandle.get<String>("watchlistId")?.toLongOrNull() ?: 0L

    fun loadWatchlistStocks(watchlistId: Long, apiKey: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Get stocks in watchlist from database
                watchlistRepository.getStocksInWatchlist(watchlistId)
                    .catch { e ->
                        _error.value = "Failed to load watchlist stocks: ${e.message}"
                        _stocks.value = emptyList()
                        _isLoading.value = false
                    }
                    .collect { stockEntities ->
                        _stocks.value = stockEntities

                        // Fetch current stock details for each stock
                        if (stockEntities.isNotEmpty()) {
                            fetchStockDetails(stockEntities, apiKey)
                        } else {
                            _isLoading.value = false
                        }
                    }
            } catch (e: Exception) {
                _error.value = "Failed to load watchlist stocks: ${e.message}"
                _stocks.value = emptyList()
                _isLoading.value = false
            }
        }
    }

    private suspend fun fetchStockDetails(stockEntities: List<WatchlistStockEntity>, apiKey: String) {
        val stockDetailsMap = mutableMapOf<String, Stock>()

        try {
            stockEntities.forEach { stockEntity ->
                try {
                    // First try to get from cache (TopGainersLosers)
                    val cachedStockInfo = stockRepository.getStockInfoFromCache(stockEntity.symbol)

                    if (cachedStockInfo != null) {
                        stockDetailsMap[stockEntity.symbol] = Stock(
                            symbol = cachedStockInfo.ticker,
                            name = stockEntity.name,
                            price = cachedStockInfo.price,
                            changeAmount = cachedStockInfo.changeAmount,
                            changePercentage = cachedStockInfo.changePercentage,
                            volume = cachedStockInfo.volume,
                            isInWatchlist = true
                        )
                    } else {
                        // Fallback: try to get company overview
                        val companyOverview = stockRepository.getCompanyOverview(stockEntity.symbol, apiKey)
                        if (companyOverview != null) {
                            stockDetailsMap[stockEntity.symbol] = Stock(
                                symbol = stockEntity.symbol,
                                name = companyOverview.name ?: stockEntity.name,
                                price = "N/A", // Company overview doesn't have current price
                                changeAmount = "N/A",
                                changePercentage = "N/A",
                                marketCap = companyOverview.marketCapitalization ?: "",
                                sector = companyOverview.sector ?: "",
                                industry = companyOverview.industry ?: "",
                                isInWatchlist = true
                            )
                        } else {
                            // Last fallback: create basic stock info
                            stockDetailsMap[stockEntity.symbol] = Stock(
                                symbol = stockEntity.symbol,
                                name = stockEntity.name,
                                price = "N/A",
                                changeAmount = "N/A",
                                changePercentage = "N/A",
                                isInWatchlist = true
                            )
                        }
                    }
                } catch (e: Exception) {
                    // If individual stock fails, create basic info
                    stockDetailsMap[stockEntity.symbol] = Stock(
                        symbol = stockEntity.symbol,
                        name = stockEntity.name,
                        price = "N/A",
                        changeAmount = "N/A",
                        changePercentage = "N/A",
                        isInWatchlist = true
                    )
                }
            }

            _stockDetails.value = stockDetailsMap

        } catch (e: Exception) {
            // Even if stock details fail, we can still show the watchlist
            _error.value = "Failed to load current stock prices: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun removeStockFromWatchlist(watchlistId: Long, symbol: String) {
        viewModelScope.launch {
            try {
                removeStockFromWatchlistUseCase.execute(watchlistId, symbol)
                // The Flow collection will automatically update the UI
            } catch (e: Exception) {
                _error.value = "Failed to remove stock: ${e.message}"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}