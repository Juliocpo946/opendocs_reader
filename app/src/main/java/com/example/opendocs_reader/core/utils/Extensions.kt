package com.example.opendocs_reader.core.utils

import android.annotation.SuppressLint
import com.example.opendocs_reader.core.domain.model.StorageStats

@SuppressLint("DefaultLocale")
fun Long.formatSize(): String {
    val kb = this / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
        gb >= 1.0 -> String.format("%.1f GB", gb)
        mb >= 1.0 -> String.format("%.1f MB", mb)
        else -> String.format("%.0f KB", kb)
    }
}

fun StorageStats.getDeviceUsageProgress(): Float {
    if (deviceTotalBytes == 0L) return 0f
    val used = deviceTotalBytes - deviceFreeBytes
    return used.toFloat() / deviceTotalBytes.toFloat()
}