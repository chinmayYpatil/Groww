package com.example.groww.ui.stockdetails

import com.example.groww.data.model.network.CompanyOverviewResponse
import com.example.groww.data.model.network.StockInfo
import com.example.groww.data.model.network.TimeSeriesResponse

sealed class StockDetailsState {
    object Loading : StockDetailsState()
    data class FullDetails(val companyOverview: CompanyOverviewResponse, val timeSeriesData: TimeSeriesResponse?) : StockDetailsState()
    data class PartialDetails(val stockInfo: StockInfo) : StockDetailsState()
    data class Error(val message: String) : StockDetailsState()
    object Empty : StockDetailsState()
}