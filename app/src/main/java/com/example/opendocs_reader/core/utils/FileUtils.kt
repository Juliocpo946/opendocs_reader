package com.example.opendocs_reader.core.utils

import android.annotation.SuppressLint
import androidx.compose.ui.graphics.Color
import com.example.opendocs_reader.shared.theme.*
import java.text.SimpleDateFormat
import java.util.*

object FileUtils {
    @SuppressLint("DefaultLocale")
    fun formatSize(size: Long): String {
        val kb = size / 1024
        return if (kb > 1024) {
            val mb = kb / 1024f
            String.format("%.1f MB", mb)
        } else {
            "$kb KB"
        }
    }

    fun formatDate(date: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        // Multiplicamos por 1000 porque MediaStore entrega segundos, Java Date usa milisegundos
        return sdf.format(Date(date * 1000))
    }
}