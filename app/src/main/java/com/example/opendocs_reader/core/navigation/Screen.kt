package com.example.opendocs_reader.core.navigation

import android.net.Uri

sealed class Screen(val route: String) {
    data object Splash : Screen("splash_screen")
    data object Menu : Screen("menu_screen")
    data object Files : Screen("files_screen/{category}") {
        fun createRoute(category: String) = "files_screen/$category"
    }
    data object Home : Screen("home_screen")
    data object Recent : Screen("recent_screen")
    data object Settings : Screen("settings_screen")

    // Actualizamos la ruta para recibir todos los datos del archivo
    data object PdfViewer : Screen("pdf_viewer_screen/{fileUri}/{fileName}/{fileId}/{fileMime}/{fileSize}/{fileDate}") {
        fun createRoute(uri: String, name: String, id: Long, mime: String, size: Long, date: Long): String {
            val encodedUri = Uri.encode(uri)
            val safeMime = Uri.encode(mime) // Por seguridad si tiene caracteres raros
            return "pdf_viewer_screen/$encodedUri/$name/$id/$safeMime/$size/$date"
        }
    }
}