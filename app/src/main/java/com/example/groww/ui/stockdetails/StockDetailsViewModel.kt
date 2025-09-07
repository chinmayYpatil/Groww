package com.example.groww.ui.stockdetails

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.groww.data.repository.StockRepository
import com.example.groww.data.repository.WatchlistRepository
import com.example.groww.domain.usecase.AddStockToWatchlistUseCase
import com.example.groww.domain.usecase.CheckStockWatchlistStatusUseCase
import com.example.groww.domain.usecase.GetStockDetailsUseCase
import com.example.groww.domain.usecase.RemoveStockFromWatchlistUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StockDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getStockDetailsUseCase: GetStockDetailsUseCase,
    private val checkStockWatchlistStatusUseCase: CheckStockWatchlistStatusUseCase,
    private val addStockToWatchlistUseCase: AddStockToWatchlistUseCase,
    private val removeStockFromWatchlistUseCase: RemoveStockFromWatchlistUseCase,
    private val stockRepository: StockRepository,
    private val watchlistRepository: WatchlistRepository
) : ViewModel() {

    companion object {
        private const val TAG = "StockDetailsViewModel"
    }

    private val _uiState = MutableLiveData<StockDetailsState>()
    val uiState: LiveData<StockDetailsState> = _uiState

    private val _isStockInWatchlist = MutableLiveData<Boolean>()
    val isStockInWatchlist: LiveData<Boolean> = _isStockInWatchlist

    private val _actionMessage = MutableLiveData<String?>()
    val actionMessage: LiveData<String?> = _actionMessage

    val symbol: String = savedStateHandle.get<String>("symbol") ?: ""

    fun fetchStockDetails(apiKey: String) {
        viewModelScope.launch {
            _uiState.value = StockDetailsState.Loading
            try {
                val details = getStockDetailsUseCase.execute(symbol, apiKey)
                if (details != null) {
                    val timeSeriesData = stockRepository.getDailyTimeSeries(symbol, apiKey)
                    _uiState.value = StockDetailsState.FullDetails(details, timeSeriesData)
                } else {
                    val stockInfo = stockRepository.getStockInfoFromCache(symbol)
                    if (stockInfo != null) {
                        _uiState.value = StockDetailsState.PartialDetails(stockInfo)
                    } else {
                        _uiState.value = StockDetailsState.Empty
                    }
                }
                checkWatchlistStatus()
            } catch (e: Exception) {
                _uiState.value = StockDetailsState.Error("Failed to load stock details: ${e.message}")
                checkWatchlistStatus()
            }
        }
    }

    fun checkWatchlistStatus() {
        viewModelScope.launch {
            try {
                val isInWatchlist = checkStockWatchlistStatusUseCase.execute(symbol)
                _isStockInWatchlist.value = isInWatchlist
                Log.d(TAG, "Stock $symbol watchlist status: $isInWatchlist")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to check watchlist status", e)
                _isStockInWatchlist.value = false
            }
        }
    }

    /**
     * Remove stock from all watchlists when toggle is turned OFF
     */
    fun removeFromAllWatchlists() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Removing $symbol from all watchlists")

                // Get all watchlists containing this stock
                val stockWatchlists = watchlistRepository.getStockWatchlists(symbol)
                Log.d(TAG, "Found ${stockWatchlists.size} watchlists containing $symbol")

                // Remove from each watchlist
                stockWatchlists.forEach { stockEntity ->
                    removeStockFromWatchlistUseCase.execute(stockEntity.watchlistId, symbol)
                    Log.d(TAG, "Removed $symbol from watchlist ${stockEntity.watchlistId}")
                }

                // Update status
                _isStockInWatchlist.value = false
                _actionMessage.value = if (stockWatchlists.isNotEmpty()) {
                    "Removed from ${stockWatchlists.size} watchlist${if (stockWatchlists.size > 1) "s" else ""}"
                } else {
                    "Stock not found in any watchlist"
                }

            } catch (e: Exception) {
                Log.e(TAG, "Failed to remove from watchlists", e)
                _actionMessage.value = "Failed to remove from watchlist"
            }
        }
    }

    fun refreshWatchlistStatus() {
        checkWatchlistStatus()
    }

    fun clearActionMessage() {
        _actionMessage.value = null
    }
}