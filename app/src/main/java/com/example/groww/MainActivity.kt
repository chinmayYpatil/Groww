package com.example.groww

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.groww.ui.explore.ExploreScreen
import com.example.groww.ui.navigation.GrowwBottomNavigation
import com.example.groww.ui.stockdetails.StockDetailsScreen
import com.example.groww.ui.theme.GrowwTheme
import com.example.groww.ui.viewall.ViewAllScreen
import com.example.groww.ui.watchlist.WatchlistScreen
import com.example.groww.ui.watchlist.WatchlistDetailScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GrowwTheme {
                GrowwApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrowwApp() {
    val navController = rememberNavController()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            bottomBar = {
                GrowwBottomNavigation(navController = navController)
            }
        ) { innerPadding ->
            AppNavHost(
                navController = navController,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "explore_route",
        modifier = modifier
    ) {
        composable("explore_route") {
            ExploreScreen(
                onStockClick = { symbol ->
                    navController.navigate("stock_details/$symbol")
                },
                onSearchClick = {
                    // TODO: Navigate to search screen
                    navController.navigate("search_route")
                },
                onViewAllClick = { type ->
                    navController.navigate("view_all/$type")
                }
            )
        }

        composable("watchlist_route") {
            WatchlistScreen(
                onWatchlistClick = { watchlistId, watchlistName ->
                    // Navigate to watchlist detail screen
                    navController.navigate("watchlist_detail/$watchlistId/$watchlistName")
                },
                onCreateWatchlist = {
                    // TODO: Navigate to create watchlist screen
                    navController.navigate("create_watchlist")
                },
                onExploreStocks = {
                    // Navigate to explore tab when user wants to find stocks
                    navController.navigate("explore_route") {
                        popUpTo("watchlist_route") { inclusive = false }
                    }
                }
            )
        }

        // Watchlist Detail Screen
        composable("watchlist_detail/{watchlistId}/{watchlistName}") { backStackEntry ->
            val watchlistId = backStackEntry.arguments?.getString("watchlistId")?.toLongOrNull() ?: 0L
            val watchlistName = backStackEntry.arguments?.getString("watchlistName") ?: ""
            WatchlistDetailScreen(
                watchlistId = watchlistId,
                watchlistName = watchlistName,
                onBackClick = { navController.popBackStack() },
                onStockClick = { symbol ->
                    navController.navigate("stock_details/$symbol")
                }
            )
        }

        // Stock Details Screen
        composable("stock_details/{symbol}") { backStackEntry ->
            val symbol = backStackEntry.arguments?.getString("symbol") ?: ""
            StockDetailsScreen(
                symbol = symbol,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("search_route") {
            // TODO: Implement SearchScreen
        }

        composable("view_all/{type}") { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type") ?: ""
            ViewAllScreen(
                type = type,
                onBackClick = { navController.popBackStack() },
                onStockClick = { symbol ->
                    navController.navigate("stock_details/$symbol")
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