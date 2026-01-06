package com.example.opendocs_reader.features.home.data.repository

import android.content.Context
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.opendocs_reader.features.home.domain.model.StorageStats
import com.example.opendocs_reader.features.home.domain.repository.FileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File

// Extensión para crear el DataStore (Singleton simple)
private val Context.dataStore by preferencesDataStore(name = "file_stats_cache")

class FileRepositoryImpl(private val context: Context) : FileRepository {

    // Claves para el caché
    private object Keys {
        val TOTAL_FILES = intPreferencesKey("total_files")
        val USED_BYTES = longPreferencesKey("used_bytes")
        val PDF_COUNT = intPreferencesKey("pdf_count")
        val WORD_COUNT = intPreferencesKey("word_count")
        val EXCEL_COUNT = intPreferencesKey("excel_count")
        val PPT_COUNT = intPreferencesKey("ppt_count")
        val TXT_COUNT = intPreferencesKey("txt_count")
    }

    // 1. Cargar caché (Innotorio / Instantáneo)
    override fun getCachedStats(): Flow<StorageStats> {
        return context.dataStore.data.map { prefs ->
            StorageStats(
                totalFiles = prefs[Keys.TOTAL_FILES] ?: 0,
                usedSpaceBytes = prefs[Keys.USED_BYTES] ?: 0L,
                pdfCount = prefs[Keys.PDF_COUNT] ?: 0,
                wordCount = prefs[Keys.WORD_COUNT] ?: 0,
                excelCount = prefs[Keys.EXCEL_COUNT] ?: 0,
                pptCount = prefs[Keys.PPT_COUNT] ?: 0,
                txtCount = prefs[Keys.TXT_COUNT] ?: 0,
                isLoading = false // Si viene del caché, ya no está cargando visualmente
            )
        }
    }

    // 2. Escaneo Real (Pesado - Background)
    override suspend fun scanAndRefreshStats() {
        withContext(Dispatchers.IO) {
            val newStats = queryFileSystem()

            // Guardamos en caché solo si hubo cambios (DataStore maneja la diff internamente)
            context.dataStore.edit { prefs ->
                prefs[Keys.TOTAL_FILES] = newStats.totalFiles
                prefs[Keys.USED_BYTES] = newStats.usedSpaceBytes
                prefs[Keys.PDF_COUNT] = newStats.pdfCount
                prefs[Keys.WORD_COUNT] = newStats.wordCount
                prefs[Keys.EXCEL_COUNT] = newStats.excelCount
                prefs[Keys.PPT_COUNT] = newStats.pptCount
                prefs[Keys.TXT_COUNT] = newStats.txtCount
            }
        }
    }

    // Lógica pura de escaneo usando ContentResolver
    private fun queryFileSystem(): StorageStats {
        var pdf = 0; var word = 0; var excel = 0; var ppt = 0; var txt = 0
        var totalSize = 0L

        val projection = arrayOf(
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DISPLAY_NAME
        )

        // Filtramos solo documentos para no traer todo el telefono
        val selection = "${MediaStore.Files.FileColumns.MIME_TYPE} IS NOT NULL"

        try {
            context.contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection,
                selection,
                null,
                null
            )?.use { cursor ->
                val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)

                while (cursor.moveToNext()) {
                    val mime = cursor.getString(mimeCol) ?: ""
                    val size = cursor.getLong(sizeCol)
                    val name = cursor.getString(nameCol)?.lowercase() ?: ""

                    // Clasificación
                    if (mime.contains("pdf")) { pdf++; totalSize += size }
                    else if (mime.contains("word") || name.endsWith(".doc") || name.endsWith(".docx")) { word++; totalSize += size }
                    else if (mime.contains("sheet") || mime.contains("excel") || name.endsWith(".xls") || name.endsWith(".xlsx")) { excel++; totalSize += size }
                    else if (mime.contains("presentation") || mime.contains("powerpoint") || name.endsWith(".ppt") || name.endsWith(".pptx")) { ppt++; totalSize += size }
                    else if (mime.contains("text") || name.endsWith(".txt")) { txt++; totalSize += size }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return StorageStats(
            totalFiles = pdf + word + excel + ppt + txt,
            usedSpaceBytes = totalSize,
            pdfCount = pdf,
            wordCount = word,
            excelCount = excel,
            pptCount = ppt,
            txtCount = txt,
            isLoading = false
        )
    }
}