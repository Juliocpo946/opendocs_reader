package com.example.opendocs_reader.core.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.opendocs_reader.core.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "app_settings")

class SettingsRepositoryImpl(private val context: Context) : SettingsRepository {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
        val LANGUAGE = stringPreferencesKey("language")
    }

    override val themeMode: Flow<String> = context.settingsDataStore.data
        .map { it[Keys.THEME_MODE] ?: "system" } // Por defecto "system"

    override val keepScreenOn: Flow<Boolean> = context.settingsDataStore.data
        .map { it[Keys.KEEP_SCREEN_ON] ?: false }

    override val language: Flow<String> = context.settingsDataStore.data
        .map { it[Keys.LANGUAGE] ?: "es" }

    override suspend fun setThemeMode(mode: String) {
        context.settingsDataStore.edit { it[Keys.THEME_MODE] = mode }
    }

    override suspend fun setKeepScreenOn(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.KEEP_SCREEN_ON] = enabled }
    }

    override suspend fun setLanguage(langCode: String) {
        context.settingsDataStore.edit { it[Keys.LANGUAGE] = langCode }
    }
}