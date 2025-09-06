package com.example.groww.ui.explore

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.groww.data.model.network.StockInfo
import com.example.groww.ui.common.StockCard

@Composable
fun ExploreScreen(
    viewModel: ExploreViewModel = hiltViewModel()
) {
    val topGainers by viewModel.topGainers.observeAsState(initial = emptyList())
    val topLosers by viewModel.topLosers.observeAsState(initial = emptyList())

    LaunchedEffect(Unit) {
        // TODO: Replace with a secure way to get API key
        viewModel.fetchTopStocks("ZVQPOOAXANQ89QAI")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Top Gainers", modifier = Modifier.padding(bottom = 8.dp))
        StockGrid(stocks = topGainers)

        Text("Top Losers", modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
        StockGrid(stocks = topLosers)
    }
}

@Composable
fun StockGrid(stocks: List<StockInfo>) {
    if (stocks.isEmpty()) {
        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.padding(8.dp)
        ) {
            items(stocks) { stock ->
                StockCard(stock = stock)
            }
        }
    }
}