package com.example.opendocs_reader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.opendocs_reader.core.data.repository.SettingsRepositoryImpl
import com.example.opendocs_reader.core.navigation.Screen
import com.example.opendocs_reader.core.utils.LocaleUtils
import com.example.opendocs_reader.features.files.presentation.view.FilesScreen
import com.example.opendocs_reader.features.menu.presentation.view.MenuScreen
import com.example.opendocs_reader.features.splash.presentation.view.SplashScreen
import com.example.opendocs_reader.shared.theme.OpenDocsReaderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val settingsRepository = SettingsRepositoryImpl(applicationContext)

        setContent {
            val themeMode by settingsRepository.themeMode.collectAsState(initial = "system")
            val language by settingsRepository.language.collectAsState(initial = "es")

            // El LaunchedEffect vigila cambios en 'language'.
            // Gracias a la corrección en LocaleUtils, si 'language' es el mismo,
            // no habrá reinicio aunque esto se ejecute.
            LaunchedEffect(language) {
                LocaleUtils.setLocale(this@MainActivity, language)
            }

            val useDarkTheme = when (themeMode) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            OpenDocsReaderTheme(darkTheme = useDarkTheme) {
                Crossfade(
                    targetState = useDarkTheme,
                    animationSpec = tween(500),
                    label = "themeAnimation"
                ) { _ ->
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val mainNavController = rememberNavController()

                        NavHost(
                            navController = mainNavController,
                            startDestination = Screen.Splash.route
                        ) {
                            composable(route = Screen.Splash.route) {
                                SplashScreen(navController = mainNavController)
                            }
                            composable(route = Screen.Menu.route) {
                                MenuScreen(rootNavController = mainNavController)
                            }
                            composable(
                                route = Screen.Files.route,
                                arguments = listOf(navArgument("category") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val category = backStackEntry.arguments?.getString("category") ?: "Todos"
                                FilesScreen(navController = mainNavController, initialCategory = category)
                            }
                        }
                    }
                }
            }
        }
    }
}