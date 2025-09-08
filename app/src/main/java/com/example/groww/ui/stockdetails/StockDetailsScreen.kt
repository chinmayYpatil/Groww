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
import com.example.groww.data.model.network.TimeSeriesDailyData
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.absoluteValue
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import android.widget.TextView
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.utils.MPPointF
import android.content.Context
import androidx.core.content.ContextCompat
import com.example.groww.R

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
    val timeSeriesData by viewModel.timeSeriesData.observeAsState()
    val isLoadingTimeSeries by viewModel.isLoadingTimeSeries.observeAsState(initial = false)

    var showAddToWatchlistPopup by remember { mutableStateOf(false) }
    var isToggled by remember { mutableStateOf(false) }
    var selectedTimeFrame by remember { mutableStateOf("1D") }

    val stockName = when (val state = uiState) {
        is StockDetailsState.FullDetails -> state.companyOverview.name ?: symbol
        is StockDetailsState.PartialDetails -> symbol
        else -> symbol
    }

    LaunchedEffect(isStockInWatchlist) {
        isToggled = isStockInWatchlist
    }

    LaunchedEffect(symbol) {
        viewModel.fetchStockDetails(BuildConfig.API_KEY)
        viewModel.fetchTimeSeriesData(selectedTimeFrame, BuildConfig.API_KEY)
    }

    LaunchedEffect(selectedTimeFrame) {
        viewModel.fetchTimeSeriesData(selectedTimeFrame, BuildConfig.API_KEY)
    }

    LaunchedEffect(actionMessage) {
        actionMessage?.let { message ->
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
                    ToggleButton(
                        isToggled = isToggled,
                        onToggle = { newToggleState ->
                            if (newToggleState) {
                                showAddToWatchlistPopup = true
                            } else {
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
                    is StockDetailsState.FullDetails -> StockDetailsContent(
                        stockDetails = state.companyOverview,
                        timeSeriesResponse = timeSeriesData,
                        selectedTimeFrame = selectedTimeFrame,
                        onTimeFrameSelected = { newTimeFrame -> selectedTimeFrame = newTimeFrame },
                        isLoadingTimeSeries = isLoadingTimeSeries
                    )
                    is StockDetailsState.PartialDetails -> PartialDetailsContent(stockInfo = state.stockInfo)
                    is StockDetailsState.Empty -> EmptyState()
                }
            }
        }

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
                    val isPositive = stockInfo.changeAmount.toFloatOrNull()?.let { it >= 0 } ?: false
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
private fun StockDetailsContent(
    stockDetails: CompanyOverviewResponse,
    timeSeriesResponse: TimeSeriesResponse?,
    selectedTimeFrame: String,
    onTimeFrameSelected: (String) -> Unit,
    isLoadingTimeSeries: Boolean
) {
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

            // Updated chart data logic
            val chartData = remember(timeSeriesResponse, selectedTimeFrame) {
                if (timeSeriesResponse != null) {
                    val sortedTimeSeries = when (selectedTimeFrame) {
                        "1D" -> timeSeriesResponse.timeSeriesIntraday?.toSortedMap()?.entries?.toList()?.takeLast(78)
                        "1W" -> timeSeriesResponse.timeSeriesDaily?.toSortedMap()?.entries?.toList()?.takeLast(5)
                        "1M" -> timeSeriesResponse.timeSeriesMonthly?.toSortedMap()?.entries?.toList()
                        "3M" -> timeSeriesResponse.timeSeriesDaily?.toSortedMap()?.entries?.toList()?.takeLast(60)
                        "1Y" -> timeSeriesResponse.timeSeriesWeekly?.toSortedMap()?.entries?.toList()?.takeLast(52)
                        else -> emptyList()
                    }
                    sortedTimeSeries?.associate { it.key to it.value } ?: emptyMap()
                } else {
                    emptyMap()
                }
            }


            if (isLoadingTimeSeries) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (chartData.isNotEmpty()) {
                StockPriceGraph(timeSeriesData = chartData, selectedTimeFrame = selectedTimeFrame)
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

            Spacer(modifier = Modifier.height(16.dp))
            TimeFrameSelector(selectedTimeFrame, onTimeFrameSelected)

            Spacer(modifier = Modifier.height(24.dp))

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

            KeyMetricsSection(stockDetails)

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun StockPriceGraph(timeSeriesData: Map<String, TimeSeriesDailyData>, selectedTimeFrame: String) {
    val entries = ArrayList<Entry>()
    val dates = ArrayList<String>()
    val sortedDates = timeSeriesData.keys.sortedBy { it }

    for ((index, date) in sortedDates.withIndex()) {
        val data = timeSeriesData[date]
        if (data != null) {
            entries.add(Entry(index.toFloat(), data.close.toFloat()))
            dates.add(date)
        }
    }

    val dateFormatter = remember(selectedTimeFrame) {
        when (selectedTimeFrame) {
            "1D" -> SimpleDateFormat("HH:mm", Locale.US)
            else -> SimpleDateFormat("MMM d", Locale.US)
        }
    }

    val xAxisValueFormatter = remember(dates, selectedTimeFrame) {
        object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                val index = value.toInt()
                return if (index >= 0 && index < dates.size) {
                    val dateString = dates[index]
                    val inputFormat = if (selectedTimeFrame == "1D") {
                        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                    } else {
                        SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    }
                    val date = inputFormat.parse(dateString)
                    date?.let { dateFormatter.format(it) } ?: ""
                } else {
                    ""
                }
            }
        }
    }

    val context = LocalContext.current
    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
    val markerBackgroundColor = ContextCompat.getColor(context, com.example.groww.R.color.groww_text_secondary)

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(top = 16.dp, bottom = 16.dp),
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                legend.isEnabled = false

                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.setDrawGridLines(false)
                xAxis.setDrawLabels(true)
                xAxis.valueFormatter = xAxisValueFormatter
                xAxis.textColor = onSurfaceVariantColor

                axisLeft.setDrawGridLines(false)
                axisLeft.textColor = onSurfaceVariantColor

                axisRight.isEnabled = false

                setTouchEnabled(true)
                setPinchZoom(true)
                isDragEnabled = true

                // Attach custom marker view
                val markerView = CustomMarkerView(context, com.example.groww.R.layout.custom_marker_view_layout, dates, selectedTimeFrame)
                markerView.chartView = this
                marker = markerView
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

            // Enable highlighting for the marker
            dataSet.setDrawHighlightIndicators(true)
            dataSet.highLightColor = primaryColor
            dataSet.isHighlightEnabled = true
            dataSet.setDrawVerticalHighlightIndicator(true)

            val lineData = LineData(dataSet)
            lineChart.data = lineData
            lineChart.invalidate()
        }
    )
}

