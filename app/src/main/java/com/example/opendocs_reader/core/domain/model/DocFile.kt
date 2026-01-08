package com.example.opendocs_reader.core.domain.model

data class DocFile(
    val id: Long,
    val name: String,
    val path: String,
    val size: Long,
    val dateAdded: Long,
    val mimeType: String,
    val extension: String,
    val lastAccessed: Long = 0,
    val isFavorite: Boolean = false
)