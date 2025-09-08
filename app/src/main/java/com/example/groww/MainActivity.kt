package com.example.groww

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
        setContent {
            GrowwApp()
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
        modifier = modifier
    ) {
        composable("explore_route") {
            ExploreScreen(
                onStockClick = { symbol ->
                    navController.navigate("stock_details/$symbol") {
                        // Optimize navigation animations
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

        // Stock Details Screen
        composable("stock_details/{symbol}") { backStackEntry ->
            val symbol = remember {
                backStackEntry.arguments?.getString("symbol") ?: ""
            }

            StockDetailsScreen(
                symbol = symbol,
                onBackClick = {
                    navController.popBackStack()
                }
            )
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
            // In a real implementation, this would be a screen to create a new watchlist
        }
    }
}