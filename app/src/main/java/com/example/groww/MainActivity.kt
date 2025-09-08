package com.example.groww

import android.content.ComponentCallbacks2
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.groww.ui.explore.ExploreScreen
import com.example.groww.ui.navigation.GrowwBottomNavigation
import com.example.groww.ui.news.NewsCard
import com.example.groww.ui.stockdetails.StockDetailsScreen
import com.example.groww.ui.theme.GrowwTheme
import com.example.groww.ui.search.SearchScreen
import com.example.groww.ui.viewall.ViewAllScreen
import com.example.groww.ui.watchlist.WatchlistScreen
import com.example.groww.ui.watchlist.WatchlistDetailScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        )

        setContent {
            GrowwApp()
        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        // Clear resources when memory is low
        if (level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE) {
            System.gc()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrowwApp() {
    val navController = rememberNavController()
    var darkTheme by rememberSaveable { mutableStateOf(false) }

    GrowwTheme(darkTheme = darkTheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                bottomBar = {
                    GrowwBottomNavigation(navController = navController)
                }
            ) { innerPadding ->
                OptimizedAppNavHost(
                    navController = navController,
                    modifier = Modifier.padding(innerPadding),
                    darkTheme = darkTheme,
                    onThemeToggle = { darkTheme = !darkTheme }
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OptimizedAppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    darkTheme: Boolean,
    onThemeToggle: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = "explore_route",
        modifier = modifier,
        // Optimized navigation transitions
        enterTransition = {
            when (targetState.destination.route) {
                "stock_details/{symbol}", "search_route", "view_all/{type}", "view_all_news" -> {
                    // Forward navigation - slide in from right with fade
                    slideInHorizontally(
                        initialOffsetX = { fullWidth -> fullWidth / 3 },
                        animationSpec = tween(300, easing = FastOutSlowInEasing)
                    ) + fadeIn(
                        animationSpec = tween(300, easing = FastOutSlowInEasing)
                    )
                }
                "watchlist_detail/{watchlistId}/{watchlistName}" -> {
                    // Watchlist detail - slide up from bottom
                    slideInVertically(
                        initialOffsetY = { fullHeight -> fullHeight / 4 },
                        animationSpec = tween(350, easing = FastOutSlowInEasing)
                    ) + fadeIn(
                        animationSpec = tween(250, easing = FastOutSlowInEasing)
                    )
                }
                else -> {
                    // Tab navigation - just fade
                    fadeIn(
                        animationSpec = tween(200, easing = LinearEasing)
                    )
                }
            }
        },
        exitTransition = {
            when (targetState.destination.route) {
                "stock_details/{symbol}", "search_route", "view_all/{type}", "view_all_news" -> {
                    // Stay put when navigating forward
                    fadeOut(
                        animationSpec = tween(150, easing = FastOutLinearInEasing)
                    )
                }
                else -> {
                    // Tab navigation
                    fadeOut(
                        animationSpec = tween(150, easing = LinearEasing)
                    )
                }
            }
        },
        popEnterTransition = {
            when (initialState.destination.route) {
                "stock_details/{symbol}", "search_route", "view_all/{type}", "view_all_news" -> {
                    // Coming back to main screens - fade in quickly
                    fadeIn(
                        animationSpec = tween(200, easing = FastOutSlowInEasing)
                    )
                }
                "watchlist_detail/{watchlistId}/{watchlistName}" -> {
                    // Coming back from watchlist detail
                    slideInVertically(
                        initialOffsetY = { fullHeight -> -fullHeight / 6 },
                        animationSpec = tween(250, easing = FastOutSlowInEasing)
                    ) + fadeIn(
                        animationSpec = tween(200, easing = FastOutSlowInEasing)
                    )
                }
                else -> {
                    fadeIn(animationSpec = tween(150, easing = LinearEasing))
                }
            }
        },
        popExitTransition = {
            when (initialState.destination.route) {
                "stock_details/{symbol}", "search_route", "view_all/{type}", "view_all_news" -> {
                    // Slide out to right when going back
                    slideOutHorizontally(
                        targetOffsetX = { fullWidth -> fullWidth / 2 },
                        animationSpec = tween(250, easing = FastOutSlowInEasing)
                    ) + fadeOut(
                        animationSpec = tween(200, easing = FastOutLinearInEasing)
                    )
                }
                "watchlist_detail/{watchlistId}/{watchlistName}" -> {
                    // Slide down when going back from watchlist detail
                    slideOutVertically(
                        targetOffsetY = { fullHeight -> fullHeight / 3 },
                        animationSpec = tween(250, easing = FastOutSlowInEasing)
                    ) + fadeOut(
                        animationSpec = tween(200, easing = FastOutLinearInEasing)
                    )
                }
                else -> {
                    fadeOut(animationSpec = tween(150, easing = LinearEasing))
                }
            }
        }
    ) {
        composable("explore_route") {
            ExploreScreen(
                onStockClick = { symbol ->
                    navController.navigate("stock_details/$symbol") {
                        launchSingleTop = true
                    }
                },
                onSearchClick = {
                    navController.navigate("search_route") {
                        launchSingleTop = true
                    }
                },
                onViewAllClick = { type ->
                    navController.navigate("view_all/$type") {
                        launchSingleTop = true
                    }
                },
                onViewAllNewsClick = {
                    navController.navigate("view_all_news") {
                        launchSingleTop = true
                    }
                },
                darkTheme = darkTheme,
                onThemeToggle = onThemeToggle
            )
        }

        composable("watchlist_route") {
            WatchlistScreen(
                onWatchlistClick = { watchlistId, watchlistName ->
                    navController.navigate("watchlist_detail/$watchlistId/$watchlistName") {
                        launchSingleTop = true
                    }
                },
                onCreateWatchlist = {
                    navController.navigate("create_watchlist") {
                        launchSingleTop = true
                    }
                },
                onExploreStocks = {
                    navController.navigate("explore_route") {
                        popUpTo("watchlist_route") { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }

        // Watchlist Detail Screen
        composable("watchlist_detail/{watchlistId}/{watchlistName}") { backStackEntry ->
            val watchlistId = remember {
                backStackEntry.arguments?.getString("watchlistId")?.toLongOrNull() ?: 0L
            }
            val watchlistName = remember {
                backStackEntry.arguments?.getString("watchlistName") ?: ""
            }

            WatchlistDetailScreen(
                watchlistId = watchlistId,
                watchlistName = watchlistName,
                onBackClick = {
                    navController.popBackStack()
                },
                onStockClick = { symbol ->
                    navController.navigate("stock_details/$symbol") {
                        launchSingleTop = true
                    }
                }
            )
        }

        // Stock Details Screen - Optimized
        composable("stock_details/{symbol}") { backStackEntry ->
            val symbol = remember {
                backStackEntry.arguments?.getString("symbol") ?: ""
            }

            // Wrap in AnimatedContent for smooth internal transitions
            AnimatedContent(
                targetState = symbol,
                transitionSpec = {
                    fadeIn(
                        animationSpec = tween(200, easing = FastOutSlowInEasing)
                    ) with fadeOut(
                        animationSpec = tween(150, easing = FastOutLinearInEasing)
                    )
                },
                label = "stock_details_content"
            ) { currentSymbol ->
                StockDetailsScreen(
                    symbol = currentSymbol,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }

        composable("search_route") {
            SearchScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onStockClick = { symbol ->
                    navController.navigate("stock_details/$symbol") {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable("view_all/{type}") { backStackEntry ->
            val type = remember {
                backStackEntry.arguments?.getString("type") ?: ""
            }

            ViewAllScreen(
                type = type,
                onBackClick = {
                    navController.popBackStack()
                },
                onStockClick = { symbol ->
                    navController.navigate("stock_details/$symbol") {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable("view_all_news") {
            NewsCard(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable("create_watchlist") {
            // TODO: Implement CreateWatchlistScreen
            // For now, just pop back
        }
    }
}