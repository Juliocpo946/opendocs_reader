package com.example.opendocs_reader.core.domain.repository

import com.example.opendocs_reader.core.domain.model.StorageStats
import kotlinx.coroutines.flow.Flow

interface StatsRepository {
    fun getCachedStats(): Flow<StorageStats>
    suspend fun scanAndRefreshStats()
}