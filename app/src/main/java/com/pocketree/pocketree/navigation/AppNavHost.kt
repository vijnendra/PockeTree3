package com.pocketree.pocketree.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val repository = PockeTreeApp.instance.repository

    NavHost(
        navController = navController,
        startDestination = "timer",
        modifier = modifier
    ) {

        composable("timer") {
            // TimerViewModel is AndroidViewModel; default factory works
            val timerViewModel: TimerViewModel = viewModel()
            TimerScreen(
                navController = navController,
                viewModel = timerViewModel
            )
        }

        composable("forest") {
            // ForestViewModel requires repository; use its factory
            val forestViewModel: ForestViewModel = viewModel(
                factory = ForestViewModelFactory(repository)
            )

            ForestScreen(viewModel = forestViewModel)
        }
    }
}
