package com.example.groww.ui.stockdetails

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockDetailsScreen(
    symbol: String,
    viewModel: StockDetailsViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.observeAsState(initial = StockDetailsState.Loading)
    val isStockInWatchlist by viewModel.isStockInWatchlist.observeAsState(initial = false)
    val actionMessage by viewModel.actionMessage.observeAsState()

    // State for showing the AddToWatchlistPopup
    var showAddToWatchlistPopup by remember { mutableStateOf(false) }

    // State for toggle button
    var isToggled by remember { mutableStateOf(false) }

    // Context for showing snackbar
    val context = LocalContext.current

    // Get stock name for the popup
    val stockName = when (val state = uiState) {
        is StockDetailsState.FullDetails -> state.companyOverview.name ?: symbol
        is StockDetailsState.PartialDetails -> symbol
        else -> symbol
    }

    // Update toggle state when watchlist status changes
    LaunchedEffect(isStockInWatchlist) {
        isToggled = isStockInWatchlist
    }

    LaunchedEffect(symbol) {
        viewModel.fetchStockDetails(BuildConfig.API_KEY)
    }

    // Show action message as snackbar
    LaunchedEffect(actionMessage) {
        actionMessage?.let { message ->
            // Here you could show a Snackbar if you want
            // For now, the message will just be logged
            viewModel.clearActionMessage()
        }
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
                    // Toggle Button for Watchlist
                    ToggleButton(
                        isToggled = isToggled,
                        onToggle = { newToggleState ->
                            if (newToggleState) {
                                // When toggled ON, show popup to select watchlists
                                showAddToWatchlistPopup = true
                            } else {
                                // When toggled OFF, remove from all watchlists
                                viewModel.removeFromAllWatchlists()
                            }
                            isToggled = newToggleState
                        }
                    )
                }
            )
        },
        snackbarHost = {
            actionMessage?.let { message ->
                Snackbar(
                    action = {
                        TextButton(onClick = { viewModel.clearActionMessage() }) {
                            Text("OK")
                        }
                    }
                ) {
                    Text(message)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Crossfade(targetState = uiState, label = "stock_details_crossfade") { state ->
                when (state) {
                    is StockDetailsState.Loading -> LoadingState()
                    is StockDetailsState.Error -> ErrorState(message = state.message, onRetry = { viewModel.fetchStockDetails(BuildConfig.API_KEY) })
                    is StockDetailsState.FullDetails -> StockDetailsContent(stockDetails = state.companyOverview, timeSeriesData = state.timeSeriesData)
                    is StockDetailsState.PartialDetails -> PartialDetailsContent(stockInfo = state.stockInfo)
                    is StockDetailsState.Empty -> EmptyState()
                }
            }
        }

        // Show AddToWatchlistPopup when toggle is turned ON
        if (showAddToWatchlistPopup) {
            AddToWatchlistBottomSheet(
                symbol = symbol,
                stockName = stockName,
                onDismiss = {
                    showAddToWatchlistPopup = false
                    viewModel.refreshWatchlistStatus()
                }
            )
        }
    }
}

@Composable
private fun ToggleButton(
    isToggled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    IconButton(
        onClick = {
            onToggle(!isToggled)
        }
    ) {
        Icon(
            imageVector = if (isToggled) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
            contentDescription = if (isToggled) "Remove from Watchlist" else "Add to Watchlist",
            tint = if (isToggled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
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
                        text = "Price: $${stockInfo.price}",
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
                        text = "$177.15",
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
            lineChart.invalidate()
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
            text = if (isPrice) "$$value" else value,
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