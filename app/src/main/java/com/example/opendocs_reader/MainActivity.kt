package com.example.opendocs_reader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.opendocs_reader.core.navigation.Screen
import com.example.opendocs_reader.core.utils.LocaleUtils
import com.example.opendocs_reader.features.files.presentation.view.FilesScreen
import com.example.opendocs_reader.features.menu.presentation.view.MenuScreen
import com.example.opendocs_reader.features.pdf.presentation.view.PdfViewerScreen //
import com.example.opendocs_reader.features.settings.domain.model.AppLanguage
import com.example.opendocs_reader.features.settings.domain.model.AppTheme
import com.example.opendocs_reader.features.settings.presentation.viewmodel.SettingsViewModel
import com.example.opendocs_reader.features.splash.presentation.view.SplashScreen
import com.example.opendocs_reader.shared.theme.Opendocs_readerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val themeMode by settingsViewModel.currentTheme.collectAsState()
            val language by settingsViewModel.currentLanguage.collectAsState()

            LaunchedEffect(language) {
                val code = if (language == AppLanguage.SYSTEM) "system" else language.code
                LocaleUtils.setLocale(this@MainActivity, code)
            }

            val useDarkTheme = when (themeMode) {
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
                else -> isSystemInDarkTheme()
            }

            // Ya no pasamos animaciones manuales, el tema lo hace todo internamente
            Opendocs_readerTheme(darkTheme = useDarkTheme, dynamicColor = false) {
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
                            MenuScreen(
                                rootNavController = mainNavController,
                                settingsViewModel = settingsViewModel
                            )
                        }
                        composable(
                            route = Screen.Files.route,
                            arguments = listOf(navArgument("category") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val category = backStackEntry.arguments?.getString("category") ?: "Todos"
                            FilesScreen(navController = mainNavController, initialCategory = category)
                        }

                        // --- NUEVA RUTA PARA PDF ---
                        composable(
                            route = Screen.PdfViewer.route,
                            arguments = listOf(
                                navArgument("fileUri") { type = NavType.StringType },
                                navArgument("fileName") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val fileUri = backStackEntry.arguments?.getString("fileUri") ?: ""
                            val fileName = backStackEntry.arguments?.getString("fileName") ?: "Documento"

                            PdfViewerScreen(
                                navController = mainNavController,
                                fileUri = fileUri,
                                fileName = fileName
                            )
                        }
                    }
                }
            }
        }
    }
}