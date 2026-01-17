package com.example.opendocs_reader.shared.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ViewerBottomBar(
    currentPage: Int,
    totalPages: Int,
    onConfigClick: () -> Unit,
    onThumbnailsClick: () -> Unit,
    onPrintClick: () -> Unit,    // Nuevo
    onPresentClick: () -> Unit,  // Nuevo
    onMoreClick: () -> Unit
) {
    Surface(
        tonalElevation = 3.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 8.dp)
        ) {
            // Indicador de página
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${currentPage + 1} / $totalPages",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            HorizontalDivider()

            // Botones de acción (Sin repetidos)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Configuración
                IconButton(onClick = onConfigClick) {
                    Icon(Icons.Default.Settings, contentDescription = "Configuración")
                }
                // Miniaturas
                IconButton(onClick = onThumbnailsClick) {
                    Icon(Icons.Default.GridView, contentDescription = "Miniaturas")
                }
                // Presentar (Diapositivas)
                IconButton(onClick = onPresentClick) {
                    Icon(Icons.Default.Slideshow, contentDescription = "Presentar")
                }
                // Imprimir
                IconButton(onClick = onPrintClick) {
                    Icon(Icons.Default.Print, contentDescription = "Imprimir")
                }
                // Más opciones (aquí están Compartir y Favoritos)
                IconButton(onClick = onMoreClick) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Más opciones")
                }
            }
        }
    }
}