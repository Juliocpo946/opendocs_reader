package com.example.opendocs_reader.core.navigation

sealed class Screen(val route: String) {
    // Rutas Principales
    data object Splash : Screen("splash_screen")
    data object Menu : Screen("menu_screen")
    data object Files : Screen("files_screen/{category}") {
        fun createRoute(category: String) = "files_screen/$category"
    }
    // Rutas Internas (BottomBar)
    data object Home : Screen("home_screen")
    data object Recent : Screen("recent_screen")
    data object Settings : Screen("settings_screen")
}