package com.example.groww.ui.stockdetails

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.groww.BuildConfig
import com.example.groww.data.model.network.CompanyOverviewResponse
import com.example.groww.data.model.network.StockInfo
import com.example.groww.data.model.network.TimeSeriesResponse
import com.example.groww.ui.theme.GrowwTheme
import com.example.groww.ui.theme.NegativeRed
import com.example.groww.ui.theme.PositiveGreen
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.log10
import kotlin.math.pow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockDetailsScreen(
    symbol: String,
    viewModel: StockDetailsViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.observeAsState(initial = StockDetailsState.Loading)
    val isStockInWatchlist by viewModel.isStockInWatchlist.observeAsState(initial = false)

    LaunchedEffect(symbol) {
        viewModel.fetchStockDetails(BuildConfig.API_KEY)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Open AddToWatchlistPopup */ }) {
                        Icon(
                            imageVector = if (isStockInWatchlist) Icons.Default.Star else Icons.Default.Star,
                            contentDescription = "Toggle Watchlist",
                            tint = if (isStockInWatchlist) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (val state = uiState) {
                is StockDetailsState.Loading -> LoadingState()
                is StockDetailsState.Error -> ErrorState(message = state.message, onRetry = { viewModel.fetchStockDetails(BuildConfig.API_KEY) })
                is StockDetailsState.FullDetails -> StockDetailsContent(stockDetails = state.companyOverview, timeSeriesData = state.timeSeriesData)
                is StockDetailsState.PartialDetails -> PartialDetailsContent(stockInfo = state.stockInfo)
                is StockDetailsState.Empty -> EmptyState()
            }
        }
    }
}

@Composable
private fun PartialDetailsContent(stockInfo: StockInfo) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stockInfo.ticker,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Price: ₹${stockInfo.price}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    val isPositive = stockInfo.changeAmount.toFloatOrNull() ?: 0f >= 0
                    val changeColor = if (isPositive) PositiveGreen else NegativeRed
                    Text(
                        text = stockInfo.changeAmount,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = changeColor
                    )
                    Text(
                        text = "(${stockInfo.changePercentage})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = changeColor
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "We don't have the full company information for this stock currently. Please check back later.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StockDetailsContent(stockDetails: CompanyOverviewResponse, timeSeriesData: TimeSeriesResponse?) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            // Price and Change
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stockDetails.name.orEmpty(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${stockDetails.symbol.orEmpty()}, Common Stock",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Placeholder for price and change from a real-time API
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹177.15",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "+0.41%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PositiveGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Graph
            if (timeSeriesData != null && timeSeriesData.timeSeriesDaily.isNotEmpty()) {
                StockPriceGraph(timeSeriesData = timeSeriesData)
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color.LightGray)
                ) {
                    Text(
                        text = "No graph data available",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // About Section
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "About ${stockDetails.name.orEmpty()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stockDetails.description.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Chip(text = "Industry: ${stockDetails.industry.orEmpty()}")
                    Chip(text = "Sector: ${stockDetails.sector.orEmpty()}")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Key Metrics
            KeyMetricsSection(stockDetails)

            Spacer(modifier = Modifier.height(100.dp)) // Space for bottom navigation
        }
    }
}

@Composable
private fun StockPriceGraph(timeSeriesData: TimeSeriesResponse) {
    val entries = ArrayList<Entry>()
    val dates = ArrayList<String>()
    val sortedDates = timeSeriesData.timeSeriesDaily.keys.sortedBy { it }
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    for ((index, date) in sortedDates.withIndex()) {
        val data = timeSeriesData.timeSeriesDaily[date]
        if (data != null) {
            entries.add(Entry(index.toFloat(), data.close.toFloat()))
            dates.add(date)
        }
    }

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(top = 16.dp, bottom = 16.dp),
        factory = { context ->
            LineChart(context).apply {
                // Configure chart appearance
                description.isEnabled = false
                legend.isEnabled = false
                axisRight.isEnabled = false
                xAxis.setDrawGridLines(false)
                xAxis.setDrawLabels(false)
                axisLeft.setDrawGridLines(false)
                setTouchEnabled(true)
                setPinchZoom(true)
                isDragEnabled = true
            }
        },
        update = { lineChart ->
            val dataSet = LineDataSet(entries, "Stock Price")
            dataSet.color = PositiveGreen.toArgb()
            dataSet.valueTextColor = PositiveGreen.toArgb()
            dataSet.valueTextSize = 10f
            dataSet.lineWidth = 2f
            dataSet.circleRadius = 4f
            dataSet.setDrawCircleHole(false)
            dataSet.setDrawCircles(false)
            dataSet.setDrawValues(false)
            dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER

            val lineData = LineData(dataSet)
            lineChart.data = lineData
            lineChart.invalidate() // refresh chart
        }
    )
}

@Composable
private fun Chip(text: String) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun KeyMetricsSection(stockDetails: CompanyOverviewResponse) {
    Column(modifier = Modifier.fillMaxWidth()) {
        MetricRow(
            label1 = "52-Week High",
            value1 = stockDetails.fiftyTwoWeekHigh.orEmpty(),
            label2 = "52-Week Low",
            value2 = stockDetails.fiftyTwoWeekLow.orEmpty(),
            isPrice = true
        )
        MetricRow(
            label1 = "Market Cap",
            value1 = stockDetails.marketCapitalization.orEmpty(),
            label2 = "P/E Ratio",
            value2 = stockDetails.peRatio.orEmpty(),
            isPrice = false
        )
        MetricRow(
            label1 = "Beta",
            value1 = stockDetails.beta.orEmpty(),
            label2 = "Dividend Yield",
            value2 = stockDetails.dividendYield.orEmpty(),
            isPrice = false
        )
        MetricRow(
            label1 = "Profit Margin",
            value1 = stockDetails.profitMargin.orEmpty(),
            label2 = "EPS",
            value2 = stockDetails.eps.orEmpty(),
            isPrice = false
        )
    }
}

@Composable
private fun MetricRow(
    label1: String,
    value1: String,
    label2: String,
    value2: String,
    isPrice: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        MetricItem(label = label1, value = value1, isPrice = isPrice, modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.width(16.dp))
        MetricItem(label = label2, value = value2, isPrice = isPrice, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun MetricItem(label: String, value: String, isPrice: Boolean, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (isPrice) "₹$value" else value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "⚠️",
            style = MaterialTheme.typography.displayLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Failed to load stock details",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Try Again")
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "No stock details available.")
    }
}