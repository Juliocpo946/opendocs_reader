package com.example.opendocs_reader.features.home.data.repository

import android.content.Context
import android.os.Environment
import android.os.StatFs
import android.provider.MediaStore
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

private val Context.dataStore by preferencesDataStore(name = "file_stats_cache")

class FileRepositoryImpl(private val context: Context) : FileRepository {

    private object Keys {
        val TOTAL_FILES = intPreferencesKey("total_files")
        val DOCS_BYTES = longPreferencesKey("docs_bytes")
        val PDF_COUNT = intPreferencesKey("pdf_count")
        val WORD_COUNT = intPreferencesKey("word_count")
        val EXCEL_COUNT = intPreferencesKey("excel_count")
        val PPT_COUNT = intPreferencesKey("ppt_count")
        val TXT_COUNT = intPreferencesKey("txt_count")
    }

    override fun getCachedStats(): Flow<StorageStats> {
        return context.dataStore.data.map { prefs ->
            val (total, free) = getDeviceStorageInfo()

            StorageStats(
                totalFiles = prefs[Keys.TOTAL_FILES] ?: 0,
                docsUsedBytes = prefs[Keys.DOCS_BYTES] ?: 0L,
                deviceTotalBytes = total,
                deviceFreeBytes = free,
                pdfCount = prefs[Keys.PDF_COUNT] ?: 0,
                wordCount = prefs[Keys.WORD_COUNT] ?: 0,
                excelCount = prefs[Keys.EXCEL_COUNT] ?: 0,
                pptCount = prefs[Keys.PPT_COUNT] ?: 0,
                txtCount = prefs[Keys.TXT_COUNT] ?: 0,
                isLoading = false
            )
        }
    }

    override suspend fun scanAndRefreshStats() {
        withContext(Dispatchers.IO) {
            val scanResult = queryFileSystem()

            context.dataStore.edit { prefs ->
                prefs[Keys.TOTAL_FILES] = scanResult.totalFiles
                prefs[Keys.DOCS_BYTES] = scanResult.docsUsedBytes
                prefs[Keys.PDF_COUNT] = scanResult.pdfCount
                prefs[Keys.WORD_COUNT] = scanResult.wordCount
                prefs[Keys.EXCEL_COUNT] = scanResult.excelCount
                prefs[Keys.PPT_COUNT] = scanResult.pptCount
                prefs[Keys.TXT_COUNT] = scanResult.txtCount
            }
        }
    }

    private fun queryFileSystem(): StorageStats {
        var pdf = 0; var word = 0; var excel = 0; var ppt = 0; var txt = 0
        var docsSize = 0L

        val (deviceTotal, deviceFree) = getDeviceStorageInfo()

        val projection = arrayOf(
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DISPLAY_NAME
        )
        val selection = "${MediaStore.Files.FileColumns.MIME_TYPE} IS NOT NULL"

        try {
            context.contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection, selection, null, null
            )?.use { cursor ->
                val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)

                while (cursor.moveToNext()) {
                    val mime = cursor.getString(mimeCol) ?: ""
                    val size = cursor.getLong(sizeCol)
                    val name = cursor.getString(nameCol)?.lowercase() ?: ""

                    if (mime.contains("pdf")) { pdf++; docsSize += size }
                    else if (mime.contains("word") || name.endsWith(".doc") || name.endsWith(".docx")) { word++; docsSize += size }
                    else if (mime.contains("sheet") || mime.contains("excel") || name.endsWith(".xls") || name.endsWith(".xlsx")) { excel++; docsSize += size }
                    else if (mime.contains("presentation") || mime.contains("powerpoint") || name.endsWith(".ppt") || name.endsWith(".pptx")) { ppt++; docsSize += size }
                    else if (mime.contains("text") || name.endsWith(".txt")) { txt++; docsSize += size }
                }
            }
        } catch (e: Exception) { e.printStackTrace() }

        return StorageStats(
            totalFiles = pdf + word + excel + ppt + txt,
            docsUsedBytes = docsSize,
            deviceTotalBytes = deviceTotal,
            deviceFreeBytes = deviceFree,
            pdfCount = pdf, wordCount = word, excelCount = excel, pptCount = ppt, txtCount = txt,
            isLoading = false
        )
    }

    private fun getDeviceStorageInfo(): Pair<Long, Long> {
        return try {
            val path = Environment.getDataDirectory()
            val stat = StatFs(path.path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            val availableBlocks = stat.availableBlocksLong

            val total = totalBlocks * blockSize
            val free = availableBlocks * blockSize
            Pair(total, free)
        } catch (e: Exception) {
            Pair(0L, 0L)
        }
    }
}