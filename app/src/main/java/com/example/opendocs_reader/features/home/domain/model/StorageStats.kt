package com.example.opendocs_reader.features.home.domain.model

data class StorageStats(
    val totalFiles: Int = 0,
    val docsUsedBytes: Long = 0L, // Renombrado para claridad (Espacio de SOLO tus docs)

    // Nuevos campos para almacenamiento del sistema
    val deviceTotalBytes: Long = 0L,
    val deviceFreeBytes: Long = 0L,

    val pdfCount: Int = 0,
    val wordCount: Int = 0,
    val excelCount: Int = 0,
    val pptCount: Int = 0,
    val txtCount: Int = 0,
    val favoritesCount: Int = 0,
    val isLoading: Boolean = true
) {
    // Formateador universal
    fun formatSize(bytes: Long): String {
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        return when {
            gb >= 1.0 -> String.format("%.1f GB", gb)
            mb >= 1.0 -> String.format("%.1f MB", mb)
            else -> String.format("%.0f KB", kb)
        }
    }

    // Porcentaje de uso del dispositivo (0.0 a 1.0)
    fun getDeviceUsageProgress(): Float {
        if (deviceTotalBytes == 0L) return 0f
        val used = deviceTotalBytes - deviceFreeBytes
        return used.toFloat() / deviceTotalBytes.toFloat()
    }
}