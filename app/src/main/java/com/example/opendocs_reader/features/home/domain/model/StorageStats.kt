package com.example.opendocs_reader.features.home.domain.model

data class StorageStats(
    val totalFiles: Int = 0,
    val usedSpaceBytes: Long = 0L,
    val pdfCount: Int = 0,
    val wordCount: Int = 0,
    val excelCount: Int = 0,
    val pptCount: Int = 0,
    val txtCount: Int = 0,
    val favoritesCount: Int = 0,
    val isLoading: Boolean = true
) {
    // Formatea los bytes a GB/MB para la UI
    fun getFormattedSize(): String {
        val kb = usedSpaceBytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        return when {
            gb >= 1.0 -> String.format("%.2f GB", gb)
            mb >= 1.0 -> String.format("%.2f MB", mb)
            else -> String.format("%.2f KB", kb)
        }
    }
}