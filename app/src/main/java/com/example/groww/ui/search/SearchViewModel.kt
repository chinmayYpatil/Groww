package com.example.groww.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.groww.data.model.network.BestMatch
import com.example.groww.data.repository.StockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val stockRepository: StockRepository
) : ViewModel() {

    private val _searchResults = MutableLiveData<List<BestMatch>?>()
    val searchResults: LiveData<List<BestMatch>> = _searchResults as LiveData<List<BestMatch>>

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var searchJob: Job? = null

    fun searchStocks(query: String, apiKey: String) {
        // Cancel previous search
        searchJob?.cancel()

        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        searchJob = viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                // Add debounce
                delay(500)

                val response = stockRepository.searchSymbol(query, apiKey)
                _searchResults.value = response.bestMatches

            } catch (e: Exception) {
                _error.value = when {
                    e.message?.contains("API limit reached", ignoreCase = true) == true ->
                        "Sorry, API limit exhausted. Please search for tesco as it's available in the demo API."
                    e.message?.contains("network", ignoreCase = true) == true ->
                        "Network error. Please check your connection."
                    else -> "Search failed. Please try again."
                }
                _searchResults.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}