package com.example.groww.domain.usecase

import com.example.groww.data.repository.WatchlistRepository
import javax.inject.Inject

class AddStockToWatchlistUseCase @Inject constructor(
    private val repository: WatchlistRepository
) {
    suspend fun execute(watchlistId: Long, symbol: String, name: String) {
        repository.addStockToWatchlist(watchlistId, symbol, name)
    }
}