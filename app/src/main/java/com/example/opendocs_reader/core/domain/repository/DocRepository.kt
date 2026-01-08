package com.example.opendocs_reader.core.domain.repository

import com.example.opendocs_reader.core.domain.model.DocFile
import kotlinx.coroutines.flow.Flow

interface DocRepository {
    fun getFilesByCategory(category: String): Flow<List<DocFile>>
    fun getRecentFiles(): Flow<List<DocFile>>
    suspend fun addToRecents(file: DocFile)
}