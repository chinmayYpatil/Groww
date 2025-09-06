package com.example.groww.domain.usecase

import com.example.groww.data.repository.StockRepository
import javax.inject.Inject

class GetTopLosersUseCase @Inject constructor(
    private val repository: StockRepository
) {
    suspend fun execute(apiKey: String) = repository.getTopGainersLosers(apiKey).topLosers
}