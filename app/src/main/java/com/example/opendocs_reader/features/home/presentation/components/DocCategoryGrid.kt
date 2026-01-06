package com.example.opendocs_reader.features.home.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.opendocs_reader.features.home.domain.model.StorageStats
import com.example.opendocs_reader.shared.theme.*

@Composable
fun DocCategoryGrid(stats: StorageStats, onCategoryClick: (String) -> Unit) {
    val categories = listOf(
        CategoryUiModel("PDF", Icons.Default.PictureAsPdf, PdfColor, stats.pdfCount),
        CategoryUiModel("Word", Icons.Default.Description, WordColor, stats.wordCount),
        CategoryUiModel("Excel", Icons.Default.TableChart, ExcelColor, stats.excelCount),
        CategoryUiModel("Slide", Icons.Default.Slideshow, PptColor, stats.pptCount),
        CategoryUiModel("Txt", Icons.Default.TextSnippet, TxtColor, stats.txtCount),
        CategoryUiModel("Favoritos", Icons.Default.Star, FavoriteColor, stats.favoritesCount)
    )

    val rows = categories.chunked(3)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                rowItems.forEach { item ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CategoryItem(item, onCategoryClick)
                    }
                }
                if (rowItems.size < 3) {
                    repeat(3 - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryItem(item: CategoryUiModel, onClick: (String) -> Unit) {
    Column(
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
                modifier = Modifier.size(32.dp)
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

private data class CategoryUiModel(
    val name: String,
    val icon: ImageVector,
    val color: Color,
    val count: Int
)