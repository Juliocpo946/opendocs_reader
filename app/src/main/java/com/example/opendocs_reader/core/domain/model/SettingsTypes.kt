package com.example.opendocs_reader.features.settings.domain.model

enum class AppTheme(val label: String) {
    SYSTEM("Sistema"),
    LIGHT("Claro"),
    DARK("Oscuro")
}

enum class AppLanguage(val code: String, val label: String) {
    SYSTEM("system", "Sistema"),
    ENGLISH("en", "English"),
    SPANISH("es", "Español"),
    FRENCH("fr", "Français"),
    PORTUGUESE("pt", "Português")
}