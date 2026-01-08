package com.example.opendocs_reader.core.data.repository

import android.content.Context
import android.provider.MediaStore
import androidx.core.content.edit
import com.example.opendocs_reader.core.domain.model.DocFile
import com.example.opendocs_reader.core.domain.repository.DocRepository
import com.example.opendocs_reader.core.utils.CategoryUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class DocRepositoryImpl(
    private val context: Context
) : DocRepository {

    private val prefs = context.getSharedPreferences("opendocs_recents", Context.MODE_PRIVATE)
    private val favoritesPrefs = context.getSharedPreferences("opendocs_favorites", Context.MODE_PRIVATE)
    private val memoryCache = ConcurrentHashMap<String, List<DocFile>>()

    override fun getFilesByCategory(category: String): Flow<List<DocFile>> = flow {
        if (category == "Favoritos") {
            memoryCache.remove(category)
        }

        memoryCache[category]?.let { cachedFiles ->
            if (cachedFiles.isNotEmpty()) emit(cachedFiles)
        }

        val freshFiles = queryMediaStore(category = category, query = null)
        memoryCache[category] = freshFiles
        emit(freshFiles)
    }.flowOn(Dispatchers.IO)

    override fun getRecentFiles(): Flow<List<DocFile>> = flow {
        val historyStr = prefs.getString("history_v1", "") ?: ""
        if (historyStr.isEmpty()) {
            emit(emptyList())
            return@flow
        }

        val historyItems = historyStr.split("|").filter { it.isNotEmpty() }
        val recentFiles = mutableListOf<DocFile>()
        val favoriteIds = getFavoriteIds()

        // Optimización: Consultar solo los IDs necesarios
        val idsToQuery = historyItems.mapNotNull { it.substringBefore(":").toLongOrNull() }.distinct()

        if (idsToQuery.isNotEmpty()) {
            val selection = "${MediaStore.Files.FileColumns._ID} IN (${idsToQuery.joinToString(",")})"
            val filesMap = queryMediaStoreCustom(selection, null).associateBy { it.id }

            historyItems.forEach { item ->
                val parts = item.split(":")
                if (parts.size == 2) {
                    val id = parts[0].toLongOrNull()
                    val timestamp = parts[1].toLongOrNull() ?: 0L
                    filesMap[id]?.let { file ->
                        recentFiles.add(file.copy(lastAccessed = timestamp, isFavorite = favoriteIds.contains(file.id)))
                    }
                }
            }
        }
        emit(recentFiles)
    }.flowOn(Dispatchers.IO)

    // Búsqueda Global para Home
    override suspend fun searchFiles(query: String): List<DocFile> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        return@withContext queryMediaStore(category = "Todos", query = query)
    }

    override suspend fun addToRecents(file: DocFile) {
        val historyStr = prefs.getString("history_v1", "") ?: ""
        val currentList = historyStr.split("|").filter { it.isNotEmpty() }.toMutableList()
        currentList.removeAll { it.startsWith("${file.id}:") }
        val newEntry = "${file.id}:${System.currentTimeMillis()}"
        currentList.add(0, newEntry)
        if (currentList.size > 50) currentList.removeAt(currentList.lastIndex)
        prefs.edit().putString("history_v1", currentList.joinToString("|")).apply()
    }

    override suspend fun toggleFavorite(file: DocFile) {
        val idStr = file.id.toString()
        val currentFavorites = favoritesPrefs.getStringSet("ids", emptySet()) ?: emptySet()
        val newFavorites = currentFavorites.toMutableSet()

        if (newFavorites.contains(idStr)) newFavorites.remove(idStr) else newFavorites.add(idStr)
        favoritesPrefs.edit { putStringSet("ids", newFavorites) }
        memoryCache.clear()
    }

    private fun getFavoriteIds(): Set<Long> {
        return favoritesPrefs.getStringSet("ids", emptySet())?.mapNotNull { it.toLongOrNull() }?.toSet() ?: emptySet()
    }

    // Método unificado de consulta
    private fun queryMediaStore(category: String, query: String?): List<DocFile> {
        val favoriteIds = getFavoriteIds()
        val selectionBuilder = StringBuilder()
        val selectionArgs = mutableListOf<String>()

        // Filtro por Categoría
        if (category == "Favoritos") {
            if (favoriteIds.isEmpty()) return emptyList()
            selectionBuilder.append("${MediaStore.Files.FileColumns._ID} IN (${favoriteIds.joinToString(",")})")
        } else {
            val extensions = if (category == "Todos") CategoryUtils.getAllSupportedExtensions() else CategoryUtils.getExtensionsForCategory(category)
            if (extensions.isNotEmpty()) {
                selectionBuilder.append("(")
                selectionBuilder.append(extensions.joinToString(" OR ") { "${MediaStore.Files.FileColumns.DATA} LIKE ?" })
                selectionBuilder.append(")")
                selectionArgs.addAll(extensions.map { "%.$it" })
            } else {
                selectionBuilder.append("${MediaStore.Files.FileColumns.MIME_TYPE} IS NOT NULL")
            }
        }

        // Filtro por Búsqueda (Query)
        if (!query.isNullOrBlank()) {
            if (selectionBuilder.isNotEmpty()) selectionBuilder.append(" AND ")
            selectionBuilder.append("${MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE ?")
            selectionArgs.add("%$query%")
        }

        return queryMediaStoreCustom(selectionBuilder.toString(), selectionArgs.toTypedArray(), favoriteIds)
    }

    private fun queryMediaStoreCustom(selection: String, args: Array<String>?, favoriteIds: Set<Long> = emptySet()): List<DocFile> {
        val files = mutableListOf<DocFile>()
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.MIME_TYPE
        )
        val sortOrder = "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"

        try {
            context.contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection,
                selection,
                args,
                sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val pathCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)
                val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)

                while (cursor.moveToNext()) {
                    val path = cursor.getString(pathCol)
                    if (File(path).exists()) {
                        val name = cursor.getString(nameCol)
                        val id = cursor.getLong(idCol)
                        val extension = name.substringAfterLast('.', "")

                        // Solo agregamos si es una extensión válida o soportada
                        if (CategoryUtils.getCategoryForExtension(extension) != "Otro" || favoriteIds.contains(id)) {
                            files.add(
                                DocFile(
                                    id = id,
                                    name = name,
                                    path = path,
                                    size = cursor.getLong(sizeCol),
                                    dateAdded = cursor.getLong(dateCol) * 1000,
                                    mimeType = cursor.getString(mimeCol) ?: "application/octet-stream",
                                    extension = extension,
                                    isFavorite = favoriteIds.contains(id)
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
        return files
    }
}