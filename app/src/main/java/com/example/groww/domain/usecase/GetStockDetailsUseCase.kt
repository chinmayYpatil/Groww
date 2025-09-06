package com.example.groww.domain.usecase

import com.example.groww.data.repository.StockRepository
import javax.inject.Inject

class GetStockDetailsUseCase @Inject constructor(
    private val repository: StockRepository
) {
    suspend fun execute(symbol: String, apiKey: String) =
        repository.getCompanyOverview(symbol, apiKey)
}