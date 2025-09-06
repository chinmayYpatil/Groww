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
import com.example.groww.ui.theme.GrowwTheme
import com.example.groww.ui.watchlist.WatchlistScreen
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
                    // TODO: Navigate to stock details screen
                    navController.navigate("stock_details/$symbol")
                },
                onSearchClick = {
                    // TODO: Navigate to search screen
                    navController.navigate("search_route")
                },
                onViewAllClick = { type ->
                    // TODO: Navigate to view all screen
                    navController.navigate("view_all/$type")
                }
            )
        }

        composable("watchlist_route") {
            WatchlistScreen(
                onStockClick = { symbol ->
                    // TODO: Navigate to stock details screen
                    navController.navigate("stock_details/$symbol")
                },
                onCreateWatchlist = {
                    // TODO: Navigate to create watchlist screen
                }
            )
        }

        // TODO: Add other screen destinations
        composable("stock_details/{symbol}") { backStackEntry ->
            val symbol = backStackEntry.arguments?.getString("symbol") ?: ""
            // TODO: Implement StockDetailsScreen
            // StockDetailsScreen(symbol = symbol)
        }

        composable("search_route") {
            // TODO: Implement SearchScreen
        }

        composable("view_all/{type}") { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type") ?: ""
            // TODO: Implement ViewAllScreen
            // ViewAllScreen(type = type)
        }
    }
}