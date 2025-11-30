package com.example.utilitymetersystem.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.utilitymetersystem.presentation.screens.AddReadingScreen
import com.example.utilitymetersystem.presentation.screens.MainScreen
import com.example.utilitymetersystem.presentation.viewmodels.UtilityViewModel

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController() // Дефолтное значение
) {
    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        composable("main") {
            // Каждый экран получает свой экземпляр ViewModel
            val viewModel: UtilityViewModel = hiltViewModel()
            MainScreen(
                navController = navController,
                viewModel = viewModel
            )
        }
        composable("add") {
            // Отдельный экземпляр ViewModel для этого экрана
            val viewModel: UtilityViewModel = hiltViewModel()
            AddReadingScreen(
                navController = navController,
                viewModel = viewModel
            )
        }
    }
}