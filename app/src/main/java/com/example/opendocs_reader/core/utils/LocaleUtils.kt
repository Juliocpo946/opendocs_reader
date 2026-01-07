package com.example.opendocs_reader.core.utils

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

object LocaleUtils {
    fun setLocale(context: Context, languageCode: String) {
        // --- CORRECCIÓN: Validar antes de aplicar ---
        // Obtenemos el idioma actual de la app
        val currentLocales = AppCompatDelegate.getApplicationLocales()
        // Creamos el idioma que queremos poner
        val requestedLocales = LocaleListCompat.forLanguageTags(languageCode)

        // Si son iguales, NO hacemos nada. Esto evita que la app se reinicie
        // cuando cambias el tema u otra cosa que no sea el idioma.
        if (currentLocales == requestedLocales) {
            return
        }

        // Si son diferentes, entonces sí aplicamos el cambio
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.getSystemService(LocaleManager::class.java)
                .applicationLocales = LocaleList.forLanguageTags(languageCode)
        } else {
            AppCompatDelegate.setApplicationLocales(requestedLocales)
        }
    }

    fun getLanguageName(code: String): String {
        return when(code) {
            "es" -> "Español"
            "en" -> "English"
            "fr" -> "Français"
            else -> "Español"
        }
    }
}