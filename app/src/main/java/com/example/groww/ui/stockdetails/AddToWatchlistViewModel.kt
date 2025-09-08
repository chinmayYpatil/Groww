package com.example.groww.ui.stockdetails

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.groww.data.local.database.entities.WatchlistEntity
import com.example.groww.data.repository.WatchlistRepository
import com.example.groww.domain.usecase.AddStockToWatchlistUseCase
import com.example.groww.domain.usecase.CheckStockWatchlistStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddToWatchlistViewModel @Inject constructor(
    private val watchlistRepository: WatchlistRepository,
    private val addStockToWatchlistUseCase: AddStockToWatchlistUseCase,
    private val checkStockWatchlistStatusUseCase: CheckStockWatchlistStatusUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "AddToWatchlistViewModel"
    }

    private val _watchlists = MutableLiveData<List<WatchlistEntity>>()
    val watchlists: LiveData<List<WatchlistEntity>> = _watchlists

    private val _selectedWatchlists = MutableLiveData<Set<Long>>(emptySet())
    val selectedWatchlists: LiveData<Set<Long>> = _selectedWatchlists

    private val _newWatchlistName = MutableLiveData<String>("")
    val newWatchlistName: LiveData<String> = _newWatchlistName

    private val _isAdding = MutableLiveData<Boolean>(false)
    val isAdding: LiveData<Boolean> = _isAdding

    private val _actionStatus = MutableLiveData<String?>()
    val actionStatus: LiveData<String?> = _actionStatus

    init {
        loadWatchlists()
    }

    private fun loadWatchlists() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Loading watchlists...")
                watchlistRepository.getAllWatchlists().collect { list ->
                    Log.d(TAG, "Loaded ${list.size} watchlists")
                    _watchlists.value = list
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load watchlists", e)
                _watchlists.value = emptyList()
            }
        }
    }

    fun onWatchlistSelected(watchlistId: Long) {
        val currentSelections = _selectedWatchlists.value.orEmpty().toMutableSet()
        if (currentSelections.contains(watchlistId)) {
            currentSelections.remove(watchlistId)
            Log.d(TAG, "Deselected watchlist: $watchlistId")
        } else {
            currentSelections.add(watchlistId)
            Log.d(TAG, "Selected watchlist: $watchlistId")
        }
        _selectedWatchlists.value = currentSelections
    }

    fun onNewWatchlistNameChange(name: String) {
        _newWatchlistName.value = name
    }

    /**
     * Add stock to a single watchlist immediately when checkbox is checked
     */
    fun addStockToSingleWatchlist(watchlistId: Long, symbol: String, stockName: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Adding stock $symbol to watchlist $watchlistId")

                // Check if stock already exists in this watchlist
                val existingStockWatchlists = watchlistRepository.getStockWatchlists(symbol)
                val alreadyExists = existingStockWatchlists.any { it.watchlistId == watchlistId }

                if (alreadyExists) {
                    val watchlistName = _watchlists.value?.find { it.id == watchlistId }?.name ?: "watchlist"
                    _actionStatus.value = "Already in $watchlistName"
                    Log.d(TAG, "Stock already exists in watchlist $watchlistId")
                } else {
                    // Add to watchlist
                    addStockToWatchlistUseCase.execute(watchlistId, symbol, stockName)
                    val watchlistName = _watchlists.value?.find { it.id == watchlistId }?.name ?: "watchlist"
                    _actionStatus.value = "Added to $watchlistName!"
                    Log.d(TAG, "Successfully added stock to watchlist $watchlistId")
                }

            } catch (e: Exception) {
                val errorMessage = "Failed to add stock: ${e.message}"
                _actionStatus.value = errorMessage
                Log.e(TAG, errorMessage, e)
            }
        }
    }

    /**
     * Add stock to multiple selected watchlists at once
     */
    fun addStockToWatchlists(symbol: String, stockName: String) {
        viewModelScope.launch {
            _isAdding.value = true
            _actionStatus.value = null

            try {
                Log.d(TAG, "Adding stock $symbol ($stockName) to watchlists...")

                var addedCount = 0
                var duplicateCount = 0
                val duplicateWatchlists = mutableListOf<String>()

                // Get existing watchlists containing this stock to prevent duplicates
                val existingStockWatchlists = watchlistRepository.getStockWatchlists(symbol)
                val existingWatchlistIds = existingStockWatchlists.map { it.watchlistId }.toSet()

                // Add to selected existing watchlists
                val selectedIds = _selectedWatchlists.value.orEmpty()
                for (watchlistId in selectedIds) {
                    try {
                        if (existingWatchlistIds.contains(watchlistId)) {
                            // Stock already exists in this watchlist
                            duplicateCount++
                            val watchlistName = _watchlists.value?.find { it.id == watchlistId }?.name ?: "Unknown"
                            duplicateWatchlists.add(watchlistName)
                            Log.d(TAG, "Stock already exists in watchlist $watchlistId ($watchlistName)")
                        } else {
                            // Add to watchlist
                            Log.d(TAG, "Adding to existing watchlist ID: $watchlistId")
                            addStockToWatchlistUseCase.execute(watchlistId, symbol, stockName)
                            addedCount++
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to add to watchlist $watchlistId", e)
                    }
                }

                // Create and add to new watchlist if name is not empty
                val newName = _newWatchlistName.value?.trim()
                if (!newName.isNullOrBlank()) {
                    try {
                        Log.d(TAG, "Creating new watchlist: $newName")
                        val newWatchlistId = watchlistRepository.createWatchlist(newName)
                        Log.d(TAG, "Created watchlist with ID: $newWatchlistId")

                        addStockToWatchlistUseCase.execute(newWatchlistId, symbol, stockName)
                        addedCount++

                        _newWatchlistName.value = "" // Clear the input field
                        Log.d(TAG, "Added stock to new watchlist")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to create new watchlist", e)
                        throw e
                    }
                }

                // Prepare status message
                val statusMessages = mutableListOf<String>()

                if (addedCount > 0) {
                    statusMessages.add("Added to $addedCount watchlist${if (addedCount > 1) "s" else ""}!")
                }

                if (duplicateCount > 0) {
                    val watchlistNames = if (duplicateWatchlists.size <= 2) {
                        duplicateWatchlists.joinToString(" and ")
                    } else {
                        "${duplicateWatchlists.take(2).joinToString(", ")} and ${duplicateWatchlists.size - 2} more"
                    }
                    statusMessages.add("Already in: $watchlistNames")
                }

                if (statusMessages.isNotEmpty()) {
                    _actionStatus.value = statusMessages.joinToString(". ")
                    Log.d(TAG, "Operation completed: ${statusMessages.joinToString(". ")}")

                    // Clear selections after successful addition
                    _selectedWatchlists.value = emptySet()
                } else {
                    _actionStatus.value = "Please select a watchlist or create a new one"
                }

            } catch (e: Exception) {
                val errorMessage = "Error adding stock: ${e.message}"
                _actionStatus.value = errorMessage
                Log.e(TAG, errorMessage, e)
            } finally {
                _isAdding.value = false
            }
        }
    }

    fun clearStatus() {
        _actionStatus.value = null
    }

    fun clearSelections() {
        _selectedWatchlists.value = emptySet()
    }
}