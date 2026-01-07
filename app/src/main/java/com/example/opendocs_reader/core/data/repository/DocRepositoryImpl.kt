package com.example.opendocs_reader.core.data.repository

import android.content.Context
import android.provider.MediaStore
import com.example.opendocs_reader.core.domain.model.DocFile
import com.example.opendocs_reader.core.domain.repository.DocRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class DocRepositoryImpl(private val context: Context) : DocRepository {

    override fun getFilesByCategory(category: String): Flow<List<DocFile>> = flow {
        val files = mutableListOf<DocFile>()

        // Snippets SQL
        val pdf = "${MediaStore.Files.FileColumns.MIME_TYPE} LIKE '%pdf%'"
        val word = "(${MediaStore.Files.FileColumns.MIME_TYPE} LIKE '%word%' OR ${MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE '%.doc' OR ${MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE '%.docx')"
        val excel = "(${MediaStore.Files.FileColumns.MIME_TYPE} LIKE '%sheet%' OR ${MediaStore.Files.FileColumns.MIME_TYPE} LIKE '%excel%' OR ${MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE '%.xls' OR ${MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE '%.xlsx')"
        val ppt = "(${MediaStore.Files.FileColumns.MIME_TYPE} LIKE '%presentation%' OR ${MediaStore.Files.FileColumns.MIME_TYPE} LIKE '%powerpoint%' OR ${MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE '%.ppt' OR ${MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE '%.pptx')"
        val txt = "(${MediaStore.Files.FileColumns.MIME_TYPE} LIKE '%text%' OR ${MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE '%.txt')"

        val selection = when (category.lowercase()) {
            "todos" -> "($pdf OR $word OR $excel OR $ppt OR $txt)"
            "pdf" -> pdf
            "word" -> word
            "excel" -> excel
            "slide", "ppt" -> ppt
            "txt" -> txt
            else -> "($pdf OR $word OR $excel OR $ppt OR $txt)"
        }

        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.MIME_TYPE
        )

        try {
            context.contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection, selection, null,
                "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val pathCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)
                val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)

                while (cursor.moveToNext()) {
                    files.add(
                        DocFile(
                            id = cursor.getLong(idCol),
                            name = cursor.getString(nameCol) ?: "",
                            path = cursor.getString(pathCol) ?: "",
                            size = cursor.getLong(sizeCol),
                            dateAdded = cursor.getLong(dateCol),
                            mimeType = cursor.getString(mimeCol) ?: "",
                            extension = (cursor.getString(nameCol) ?: "").substringAfterLast('.', "")
                        )
                    )
                }
            }
        } catch (e: Exception) { e.printStackTrace() }

        emit(files)
    }.flowOn(Dispatchers.IO)
}