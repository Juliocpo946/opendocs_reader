package com.example.opendocs_reader.core.utils

import android.annotation.SuppressLint
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
        // CORRECCIÓN: Quitamos el "* 1000" aquí porque el Repositorio ya nos entrega
        // el dato en milisegundos. Si multiplicamos de nuevo, la fecha sale mal.
        return sdf.format(Date(date))
    }
}