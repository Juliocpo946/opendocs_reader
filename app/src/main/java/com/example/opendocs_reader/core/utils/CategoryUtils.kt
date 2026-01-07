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

    // Lista maestra de categorías
    val categories = listOf(
        CategoryDef("Todos", "Todos", Icons.Default.Folder, AllFilesColor),
        CategoryDef("PDF", "PDF", Icons.Default.PictureAsPdf, PdfColor),
        CategoryDef("Word", "Word", Icons.Default.Description, WordColor),
        CategoryDef("Excel", "Excel", Icons.Default.TableChart, ExcelColor),
        CategoryDef("Slide", "Slide", Icons.Default.Slideshow, PptColor),
        CategoryDef("Txt", "Txt", Icons.AutoMirrored.Filled.TextSnippet, TxtColor),
        CategoryDef("Favoritos", "Favoritos", Icons.Default.Star, FavoriteColor)
    )

    // Helper para obtener el conteo dinámicamente según la categoría
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

    // Helper para obtener el color del archivo basado en su extensión
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
}