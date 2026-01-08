package com.example.opendocs_reader.shared.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShortText
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.opendocs_reader.core.domain.model.DocFile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileOptionsSheet(
    file: DocFile,
    onDismiss: () -> Unit,
    onFavorite: () -> Unit,
    onShare: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onProperties: () -> Unit,
    onShortcut: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            // Cabecera con nombre del archivo
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.DriveFileMove,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Divider()

            OptionItem(
                icon = if (file.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                text = if (file.isFavorite) "Quitar de favoritos" else "Añadir a favoritos",
                onClick = onFavorite
            )
            OptionItem(icon = Icons.Default.Share, text = "Compartir", onClick = onShare)
            OptionItem(icon = Icons.Default.Edit, text = "Renombrar", onClick = onRename)
            OptionItem(icon = Icons.Default.Delete, text = "Eliminar", onClick = onDelete)
            OptionItem(icon = Icons.Default.Info, text = "Propiedades", onClick = onProperties)
            OptionItem(icon = Icons.Default.ShortText, text = "Crear acceso directo", onClick = onShortcut)
        }
    }
}

@Composable
private fun OptionItem(icon: ImageVector, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(24.dp))
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}