package com.example.asystentnawodnienia.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigation(waterViewModel: WaterViewModel) {
    // Tworzymy kontroler nawigacji
    val navController = rememberNavController()
    // Ustawiamy mapę ekranów aplikacji

    NavHost(navController = navController, startDestination = "main") {
        // Ustawiamy trasę do ekranu głównego
        composable("main") {
            MainScreen(navController = navController, viewModel = waterViewModel)
        }
        // Ustawiamy trasę do podsumowania tygodnia
        composable("week_summary") {
            WeekSummaryScreen(navController = navController, viewModel = waterViewModel)
        }
        // Ustawiamy trasę do historii dziennej
        composable("today_history") {
            TodayHistoryScreen(navController = navController, viewModel = waterViewModel)
        }
        // Ustawiamy trasę do ustawień
        composable("settings") {
            // Przekazujemy navController do ekranu ustawień
            SettingsScreen(navController = navController)
        }
    }
}