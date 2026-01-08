package com.example.opendocs_reader.shared.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
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
fun FileListItem(
    file: DocFile,
    isSelected: Boolean,
    selectionMode: Boolean,
    subtitleOverride: String? = null,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMenuAction: (String) -> Unit
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .combinedClickable(onClick = { if (selectionMode) onLongClick() else onClick() }, onLongClick = onLongClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
            contentDescription = null,
            tint = CategoryUtils.getFileColor(file.extension),
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = file.name, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitleOverride ?: "${FileUtils.formatSize(file.size)} • ${FileUtils.formatDate(file.dateAdded)}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
        if (!selectionMode) {
            Box {
                IconButton(onClick = { showMenu = true }) { Icon(Icons.Default.MoreVert, contentDescription = "Opciones") }
                FileOptionsMenu(expanded = showMenu, onDismiss = { showMenu = false }, onAction = { showMenu = false; onMenuAction(it) })
            }
        }
    }
}