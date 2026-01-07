package com.example.opendocs_reader.core.domain.model

data class StorageStats(
    val totalFiles: Int = 0,
    val docsUsedBytes: Long = 0L,
    val deviceTotalBytes: Long = 0L,
    val deviceFreeBytes: Long = 0L,
    val pdfCount: Int = 0,
    val wordCount: Int = 0,
    val excelCount: Int = 0,
    val pptCount: Int = 0,
    val txtCount: Int = 0,
    val favoritesCount: Int = 0,
    val isLoading: Boolean = true
)