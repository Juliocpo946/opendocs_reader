package com.example.opendocs_reader.core.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val themeMode: Flow<String>
    val keepScreenOn: Flow<Boolean>
    val language: Flow<String>

    suspend fun setThemeMode(mode: String)
    suspend fun setKeepScreenOn(enabled: Boolean)
    suspend fun setLanguage(langCode: String)
}