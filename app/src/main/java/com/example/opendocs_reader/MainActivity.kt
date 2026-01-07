package com.example.opendocs_reader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.example.opendocs_reader.shared.theme.Opendocs_readerTheme

val LocalAnimatedSurface = compositionLocalOf { Color.Unspecified }
val LocalAnimatedOnSurface = compositionLocalOf { Color.Unspecified }
val LocalAnimatedBackground = compositionLocalOf { Color.Unspecified }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val settingsRepository = SettingsRepositoryImpl(applicationContext)

        setContent {
            val themeMode by settingsRepository.themeMode.collectAsState(initial = "system")
            val language by settingsRepository.language.collectAsState(initial = "es")

            LaunchedEffect(language) {
                LocaleUtils.setLocale(this@MainActivity, language)
            }

            val useDarkTheme = when (themeMode) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            Opendocs_readerTheme(darkTheme = useDarkTheme, dynamicColor = false) {
                // Anima los colores principales
                val animatedSurface by animateColorAsState(
                    targetValue = MaterialTheme.colorScheme.surface,
                    animationSpec = tween(durationMillis = 600),
                    label = "surface"
                )

                val animatedOnSurface by animateColorAsState(
                    targetValue = MaterialTheme.colorScheme.onSurface,
                    animationSpec = tween(durationMillis = 600),
                    label = "onSurface"
                )

                val animatedBackground by animateColorAsState(
                    targetValue = MaterialTheme.colorScheme.background,
                    animationSpec = tween(durationMillis = 600),
                    label = "background"
                )

                CompositionLocalProvider(
                    LocalAnimatedSurface provides animatedSurface,
                    LocalAnimatedOnSurface provides animatedOnSurface,
                    LocalAnimatedBackground provides animatedBackground
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = animatedBackground
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