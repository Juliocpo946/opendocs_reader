package com.example.opendocs_reader.core.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TextSnippet
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.opendocs_reader.core.domain.model.StorageStats
import com.example.opendocs_reader.shared.theme.*

data class CategoryDef(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val color: Color
)

object CategoryUtils {

    val categories = listOf(
        CategoryDef("Todos", "Todos", Icons.Default.Folder, AllFilesColor),
        CategoryDef("PDF", "PDF", Icons.Default.PictureAsPdf, PdfColor),
        CategoryDef("Word", "Word", Icons.Default.Description, WordColor),
        CategoryDef("Excel", "Excel", Icons.Default.TableChart, ExcelColor),
        CategoryDef("Slide", "Slide", Icons.Default.Slideshow, PptColor),
        CategoryDef("Txt", "Txt", Icons.AutoMirrored.Filled.TextSnippet, TxtColor),
        CategoryDef("Favoritos", "Favoritos", Icons.Default.Star, FavoriteColor)
    )

    fun getCountForCategory(stats: StorageStats, categoryId: String): Int {
        return when (categoryId) {
            "Todos" -> stats.totalFiles
            "PDF" -> stats.pdfCount
            "Word" -> stats.wordCount
            "Excel" -> stats.excelCount
            "Slide" -> stats.pptCount
            "Txt" -> stats.txtCount
            "Favoritos" -> stats.favoritesCount
            else -> 0
        }
    }

    fun getFileColor(ext: String): Color {
        return when {
            ext.contains("pdf", true) -> PdfColor
            ext.contains("doc", true) -> WordColor
            ext.contains("xls", true) -> ExcelColor
            ext.contains("ppt", true) -> PptColor
            ext.contains("txt", true) -> TxtColor
            else -> Color.Gray
        }
    }

    fun getExtensionsForCategory(category: String): List<String> {
        return when (category) {
            "PDF" -> listOf("pdf")
            "Word" -> listOf("doc", "docx")
            "Excel" -> listOf("xls", "xlsx")
            "Slide" -> listOf("ppt", "pptx")
            "Txt" -> listOf("txt", "log", "xml", "csv")
            else -> emptyList()
        }
    }

    fun getCategoryForExtension(extension: String): String {
        val ext = extension.lowercase()
        return when {
            ext == "pdf" -> "PDF"
            ext.startsWith("doc") -> "Word"
            ext.startsWith("xls") -> "Excel"
            ext.startsWith("ppt") -> "Slide"
            ext == "txt" || ext == "log" || ext == "xml" || ext == "csv" -> "Txt"
            else -> "Otro"
        }
    }

    fun getAllSupportedExtensions(): List<String> {
        return getExtensionsForCategory("PDF") +
                getExtensionsForCategory("Word") +
                getExtensionsForCategory("Excel") +
                getExtensionsForCategory("Slide") +
                getExtensionsForCategory("Txt")
    }
}