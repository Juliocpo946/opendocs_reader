package com.example.opendocs_reader.shared.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun ViewerBottomBar(
    currentPage: Int,
    totalPages: Int,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit
) {
    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousPage, enabled = currentPage > 0) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Página anterior")
            }

            Text(
                text = "Página ${currentPage + 1} de $totalPages",
                style = MaterialTheme.typography.bodyMedium
            )

            IconButton(onClick = onNextPage, enabled = currentPage < totalPages - 1) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Siguiente página")
            }
        }
    }
}