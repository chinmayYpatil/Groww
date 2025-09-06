package com.example.groww.domain.usecase

import com.example.groww.data.model.network.CompanyOverviewResponse
import com.example.groww.data.repository.StockRepository
import javax.inject.Inject

class GetStockDetailsUseCase @Inject constructor(
    private val repository: StockRepository
) {
    suspend fun execute(symbol: String, apiKey: String): CompanyOverviewResponse? =
        repository.getCompanyOverview(symbol, apiKey)
}