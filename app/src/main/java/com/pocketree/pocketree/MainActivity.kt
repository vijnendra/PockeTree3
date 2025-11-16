package com.pocketree.pocketree

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.pocketree.pocketree.navigation.AppNavHost
import com.pocketree.pocketree.ui.theme.PocketreeTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PocketreeTheme {
                val navController = rememberNavController()
                AppNavHost(navController = navController)
            }
        }
    }
}
