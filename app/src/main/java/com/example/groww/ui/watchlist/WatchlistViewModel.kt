package com.example.groww.ui.watchlist

import android.util.Log
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

    companion object {
        private const val TAG = "WatchlistViewModel"
    }

    private val _watchlists = MutableLiveData<List<WatchlistEntity>>()
    val watchlists: LiveData<List<WatchlistEntity>> = _watchlists

    private val _isLoading = MutableLiveData<Boolean>(true)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        Log.d(TAG, "WatchlistViewModel initialized")
        loadWatchlists()
    }

    fun loadWatchlists() {
        Log.d(TAG, "Loading watchlists...")
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                watchlistRepository.getAllWatchlists()
                    .catch { e ->
                        Log.e(TAG, "Error loading watchlists", e)
                        _error.value = "Failed to load watchlists: ${e.message}"
                        _watchlists.value = emptyList()
                        _isLoading.value = false
                    }
                    .collect { watchlistsFromDb ->
                        Log.d(TAG, "Received ${watchlistsFromDb.size} watchlists from database")
                        _watchlists.value = watchlistsFromDb
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Exception loading watchlists", e)
                _error.value = "Failed to load watchlists: ${e.message}"
                _watchlists.value = emptyList()
                _isLoading.value = false
            }
        }
    }

    fun createWatchlist(name: String) {
        Log.d(TAG, "Creating watchlist: $name")
        viewModelScope.launch {
            try {
                val watchlistId = watchlistRepository.createWatchlist(name)
                Log.d(TAG, "Created watchlist with ID: $watchlistId")
                // The Flow collection will automatically update the UI
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create watchlist", e)
                _error.value = "Failed to create watchlist: ${e.message}"
            }
        }
    }

    fun deleteWatchlist(watchlist: WatchlistEntity) {
        Log.d(TAG, "Deleting watchlist: ${watchlist.name}")
        viewModelScope.launch {
            try {
                watchlistRepository.deleteWatchlist(watchlist)
                Log.d(TAG, "Deleted watchlist: ${watchlist.name}")
                // The Flow collection will automatically update the UI
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete watchlist", e)
                _error.value = "Failed to delete watchlist: ${e.message}"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}