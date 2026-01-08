package com.example.opendocs_reader.shared.components

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun FileOptionsMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onAction: (String) -> Unit
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        DropdownMenuItem(text = { Text("Abrir") }, onClick = { onAction("open") })
        DropdownMenuItem(text = { Text("Compartir") }, onClick = { onAction("share") })
        DropdownMenuItem(text = { Text("Eliminar") }, onClick = { onAction("delete") })
    }
}