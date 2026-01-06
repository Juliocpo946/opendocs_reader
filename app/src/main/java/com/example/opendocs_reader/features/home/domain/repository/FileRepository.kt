package com.example.opendocs_reader.features.home.domain.repository

import com.example.opendocs_reader.features.home.domain.model.StorageStats
import kotlinx.coroutines.flow.Flow

interface FileRepository {
    fun getCachedStats(): Flow<StorageStats>

    suspend fun scanAndRefreshStats()
}