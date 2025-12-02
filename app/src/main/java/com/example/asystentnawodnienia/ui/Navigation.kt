package com.example.asystentnawodnienia.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigation(waterViewModel: WaterViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            MainScreen(navController = navController, viewModel = waterViewModel)
        }
        composable("week_summary") {
            WeekSummaryScreen(navController = navController, viewModel = waterViewModel)
        }
        composable("today_history") {
            TodayHistoryScreen(navController = navController, viewModel = waterViewModel)
        }
        composable("settings") {
            // Przekazujemy navController do ekranu ustawień
            SettingsScreen(navController = navController)
        }
    }
}
