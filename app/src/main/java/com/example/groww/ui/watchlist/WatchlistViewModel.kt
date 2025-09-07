package com.example.groww.ui.watchlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.groww.data.local.database.entities.WatchlistEntity
import com.example.groww.data.repository.WatchlistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WatchlistViewModel @Inject constructor(
    private val watchlistRepository: WatchlistRepository
) : ViewModel() {

    private val _watchlists = MutableLiveData<List<WatchlistEntity>>()
    val watchlists: LiveData<List<WatchlistEntity>> = _watchlists

    private val _isLoading = MutableLiveData<Boolean>(false) // Start with false
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        loadWatchlists()
    }

    fun loadWatchlists() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                watchlistRepository.getAllWatchlists()
                    .catch { e ->
                        _error.value = "Failed to load watchlists: ${e.message}"
                        _watchlists.value = emptyList()
                        _isLoading.value = false
                    }
                    .collect { watchlistsFromDb ->
                        _watchlists.value = watchlistsFromDb
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                _error.value = "Failed to load watchlists: ${e.message}"
                _watchlists.value = emptyList()
                _isLoading.value = false
            }
        }
    }

    fun createWatchlist(name: String) {
        viewModelScope.launch {
            try {
                watchlistRepository.createWatchlist(name)
                // The Flow collection will automatically update the UI
            } catch (e: Exception) {
                _error.value = "Failed to create watchlist: ${e.message}"
            }
        }
    }

    fun deleteWatchlist(watchlist: WatchlistEntity) {
        viewModelScope.launch {
            try {
                watchlistRepository.deleteWatchlist(watchlist)
                // The Flow collection will automatically update the UI
            } catch (e: Exception) {
                _error.value = "Failed to delete watchlist: ${e.message}"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}