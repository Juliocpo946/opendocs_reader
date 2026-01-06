package com.example.opendocs_reader.features.home.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.opendocs_reader.features.home.domain.model.StorageStats

@Composable
fun DocCategoryGrid(stats: StorageStats, onCategoryClick: (String) -> Unit) {
    val categories = listOf(
        CategoryUiModel("PDF", Icons.Default.PictureAsPdf, Color(0xFFE57373), stats.pdfCount),
        CategoryUiModel("Word", Icons.Default.Description, Color(0xFF64B5F6), stats.wordCount),
        CategoryUiModel("Excel", Icons.Default.TableChart, Color(0xFF81C784), stats.excelCount),
        CategoryUiModel("Slide", Icons.Default.Slideshow, Color(0xFFFFB74D), stats.pptCount),
        CategoryUiModel("Txt", Icons.Default.TextSnippet, Color(0xFF90A4AE), stats.txtCount),
        CategoryUiModel("Favoritos", Icons.Default.Star, Color(0xFFFFC107), stats.favoritesCount)
    )

    // Renderizamos las filas
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            categories.take(3).forEach { CategoryItem(it, onCategoryClick) }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            categories.drop(3).take(3).forEach { CategoryItem(it, onCategoryClick) }
        }
    }
}

@Composable
private fun CategoryItem(item: CategoryUiModel, onClick: (String) -> Unit) {
    Column(
        modifier = Modifier.width(80.dp), // Ancho fijo para alineación
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(item.color.copy(alpha = 0.15f))
                .clickable { onClick(item.name) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.name,
                tint = item.color,
                modifier = Modifier.size(30.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = item.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "${item.count}",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )
    }
}

// Data class privada para la UI interna
private data class CategoryUiModel(
    val name: String,
    val icon: ImageVector,
    val color: Color,
    val count: Int
)