package com.example.groww.domain.usecase

import com.example.groww.data.model.network.TopGainersLosersResponse
import com.example.groww.data.repository.StockRepository
import javax.inject.Inject

class GetTopGainersUseCase @Inject constructor(
    private val repository: StockRepository
) {
    suspend fun execute(apiKey: String) = repository.getTopGainersLosers(apiKey).topGainers

    // New function to get the entire response
    suspend fun getTopGainersLosersResponse(apiKey: String): TopGainersLosersResponse {
        return repository.getTopGainersLosers(apiKey)
    }
}