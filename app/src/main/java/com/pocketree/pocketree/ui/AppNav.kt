package com.pocketree.pocketree.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.pocketree.pocketree.PockeTreeApp
import com.pocketree.pocketree.ui.forest.ForestScreen
import com.pocketree.pocketree.ui.forest.ForestViewModel
import com.pocketree.pocketree.ui.forest.ForestViewModelFactory
import com.pocketree.pocketree.ui.timer.TimerScreen
import com.pocketree.pocketree.ui.timer.TimerViewModel

@Composable
fun AppNav(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "timer") {
        composable("timer") {
            val timerViewModel: TimerViewModel = viewModel()
            TimerScreen(navController = navController, viewModel = timerViewModel)
        }
        composable("forest") {
            val repository = PockeTreeApp.instance.treeSessionRepository
            val forestViewModel: ForestViewModel = viewModel(
                factory = ForestViewModelFactory(repository)
            )
            ForestScreen(viewModel = forestViewModel)
        }
    }
}
