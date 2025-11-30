package com.example.utilitymetersystem.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.utilitymetersystem.presentation.screens.AddReadingScreen
import com.example.utilitymetersystem.presentation.screens.MainScreen
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
    }
}