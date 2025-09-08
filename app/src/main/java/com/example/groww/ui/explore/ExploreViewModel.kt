package com.example.groww.ui.explore

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.groww.data.model.network.StockInfo
import com.example.groww.data.model.network.Article
import com.example.groww.domain.usecase.GetTopGainersUseCase
import com.example.groww.domain.usecase.GetTopLosersUseCase
import com.example.groww.domain.usecase.GetNewsSentimentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val getTopGainersUseCase: GetTopGainersUseCase,
    private val getTopLosersUseCase: GetTopLosersUseCase,
    private val getNewsSentimentUseCase: GetNewsSentimentUseCase
) : ViewModel() {

    private val _topGainers = MutableLiveData<List<StockInfo>>()
    val topGainers: LiveData<List<StockInfo>> = _topGainers

    private val _topLosers = MutableLiveData<List<StockInfo>>()
    val topLosers: LiveData<List<StockInfo>> = _topLosers

    private val _mostActivelyTraded = MutableLiveData<List<StockInfo>>()
    val mostActivelyTraded: LiveData<List<StockInfo>> = _mostActivelyTraded

    private val _newsFeed = MutableLiveData<List<Article>>()
    val newsFeed: LiveData<List<Article>> = _newsFeed

    // Separate loading states
    private val _isLoadingStocks = MutableLiveData<Boolean>()
    val isLoadingStocks: LiveData<Boolean> = _isLoadingStocks

    private val _isLoadingNews = MutableLiveData<Boolean>()
    val isLoadingNews: LiveData<Boolean> = _isLoadingNews

    // Keep the old isLoading for backward compatibility
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _newsError = MutableLiveData<String?>()
    val newsError: LiveData<String?> = _newsError

    fun fetchTopStocks(apiKey: String) {
        // Launch both operations concurrently
        fetchStockData(apiKey)
        fetchNewsData(apiKey)
    }

    private fun fetchStockData(apiKey: String) {
        viewModelScope.launch {
            try {
                _isLoadingStocks.value = true
                _isLoading.value = true // For backward compatibility
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
                _isLoadingStocks.value = false
                _isLoading.value = false // For backward compatibility
            }
        }
    }

    private fun fetchNewsData(apiKey: String) {
        viewModelScope.launch {
            try {
                _isLoadingNews.value = true
                _newsError.value = null

                // Fetch news data separately
                val newsResponse = getNewsSentimentUseCase.execute("AAPL", "demo")
                _newsFeed.value = newsResponse.feed

            } catch (e: Exception) {
                _newsError.value = when {
                    e.message?.contains("network", ignoreCase = true) == true ->
                        "Failed to load news. Check your connection."
                    e.message?.contains("api", ignoreCase = true) == true ->
                        "News API limit reached. Please try again later."
                    else -> "Failed to load news. Please try again."
                }
                _newsFeed.value = emptyList()
            } finally {
                _isLoadingNews.value = false
            }
        }
    }

    // Method to retry only stock data
    fun retryStockData(apiKey: String) {
        fetchStockData(apiKey)
    }

    // Method to retry only news data
    fun retryNewsData(apiKey: String) {
        fetchNewsData(apiKey)
    }

    fun clearError() {
        _error.value = null
    }

    fun clearNewsError() {
        _newsError.value = null
    }
}