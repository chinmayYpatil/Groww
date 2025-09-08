package com.example.groww.ui.explore

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.groww.BuildConfig
import com.example.groww.data.model.network.StockInfo
import com.example.groww.data.model.network.Article
import com.example.groww.ui.common.StockCard
import com.example.groww.ui.theme.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextOverflow
import coil3.compose.AsyncImage
import com.example.groww.ui.news.NewsTickerCard
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    viewModel: ExploreViewModel = hiltViewModel(),
    onStockClick: (String) -> Unit = {},
    onSearchClick: () -> Unit = {},
    onViewAllClick: (String) -> Unit = {},
    onViewAllNewsClick: () -> Unit = {},
    darkTheme: Boolean,
    onThemeToggle: () -> Unit
) {
    // Cache state observations to reduce recompositions
    val topGainers by viewModel.topGainers.observeAsState(initial = emptyList())
    val topLosers by viewModel.topLosers.observeAsState(initial = emptyList())
    val mostActivelyTraded by viewModel.mostActivelyTraded.observeAsState(initial = emptyList())
    val newsFeed by viewModel.newsFeed.observeAsState(initial = emptyList())

    // Separate loading states
    val isLoadingStocks by viewModel.isLoadingStocks.observeAsState(initial = false)
    val isLoadingNews by viewModel.isLoadingNews.observeAsState(initial = false)

    val error by viewModel.error.observeAsState()
    val newsError by viewModel.newsError.observeAsState()

    // Memoize expensive operations
    val topGainersForDisplay = remember(topGainers) { topGainers.take(4) }
    val topLosersForDisplay = remember(topLosers) { topLosers.take(4) }

    // Cache scroll state to prevent recreation
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.fetchTopStocks(BuildConfig.API_KEY)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Custom Top App Bar with Gradient - Optimized with key
        key(darkTheme) {
            OptimizedGrowwTopAppBar(
                onSearchClick = onSearchClick,
                darkTheme = darkTheme,
                onThemeToggle = onThemeToggle
            )
        }

        // Main Content - Use cached scroll state
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Welcome Section - Memoized with key
            key("welcome") {
                MemoizedWelcomeSection()
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Stock Error State (only for stocks)
            error?.let { errorMessage ->
                ErrorCard(
                    message = errorMessage,
                    onRetry = { viewModel.retryStockData(BuildConfig.API_KEY) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Top Gainers Section - Optimized with key
            key("top_gainers", topGainersForDisplay.size, isLoadingStocks) {
                OptimizedVerticalStockSection(
                    title = "Top Gainers",
                    subtitle = "Stocks with highest gains today",
                    stocks = topGainersForDisplay,
                    isLoading = isLoadingStocks,
                    onStockClick = onStockClick,
                    onViewAllClick = { onViewAllClick("gainers") }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Top Losers Section - Optimized with key
            key("top_losers", topLosersForDisplay.size, isLoadingStocks) {
                OptimizedVerticalStockSection(
                    title = "Top Losers",
                    subtitle = "Stocks with highest losses today",
                    stocks = topLosersForDisplay,
                    isLoading = isLoadingStocks,
                    onStockClick = onStockClick,
                    onViewAllClick = { onViewAllClick("losers") }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Most Actively Traded Section - Optimized with key
            key("most_active", mostActivelyTraded.size, isLoadingStocks) {
                OptimizedHorizontalStockSection(
                    title = "Most Actively Traded",
                    subtitle = "Stocks with the most volume today",
                    stocks = mostActivelyTraded,
                    isLoading = isLoadingStocks,
                    onStockClick = onStockClick
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // News Ticker with separate loading state - Optimized with key
            key("news", newsFeed.size, isLoadingNews) {
                OptimizedNewsSection(
                    news = newsFeed,
                    isLoadingNews = isLoadingNews,
                    newsError = newsError,
                    onViewAllNewsClick = onViewAllNewsClick,
                    onRetryNews = { viewModel.retryNewsData(BuildConfig.API_KEY) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Special IBM Demo Section - Memoized with key
            key("ibm_demo") {
                MemoizedIBMDemoSection(
                    onStockClick = onStockClick
                )
            }
        }
    }
}

private fun getGreeting(): String {
    val currentHour = LocalTime.now().hour
    return when (currentHour) {
        in 5..11 -> "Good Morning!"
        in 12..16 -> "Good Afternoon!"
        in 17..20 -> "Good Evening!"
        else -> "Good Night!"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OptimizedGrowwTopAppBar(
    onSearchClick: () -> Unit,
    darkTheme: Boolean,
    onThemeToggle: () -> Unit
) {
    // Optimize animation with faster duration and better easing
    val rotation by animateFloatAsState(
        targetValue = if (darkTheme) 180f else 0f,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "theme_toggle_rotation"
    )

    // Cache gradient colors to prevent recreation
    val gradientColors = remember(darkTheme) {
        listOf(
            if (darkTheme) Color(0xFF00D09C) else Color(0xFF00D09C),
            if (darkTheme) Color(0xFF00B085) else Color(0xFF00B085)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(brush = Brush.verticalGradient(colors = gradientColors))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = getGreeting(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Text(
                    text = "Groww",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(
                    onClick = onSearchClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.White
                    )
                }

                IconButton(
                    onClick = onThemeToggle,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                        .rotate(rotation)
                ) {
                    Icon(
                        imageVector = if (darkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle Theme",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun MemoizedWelcomeSection() {
    // Memoize the welcome section to prevent unnecessary recompositions
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Welcome to Stock Trading",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Track your favorite stocks and make informed investment decisions",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun OptimizedVerticalStockSection(
    title: String,
    subtitle: String,
    stocks: List<StockInfo>,
    isLoading: Boolean,
    onStockClick: (String) -> Unit,
    onViewAllClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            TextButton(onClick = onViewAllClick) {
                Text(
                    text = "View All",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Simplified animations for better performance
        if (isLoading) {
            LoadingGrid()
        } else if (stocks.isEmpty()) {
            EmptyStateCard()
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.height(320.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(stocks, key = { it.ticker }) { stock ->
                    StockCard(
                        stock = stock,
                        onClick = onStockClick
                    )
                }
            }
        }
    }
}

@Composable
private fun OptimizedHorizontalStockSection(
    title: String,
    subtitle: String,
    stocks: List<StockInfo>,
    isLoading: Boolean,
    onStockClick: (String) -> Unit
) {
    Column {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            LoadingRow()
        } else if (stocks.isEmpty()) {
            EmptyStateCard()
        } else {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(stocks, key = { it.ticker }) { stock ->
                    StockCard(
                        stock = stock,
                        onClick = onStockClick,
                        modifier = Modifier.width(220.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun OptimizedNewsSection(
    news: List<Article>,
    isLoadingNews: Boolean,
    newsError: String?,
    onViewAllNewsClick: () -> Unit,
    onRetryNews: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Latest News",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            if (!isLoadingNews && news.isNotEmpty()) {
                TextButton(
                    onClick = onViewAllNewsClick
                ) {
                    Text(
                        text = "View All",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when {
            isLoadingNews -> {
                OptimizedNewsLoadingState()
            }
            newsError != null -> {
                NewsErrorState(
                    message = newsError,
                    onRetry = onRetryNews
                )
            }
            news.isEmpty() -> {
                NewsEmptyState()
            }
            else -> {
                // Simplified ticker without complex animations for better performance
                OptimizedNewsTicker(news = news)
            }
        }
    }
}

@Composable
private fun OptimizedNewsLoadingState() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Loading news...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun OptimizedNewsTicker(news: List<Article>) {
    val uriHandler = LocalUriHandler.current

    // Simplified ticker without complex animations for better performance
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState(), enabled = true)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            news.forEach { article ->
                NewsTickerCard(
                    article = article,
                    onClick = { uriHandler.openUri(article.url) }
                )
            }
        }
    }
}

@Composable
private fun MemoizedIBMDemoSection(
    onStockClick: (String) -> Unit
) {
    // Memoized IBM section to prevent unnecessary recompositions
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Featured",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Featured Demo Stock",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "IBM - Full details available for testing",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            onClick = { onStockClick("IBM") },
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp,
                pressedElevation = 8.dp
            ),
            shape = MaterialTheme.shapes.medium
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // IBM Logo Circle
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "IBM",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Column {
                        Text(
                            text = "International Business Machines",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "IBM • Technology",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Tap to view full company details",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "$185.92",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "+1.23 (0.67%)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PositiveGreen,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Demo info card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = MaterialTheme.shapes.small
        ) {
            Text(
                text = "💡 Demo Mode: IBM stock uses demo API key with full company overview data available",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@Composable
private fun LoadingGrid() {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.height(320.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(4) {
            LoadingStockCard()
        }
    }
}

@Composable
private fun LoadingRow() {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(4) {
            LoadingStockCard(modifier = Modifier.width(160.dp))
        }
    }
}

@Composable
private fun LoadingStockCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(140.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 2.dp
            )
        }
    }
}

@Composable
private fun EmptyStateCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "📦",
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No data available",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Please check back later",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorCard(
    message: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "⚠️Error Loading Data",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Retry")
            }
        }
    }
}

@Composable
private fun NewsErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "⚠️ Failed to Load News",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.height(32.dp)
            ) {
                Text(
                    text = "Retry",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
private fun NewsEmptyState() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "📰",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No news available",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Please check back later",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}