package com.example.groww.domain.usecase


import com.example.groww.data.repository.WatchlistRepository
import javax.inject.Inject

class CheckStockWatchlistStatusUseCase @Inject constructor(
    private val repository: WatchlistRepository
) {
    suspend fun execute(symbol: String): Boolean {
        return repository.isStockInAnyWatchlist(symbol)
    }
}