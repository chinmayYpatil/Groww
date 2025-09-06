package com.example.groww.ui.stockdetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.groww.data.local.database.entities.WatchlistEntity
import com.example.groww.data.repository.WatchlistRepository
import com.example.groww.domain.usecase.AddStockToWatchlistUseCase
import com.example.groww.domain.usecase.CheckStockWatchlistStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddToWatchlistViewModel @Inject constructor(
    private val watchlistRepository: WatchlistRepository,
    private val addStockToWatchlistUseCase: AddStockToWatchlistUseCase,
    private val checkStockWatchlistStatusUseCase: CheckStockWatchlistStatusUseCase
) : ViewModel() {

    private val _watchlists = MutableLiveData<List<WatchlistEntity>>()
    val watchlists: LiveData<List<WatchlistEntity>> = _watchlists

    private val _selectedWatchlists = MutableLiveData<Set<Long>>(emptySet())
    val selectedWatchlists: LiveData<Set<Long>> = _selectedWatchlists

    private val _newWatchlistName = MutableLiveData<String>()
    val newWatchlistName: LiveData<String> = _newWatchlistName

    private val _isAdding = MutableLiveData<Boolean>()
    val isAdding: LiveData<Boolean> = _isAdding

    private val _actionStatus = MutableLiveData<String?>()
    val actionStatus: LiveData<String?> = _actionStatus

    init {
        loadWatchlists()
    }

    private fun loadWatchlists() {
        viewModelScope.launch {
            watchlistRepository.getAllWatchlists().collect { list ->
                _watchlists.value = list
            }
        }
    }

    fun onWatchlistSelected(watchlistId: Long) {
        val currentSelections = _selectedWatchlists.value.orEmpty().toMutableSet()
        if (currentSelections.contains(watchlistId)) {
            currentSelections.remove(watchlistId)
        } else {
            currentSelections.add(watchlistId)
        }
        _selectedWatchlists.value = currentSelections
    }

    fun onNewWatchlistNameChange(name: String) {
        _newWatchlistName.value = name
    }

    fun addStockToWatchlists(symbol: String, stockName: String) {
        viewModelScope.launch {
            _isAdding.value = true
            try {
                // Add to selected existing watchlists
                _selectedWatchlists.value?.forEach { watchlistId ->
                    addStockToWatchlistUseCase.execute(watchlistId, symbol, stockName)
                }

                // Create and add to new watchlist if name is not empty
                val newName = _newWatchlistName.value
                if (!newName.isNullOrBlank()) {
                    val newWatchlistId = watchlistRepository.createWatchlist(newName)
                    addStockToWatchlistUseCase.execute(newWatchlistId, symbol, stockName)
                    _newWatchlistName.value = "" // Clear the input field
                }
                _actionStatus.value = "Stock added to watchlist(s)."
            } catch (e: Exception) {
                _actionStatus.value = "Error adding stock: ${e.message}"
            } finally {
                _isAdding.value = false
            }
        }
    }
}