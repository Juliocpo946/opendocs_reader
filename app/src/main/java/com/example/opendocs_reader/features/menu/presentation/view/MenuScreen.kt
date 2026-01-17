package com.example.opendocs_reader.features.menu.presentation.view

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.opendocs_reader.core.di.AppModule
import com.example.opendocs_reader.core.navigation.Screen
import com.example.opendocs_reader.features.home.presentation.view.HomeScreen
import com.example.opendocs_reader.features.recent.presentation.view.RecentScreen
import com.example.opendocs_reader.features.recent.presentation.viewmodel.RecentViewModel
import com.example.opendocs_reader.features.settings.presentation.view.SettingsScreen
import com.example.opendocs_reader.features.settings.presentation.viewmodel.SettingsViewModel
import com.example.opendocs_reader.shared.components.OpenDocsBottomBar
import com.example.opendocs_reader.shared.components.OpenDocsTopBar

@Composable
fun MenuScreen(
    rootNavController: NavController,
    settingsViewModel: SettingsViewModel
) {
    val menuNavController = rememberNavController()
    val navBackStackEntry by menuNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val context = LocalContext.current

    val recentViewModel: RecentViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return RecentViewModel(AppModule.provideDocRepository(context)) as T
            }
        }
    )

    Scaffold(
        // IMPORTANTE: Transparente para respetar la animación del Surface padre
        containerColor = Color.Transparent,
        topBar = {
            if (currentRoute == Screen.Settings.route) {
                OpenDocsTopBar(
                    title = "Configuración",
                    showActions = false
                )
            }
        },
        bottomBar = {
            OpenDocsBottomBar(navController = menuNavController)
        }
    ) { paddingValues ->
        val topPadding = if (currentRoute == Screen.Settings.route) paddingValues.calculateTopPadding() else 0.dp

        NavHost(
            navController = menuNavController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(top = topPadding, bottom = paddingValues.calculateBottomPadding())
        ) {
            composable(route = Screen.Home.route) {
                HomeScreen(rootNavController = rootNavController)
            }
            composable(route = Screen.Recent.route) {
                RecentScreen(
                    navController = rootNavController,
                    viewModel = recentViewModel
                )
            }
            composable(route = Screen.Settings.route) {
                SettingsScreen(viewModel = settingsViewModel)
            }
        }
    }
}