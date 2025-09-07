package com.example.groww.ui.stockdetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.groww.data.repository.StockRepository
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
    private val stockRepository: StockRepository
) : ViewModel() {

    private val _uiState = MutableLiveData<StockDetailsState>()
    val uiState: LiveData<StockDetailsState> = _uiState

    private val _isStockInWatchlist = MutableLiveData<Boolean>()
    val isStockInWatchlist: LiveData<Boolean> = _isStockInWatchlist

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
            }
        }
    }

    private fun checkWatchlistStatus() {
        viewModelScope.launch {
            _isStockInWatchlist.value = checkStockWatchlistStatusUseCase.execute(symbol)
        }
    }
}