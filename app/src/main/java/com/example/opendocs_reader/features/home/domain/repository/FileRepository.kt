package com.example.opendocs_reader.features.home.domain.repository

import com.example.opendocs_reader.features.home.domain.model.StorageStats
import kotlinx.coroutines.flow.Flow

interface FileRepository {
    // Obtiene los datos cacheados (carga instantánea)
    fun getCachedStats(): Flow<StorageStats>

    // Escanea el dispositivo en segundo plano y actualiza si hay cambios
    suspend fun scanAndRefreshStats()
}