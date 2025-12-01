package com.example.utilitymetersystem.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.utilitymetersystem.presentation.screens.AddReadingScreen
import com.example.utilitymetersystem.presentation.screens.CameraScreen
import com.example.utilitymetersystem.presentation.screens.HistoryScreen
import com.example.utilitymetersystem.presentation.screens.MainScreen
import com.example.utilitymetersystem.presentation.screens.StatisticsScreen
import com.example.utilitymetersystem.presentation.viewmodels.UtilityViewModel

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        composable("main") {
            val viewModel: UtilityViewModel = hiltViewModel()
            MainScreen(navController = navController, viewModel = viewModel)
        }
        composable("add") {
            val viewModel: UtilityViewModel = hiltViewModel()
            AddReadingScreen(navController = navController, viewModel = viewModel)
        }
        composable("camera") {
            val cameraViewModel: UtilityViewModel = hiltViewModel()
            CameraScreen(navController, cameraViewModel)
        }
        // Добавьте новые маршруты в NavGraph
        composable("history") {
            HistoryScreen(navController, viewModel())
        }
        composable("statistics") {
            StatisticsScreen(navController, viewModel())
        }
    }
}