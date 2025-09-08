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
import kotlinx.coroutines.Job
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

    // Separate loading states for better UX
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

    // Add job management to prevent overlapping requests
    private var fetchStocksJob: Job? = null
    private var fetchNewsJob: Job? = null

    fun fetchTopStocks(apiKey: String) {
        // Cancel any existing jobs to prevent overlapping requests
        fetchStocksJob?.cancel()
        fetchNewsJob?.cancel()

        // Launch both operations concurrently
        fetchStocksJob = fetchStockData(apiKey)
        fetchNewsJob = fetchNewsData(apiKey)
    }

    private fun fetchStockData(apiKey: String): Job {
        return viewModelScope.launch {
            try {
                // Only update loading state if it's actually changing
                setLoadingStocksIfChanged(true)
                setLoadingIfChanged(true) // For backward compatibility
                clearErrorIfChanged()

                val response = getTopGainersUseCase.getTopGainersLosersResponse(apiKey)

                // Only update values if they actually changed
                setTopGainersIfChanged(response.topGainers)
                setTopLosersIfChanged(response.topLosers)
                setMostActiveIfChanged(response.mostActivelyTraded)

            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("network", ignoreCase = true) == true ->
                        "Network error. Please check your connection."
                    e.message?.contains("api", ignoreCase = true) == true ->
                        "API limit reached. Please try again later."
                    else -> "Something went wrong. Please try again."
                }

                setErrorIfChanged(errorMessage)
                setTopGainersIfChanged(emptyList())
                setTopLosersIfChanged(emptyList())
                setMostActiveIfChanged(emptyList())
            } finally {
                setLoadingStocksIfChanged(false)
                setLoadingIfChanged(false) // For backward compatibility
            }
        }
    }

    private fun fetchNewsData(apiKey: String): Job {
        return viewModelScope.launch {
            try {
                setLoadingNewsIfChanged(true)
                clearNewsErrorIfChanged()

                // Fetch news data separately
                val newsResponse = getNewsSentimentUseCase.execute("AAPL", "demo")
                setNewsFeedIfChanged(newsResponse.feed)

            } catch (e: Exception) {
                val newsErrorMessage = when {
                    e.message?.contains("network", ignoreCase = true) == true ->
                        "Failed to load news. Check your connection."
                    e.message?.contains("api", ignoreCase = true) == true ->
                        "News API limit reached. Please try again later."
                    else -> "Failed to load news. Please try again."
                }

                setNewsErrorIfChanged(newsErrorMessage)
                setNewsFeedIfChanged(emptyList())
            } finally {
                setLoadingNewsIfChanged(false)
            }
        }
    }

    // Optimized setter methods to prevent unnecessary LiveData emissions
    private fun setLoadingStocksIfChanged(isLoading: Boolean) {
        if (_isLoadingStocks.value != isLoading) {
            _isLoadingStocks.value = isLoading
        }
    }

    private fun setLoadingIfChanged(isLoading: Boolean) {
        if (_isLoading.value != isLoading) {
            _isLoading.value = isLoading
        }
    }

    private fun setLoadingNewsIfChanged(isLoading: Boolean) {
        if (_isLoadingNews.value != isLoading) {
            _isLoadingNews.value = isLoading
        }
    }

    private fun setTopGainersIfChanged(stocks: List<StockInfo>) {
        if (_topGainers.value != stocks) {
            _topGainers.value = stocks
        }
    }

    private fun setTopLosersIfChanged(stocks: List<StockInfo>) {
        if (_topLosers.value != stocks) {
            _topLosers.value = stocks
        }
    }

    private fun setMostActiveIfChanged(stocks: List<StockInfo>) {
        if (_mostActivelyTraded.value != stocks) {
            _mostActivelyTraded.value = stocks
        }
    }

    private fun setNewsFeedIfChanged(articles: List<Article>) {
        if (_newsFeed.value != articles) {
            _newsFeed.value = articles
        }
    }

    private fun setErrorIfChanged(error: String?) {
        if (_error.value != error) {
            _error.value = error
        }
    }

    private fun setNewsErrorIfChanged(error: String?) {
        if (_newsError.value != error) {
            _newsError.value = error
        }
    }

    private fun clearErrorIfChanged() {
        if (_error.value != null) {
            _error.value = null
        }
    }

    private fun clearNewsErrorIfChanged() {
        if (_newsError.value != null) {
            _newsError.value = null
        }
    }

    // Method to retry only stock data
    fun retryStockData(apiKey: String) {
        fetchStocksJob?.cancel()
        fetchStocksJob = fetchStockData(apiKey)
    }

    // Method to retry only news data
    fun retryNewsData(apiKey: String) {
        fetchNewsJob?.cancel()
        fetchNewsJob = fetchNewsData(apiKey)
    }

    fun clearError() {
        _error.value = null
    }

    fun clearNewsError() {
        _newsError.value = null
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up jobs when ViewModel is destroyed
        fetchStocksJob?.cancel()
        fetchNewsJob?.cancel()
    }
}