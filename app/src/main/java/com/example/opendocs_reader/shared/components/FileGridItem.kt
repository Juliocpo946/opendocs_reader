package com.example.opendocs_reader.shared.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.opendocs_reader.core.domain.model.DocFile
import com.example.opendocs_reader.core.utils.CategoryUtils
import com.example.opendocs_reader.core.utils.FileUtils

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileGridItem(
    file: DocFile,
    isSelected: Boolean,
    selectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onMenuAction: (String) -> Unit
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    var showMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .combinedClickable(onClick = { if (selectionMode) onLongClick() else onClick() }, onLongClick = onLongClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!selectionMode) {
                Icon(
                    imageVector = if (file.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorito",
                    tint = if (file.isFavorite) Color(0xFFFFC107) else Color.Gray,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onFavoriteClick() }
                        .padding(2.dp)
                )
            } else {
                Spacer(modifier = Modifier.size(24.dp))
            }

            Box {
                if (!selectionMode) {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
                    }
                    FileOptionsMenu(expanded = showMenu, onDismiss = { showMenu = false }, onAction = { showMenu = false; onMenuAction(it) })
                }
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
            contentDescription = null,
            tint = CategoryUtils.getFileColor(file.extension),
            modifier = Modifier.size(60.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = file.name, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = FileUtils.formatSize(file.size), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
    }
}