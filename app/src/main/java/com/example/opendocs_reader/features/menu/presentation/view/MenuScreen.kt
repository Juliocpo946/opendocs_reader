package com.example.opendocs_reader.features.menu.presentation.view

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.opendocs_reader.core.navigation.Screen
import com.example.opendocs_reader.features.home.presentation.view.HomeScreen
import com.example.opendocs_reader.features.recent.presentation.view.RecentScreen
import com.example.opendocs_reader.features.settings.presentation.view.SettingsScreen
import com.example.opendocs_reader.shared.components.OpenDocsBottomBar
import com.example.opendocs_reader.shared.components.OpenDocsTopBar

@Composable
fun MenuScreen(rootNavController: NavController) {
    val menuNavController = rememberNavController()
    val navBackStackEntry by menuNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val topBarTitle = when (currentRoute) {
        Screen.Settings.route -> "Configuración"
        Screen.Recent.route -> "Recientes"
        else -> "OpenDocs"
    }

    Scaffold(
        topBar = {
            OpenDocsTopBar(title = topBarTitle)
        },
        bottomBar = {
            OpenDocsBottomBar(navController = menuNavController)
        }
    ) { paddingValues ->
        NavHost(
            navController = menuNavController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(route = Screen.Home.route) {
                HomeScreen(rootNavController = rootNavController)
            }
            composable(route = Screen.Recent.route) {
                RecentScreen()
            }
            composable(route = Screen.Settings.route) {
                SettingsScreen()
            }
        }
    }
}