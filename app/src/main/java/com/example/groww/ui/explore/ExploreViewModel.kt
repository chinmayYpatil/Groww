package com.example.groww.ui.explore

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
class ExploreViewModel @Inject constructor(
    private val getTopGainersUseCase: GetTopGainersUseCase,
    private val getTopLosersUseCase: GetTopLosersUseCase
) : ViewModel() {

    private val _topGainers = MutableLiveData<List<StockInfo>>()
    val topGainers: LiveData<List<StockInfo>> = _topGainers

    private val _topLosers = MutableLiveData<List<StockInfo>>()
    val topLosers: LiveData<List<StockInfo>> = _topLosers

    private val _mostActivelyTraded = MutableLiveData<List<StockInfo>>()
    val mostActivelyTraded: LiveData<List<StockInfo>> = _mostActivelyTraded

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun fetchTopStocks(apiKey: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val response = getTopGainersUseCase.getTopGainersLosersResponse(apiKey)

                _topGainers.value = response.topGainers
                _topLosers.value = response.topLosers
                _mostActivelyTraded.value = response.mostActivelyTraded

            } catch (e: Exception) {
                _error.value = when {
                    e.message?.contains("network", ignoreCase = true) == true ->
                        "Network error. Please check your connection."
                    e.message?.contains("api", ignoreCase = true) == true ->
                        "API limit reached. Please try again later."
                    else -> "Something went wrong. Please try again."
                }
                _topGainers.value = emptyList()
                _topLosers.value = emptyList()
                _mostActivelyTraded.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}