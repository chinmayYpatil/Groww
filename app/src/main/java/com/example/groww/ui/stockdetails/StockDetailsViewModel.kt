package com.example.groww.ui.stockdetails

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.groww.data.model.network.TimeSeriesResponse
import com.example.groww.data.repository.StockRepository
import com.example.groww.data.repository.WatchlistRepository
import com.example.groww.domain.usecase.AddStockToWatchlistUseCase
import com.example.groww.domain.usecase.CheckStockWatchlistStatusUseCase
import com.example.groww.domain.usecase.GetStockDetailsUseCase
import com.example.groww.domain.usecase.RemoveStockFromWatchlistUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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

    private val _timeSeriesData = MutableLiveData<TimeSeriesResponse?>()
    val timeSeriesData: LiveData<TimeSeriesResponse?> = _timeSeriesData

    private val _isLoadingTimeSeries = MutableLiveData<Boolean>()
    val isLoadingTimeSeries: LiveData<Boolean> = _isLoadingTimeSeries

    val symbol: String = savedStateHandle.get<String>("symbol") ?: ""

    private var fetchDetailsJob: Job? = null
    private var fetchTimeSeriesJob: Job? = null
    private var watchlistJob: Job? = null

    fun fetchStockDetails(apiKey: String) {
        fetchDetailsJob?.cancel()
        fetchDetailsJob = viewModelScope.launch {
            setUiStateIfChanged(StockDetailsState.Loading)
            try {
                val details = getStockDetailsUseCase.execute(symbol, apiKey)
                if (details != null) {
                    setUiStateIfChanged(StockDetailsState.FullDetails(details, null))
                } else {
                    val stockInfo = stockRepository.getStockInfoFromCache(symbol)
                    if (stockInfo != null) {
                        setUiStateIfChanged(StockDetailsState.PartialDetails(stockInfo))
                    } else {
                        setUiStateIfChanged(StockDetailsState.Empty)
                    }
                }
                checkWatchlistStatus()
            } catch (e: Exception) {
                setUiStateIfChanged(StockDetailsState.Error("Failed to load stock details: ${e.message}"))
                checkWatchlistStatus()
            }
        }
    }

    fun fetchTimeSeriesData(timeFrame: String, apiKey: String) {
        fetchTimeSeriesJob?.cancel()
        fetchTimeSeriesJob = viewModelScope.launch {
            setLoadingTimeSeriesIfChanged(true)
            try {
                val response = when (timeFrame) {
                    "1D" -> stockRepository.getIntradayTimeSeries(symbol, apiKey)
                    "1W" -> stockRepository.getDailyTimeSeries(symbol, apiKey)
                    "1M" -> stockRepository.getMonthlyAdjustedTimeSeries(symbol, apiKey)
                    "3M" -> stockRepository.getDailyTimeSeries(symbol, apiKey)
                    "1Y" -> stockRepository.getWeeklyTimeSeries(symbol, apiKey)
                    else -> null
                }
                val timeSeriesResponse = when (response) {
                    is com.example.groww.data.model.network.TimeSeriesResponse -> response
                    is com.example.groww.data.model.network.TimeSeriesResponseAdjusted -> response.toTimeSeriesResponse()
                    else -> null
                }
                setTimeSeriesDataIfChanged(timeSeriesResponse)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch time series data for $timeFrame", e)
                setTimeSeriesDataIfChanged(null)
            } finally {
                setLoadingTimeSeriesIfChanged(false)
            }
        }
    }

    fun checkWatchlistStatus() {
        watchlistJob?.cancel()
        watchlistJob = viewModelScope.launch {
            try {
                val isInWatchlist = checkStockWatchlistStatusUseCase.execute(symbol)
                setWatchlistStatusIfChanged(isInWatchlist)
                Log.d(TAG, "Stock $symbol watchlist status: $isInWatchlist")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to check watchlist status", e)
                setWatchlistStatusIfChanged(false)
            }
        }
    }

    fun removeFromAllWatchlists() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Removing $symbol from all watchlists")
                val stockWatchlists = watchlistRepository.getStockWatchlists(symbol)
                Log.d(TAG, "Found ${stockWatchlists.size} watchlists containing $symbol")
                stockWatchlists.forEach { stockEntity ->
                    removeStockFromWatchlistUseCase.execute(stockEntity.watchlistId, symbol)
                    Log.d(TAG, "Removed $symbol from watchlist ${stockEntity.watchlistId}")
                }
                setWatchlistStatusIfChanged(false)
                setActionMessageIfChanged(
                    if (stockWatchlists.isNotEmpty()) {
                        "Removed from ${stockWatchlists.size} watchlist${if (stockWatchlists.size > 1) "s" else ""}"
                    } else {
                        "Stock not found in any watchlist"
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to remove from watchlists", e)
                setActionMessageIfChanged("Failed to remove from watchlist")
            }
        }
    }

    fun refreshWatchlistStatus() {
        checkWatchlistStatus()
    }

    fun clearActionMessage() {
        _actionMessage.value = null
    }

    private fun setUiStateIfChanged(newState: StockDetailsState) {
        if (_uiState.value != newState) {
            _uiState.value = newState
        }
    }

    private fun setWatchlistStatusIfChanged(isInWatchlist: Boolean) {
        if (_isStockInWatchlist.value != isInWatchlist) {
            _isStockInWatchlist.value = isInWatchlist
        }
    }

    private fun setActionMessageIfChanged(message: String?) {
        if (_actionMessage.value != message) {
            _actionMessage.value = message
        }
    }

    private fun setTimeSeriesDataIfChanged(data: TimeSeriesResponse?) {
        if (_timeSeriesData.value != data) {
            _timeSeriesData.value = data
        }
    }

    private fun setLoadingTimeSeriesIfChanged(isLoading: Boolean) {
        if (_isLoadingTimeSeries.value != isLoading) {
            _isLoadingTimeSeries.value = isLoading
        }
    }

    override fun onCleared() {
        super.onCleared()
        fetchDetailsJob?.cancel()
        fetchTimeSeriesJob?.cancel()
        watchlistJob?.cancel()
    }
}