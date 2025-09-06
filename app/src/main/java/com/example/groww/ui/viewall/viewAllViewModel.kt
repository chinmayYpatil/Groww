package com.example.groww.ui.viewall

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.groww.data.model.network.StockInfo
import com.example.groww.domain.usecase.GetTopGainersUseCase
import com.example.groww.domain.usecase.GetTopLosersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewAllViewModel @Inject constructor(
    private val getTopGainersUseCase: GetTopGainersUseCase,
    private val getTopLosersUseCase: GetTopLosersUseCase
) : ViewModel() {

    private val _stocks = MutableLiveData<List<StockInfo>>()
    val stocks: LiveData<List<StockInfo>> = _stocks

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadStocks(type: String, apiKey: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val result = when (type) {
                    "gainers" -> getTopGainersUseCase.execute(apiKey)
                    "losers" -> getTopLosersUseCase.execute(apiKey)
                    "active" -> {
                        // For most active, we can use the same endpoint but show all
                        // In a real app, you might have a separate use case for this
                        getTopGainersUseCase.execute(apiKey)
                    }
                    else -> emptyList()
                }

                _stocks.value = result

            } catch (e: Exception) {
                _error.value = when {
                    e.message?.contains("network", ignoreCase = true) == true ->
                        "Network error. Please check your connection."
                    e.message?.contains("api", ignoreCase = true) == true ->
                        "API limit reached. Please try again later."
                    else -> "Failed to load stocks. Please try again."
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}