// Custom MarkerView class
class CustomMarkerView(
    context: Context,
    layoutResource: Int,
    private val dates: List<String>,
    private val selectedTimeFrame: String
) : MarkerView(context, layoutResource) {

    private val tvContent: TextView = findViewById(com.example.groww.R.id.tvContent)
    private val tvPrice: TextView = findViewById(com.example.groww.R.id.tvPrice)

    private val dateFormatter = when (selectedTimeFrame) {
        "1D" -> SimpleDateFormat("MMM d, HH:mm", Locale.US)
        else -> SimpleDateFormat("MMM d, yyyy", Locale.US)
    }

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        if (e != null) {
            val dateIndex = e.x.toInt()
            val price = e.y

            val dateString = if (dateIndex >= 0 && dateIndex < dates.size) {
                val inputFormat = if (selectedTimeFrame == "1D") {
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                } else {
                    SimpleDateFormat("yyyy-MM-dd", Locale.US)
                }
                val date = inputFormat.parse(dates[dateIndex])
                date?.let { dateFormatter.format(it) } ?: "N/A"
            } else {
                "N/A"
            }

            tvContent.text = dateString
            tvPrice.text = "$${"%.2f".format(price)}"
        }
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-(width / 2f), -height.toFloat() - 10)
    }
}

@Composable
private fun TimeFrameSelector(
    selectedTimeFrame: String,
    onTimeFrameSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        val timeFrames = listOf("1D", "1W", "1M", "3M", "1Y")
        timeFrames.forEach { timeFrame ->
            val isSelected = selectedTimeFrame == timeFrame
            OutlinedButton(
                onClick = { onTimeFrameSelected(timeFrame) },
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = MaterialTheme.shapes.extraSmall,
                border = if (isSelected) ButtonDefaults.outlinedButtonBorder else ButtonDefaults.outlinedButtonBorder,
                modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
            ) {
                Text(text = timeFrame)
            }
        }
    }
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