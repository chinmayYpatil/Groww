package com.example.groww.domain.usecase


import com.example.groww.data.repository.StockRepository
import com.example.groww.data.model.network.NewsSentimentResponse
import javax.inject.Inject

class GetNewsSentimentUseCase @Inject constructor(
    private val repository: StockRepository
) {
    suspend fun execute(tickers: String, apiKey: String): NewsSentimentResponse {
        return repository.getNewsSentiment(tickers, apiKey)
    }
}