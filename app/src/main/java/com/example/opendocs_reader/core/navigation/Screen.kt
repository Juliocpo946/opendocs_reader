package com.example.opendocs_reader.core.navigation

import android.net.Uri

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

    data object PdfViewer : Screen("pdf_viewer_screen/{fileUri}/{fileName}") {
        fun createRoute(uri: String, name: String) = "pdf_viewer_screen/${Uri.encode(uri)}/$name"
    }
}