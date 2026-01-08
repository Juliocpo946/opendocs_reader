package com.example.opendocs_reader.core.data.repository

import android.content.Context
import android.provider.MediaStore
import com.example.opendocs_reader.core.domain.model.DocFile
import com.example.opendocs_reader.core.domain.repository.DocRepository
import com.example.opendocs_reader.core.utils.CategoryUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class DocRepositoryImpl(
    private val context: Context
) : DocRepository {

    private val prefs = context.getSharedPreferences("opendocs_recents", Context.MODE_PRIVATE)
    private val memoryCache = ConcurrentHashMap<String, List<DocFile>>()

    override fun getFilesByCategory(category: String): Flow<List<DocFile>> = flow {
        memoryCache[category]?.let { cachedFiles ->
            if (cachedFiles.isNotEmpty()) {
                emit(cachedFiles)
            }
        }

        val freshFiles = queryMediaStore(category)

        memoryCache[category] = freshFiles
        emit(freshFiles)

    }.flowOn(Dispatchers.IO)

    private fun queryMediaStore(category: String): List<DocFile> {
        val files = mutableListOf<DocFile>()
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.MIME_TYPE
        )

        val extensionsToQuery = if (category == "Todos") {
            CategoryUtils.getAllSupportedExtensions()
        } else {
            CategoryUtils.getExtensionsForCategory(category)
        }

        val selection = if (extensionsToQuery.isNotEmpty()) {
            extensionsToQuery.joinToString(" OR ") { "${MediaStore.Files.FileColumns.DATA} LIKE ?" }
        } else {
            "${MediaStore.Files.FileColumns.MIME_TYPE} IS NOT NULL"
        }

        val selectionArgs = extensionsToQuery.map { "%.$it" }.toTypedArray()
        val sortOrder = "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"

        try {
            context.contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection,
                "($selection)",
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)
                val mimeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)

                while (cursor.moveToNext()) {
                    val path = cursor.getString(pathColumn)
                    val file = File(path)
                    if (file.exists()) {
                        val name = cursor.getString(nameColumn)
                        val extension = name.substringAfterLast('.', "")
                        val determinedCategory = CategoryUtils.getCategoryForExtension(extension)

                        if (determinedCategory != "Otro") {
                            files.add(
                                DocFile(
                                    id = cursor.getLong(idColumn),
                                    name = name,
                                    path = path,
                                    size = cursor.getLong(sizeColumn),
                                    dateAdded = cursor.getLong(dateColumn) * 1000,
                                    mimeType = cursor.getString(mimeColumn) ?: "application/octet-stream",
                                    extension = extension
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return files
    }

    override fun getRecentFiles(): Flow<List<DocFile>> = flow {
        val historyStr = prefs.getString("history_v1", "") ?: ""
        if (historyStr.isEmpty()) {
            emit(emptyList())
            return@flow
        }
        val historyMap = historyStr.split("|")
            .filter { it.isNotEmpty() }
            .associate {
                val parts = it.split(":")
                val id = parts.getOrNull(0)?.toLongOrNull() ?: 0L
                val time = parts.getOrNull(1)?.toLongOrNull() ?: 0L
                id to time
            }
        if (historyMap.isEmpty()) {
            emit(emptyList())
            return@flow
        }

        val files = mutableListOf<DocFile>()
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.MIME_TYPE
        )
        val idList = historyMap.keys.joinToString(",")
        val selection = "${MediaStore.Files.FileColumns._ID} IN ($idList)"

        try {
            context.contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection,
                selection,
                null,
                null
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)
                val mimeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val path = cursor.getString(pathColumn)
                    val file = File(path)
                    if (file.exists() && historyMap.containsKey(id)) {
                        val name = cursor.getString(nameColumn)
                        files.add(
                            DocFile(
                                id = id,
                                name = name,
                                path = path,
                                size = cursor.getLong(sizeColumn),
                                dateAdded = cursor.getLong(dateColumn) * 1000,
                                mimeType = cursor.getString(mimeColumn) ?: "application/octet-stream",
                                extension = name.substringAfterLast('.', ""),
                                lastAccessed = historyMap[id] ?: 0L
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        emit(files.sortedByDescending { it.lastAccessed })
    }.flowOn(Dispatchers.IO)

    override suspend fun addToRecents(file: DocFile) {
        val historyStr = prefs.getString("history_v1", "") ?: ""
        val currentList = historyStr.split("|")
            .filter { it.isNotEmpty() }
            .toMutableList()
        currentList.removeAll { it.startsWith("${file.id}:") }
        val newEntry = "${file.id}:${System.currentTimeMillis()}"
        currentList.add(0, newEntry)
        if (currentList.size > 50) currentList.removeAt(currentList.lastIndex)
        prefs.edit().putString("history_v1", currentList.joinToString("|")).apply()
    }
}