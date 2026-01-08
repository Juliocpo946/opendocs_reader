package com.example.opendocs_reader.features.settings.presentation.viewmodel

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.AndroidViewModel
import com.example.opendocs_reader.features.settings.domain.model.AppLanguage
import com.example.opendocs_reader.features.settings.domain.model.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    private val _currentTheme = MutableStateFlow(AppTheme.SYSTEM)
    val currentTheme: StateFlow<AppTheme> = _currentTheme.asStateFlow()

    private val _currentLanguage = MutableStateFlow(AppLanguage.SYSTEM)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        val themeName = prefs.getString("theme", AppTheme.SYSTEM.name) ?: AppTheme.SYSTEM.name
        _currentTheme.value = try { AppTheme.valueOf(themeName) } catch (e: Exception) { AppTheme.SYSTEM }

        val langCode = prefs.getString("language", AppLanguage.SYSTEM.code) ?: AppLanguage.SYSTEM.code
        // CAMBIO: values() -> entries
        _currentLanguage.value = AppLanguage.entries.find { it.code == langCode } ?: AppLanguage.SYSTEM
    }

    fun setTheme(theme: AppTheme) {
        _currentTheme.value = theme
        prefs.edit().putString("theme", theme.name).apply()

        val mode = when (theme) {
            AppTheme.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            AppTheme.DARK -> AppCompatDelegate.MODE_NIGHT_YES
            AppTheme.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    fun setLanguage(language: AppLanguage) {
        _currentLanguage.value = language
        prefs.edit().putString("language", language.code).apply()

        val localeList = if (language == AppLanguage.SYSTEM) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(language.code)
        }
        AppCompatDelegate.setApplicationLocales(localeList)
    }
}