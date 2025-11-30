package com.example.utilitymetersystem.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.compose.runtime.Composable
import com.example.utilitymetersystem.presentation.screens.*
import com.example.utilitymetersystem.presentation.viewmodels.UtilityViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: UtilityViewModel,
    startDestination: String = "registration"
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("registration") {
            RegistrationScreen(navController = navController)
        }
        composable("login") {
            RegistrationScreen(navController = navController)
        }
        composable("main") {
            MainScreen(navController = navController, viewModel = viewModel)
        }
        composable("add_reading") {
            AddReadingScreen(navController = navController, viewModel = viewModel)
        }
        composable("edit_reading") {
            EditReadingScreen(navController = navController, viewModel = viewModel)
        }
        composable("history") {
            HistoryScreen(navController = navController, viewModel = viewModel)
        }
        composable("camera") {
            CameraScreen(navController = navController, viewModel = viewModel)
        }
    }
}