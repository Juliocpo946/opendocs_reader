package com.example.opendocs_reader.core.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash_screen")
    data object Menu : Screen("menu_screen")
}