package com.example.opendocs_reader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.opendocs_reader.core.navigation.Screen
import com.example.opendocs_reader.features.menu.presentation.view.MenuScreen
import com.example.opendocs_reader.features.splash.presentation.view.SplashScreen
import com.example.opendocs_reader.shared.theme.Opendocs_readerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Opendocs_readerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = Screen.Splash.route
                    ) {
                        composable(route = Screen.Splash.route) {
                            SplashScreen(navController = navController)
                        }
                        composable(route = Screen.Menu.route) {
                            MenuScreen()
                        }
                    }
                }
            }
        }
    }
}