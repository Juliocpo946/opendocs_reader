package com.example.opendocs_reader.shared.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilesTopBar(
    title: String,
    isGrid: Boolean,
    // Nuevos parámetros para selección
    selectionMode: Boolean,
    selectedCount: Int,
    onBackClick: () -> Unit,
    onClearSelection: () -> Unit,
    onSelectAll: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    // Parámetros normales
    onToggleView: () -> Unit,
    onSortClick: () -> Unit,
    onSearchClick: () -> Unit,
    onPremiumClick: () -> Unit
) {
    // Lógica para cambiar colores en modo selección
    val containerColor = if (selectionMode) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val contentColor = if (selectionMode) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface

    TopAppBar(
        title = {
            Text(
                text = if (selectionMode) "$selectedCount seleccionados" else title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp
                )
            )
        },
        navigationIcon = {
            if (selectionMode) {
                // Icono X para cancelar selección
                IconButton(onClick = onClearSelection) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancelar",
                        tint = contentColor
                    )
                }
            } else {
                // Icono Flecha para volver atrás
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Regresar",
                        tint = contentColor
                    )
                }
            }
        },
        actions = {
            if (selectionMode) {
                // Acciones de Modo Selección
                IconButton(onClick = onSelectAll) {
                    Icon(Icons.Default.SelectAll, "Seleccionar todo", tint = contentColor)
                }
                IconButton(onClick = onShare) {
                    Icon(Icons.Default.Share, "Compartir", tint = contentColor)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Borrar", tint = contentColor)
                }
            } else {
                // Acciones Normales
                IconButton(onClick = onPremiumClick) {
                    Icon(Icons.Default.Diamond, "Premium", tint = Color(0xFF00BCD4))
                }
                IconButton(onClick = onSearchClick) {
                    Icon(Icons.Default.Search, "Buscar", tint = contentColor)
                }
                IconButton(onClick = onSortClick) {
                    Icon(Icons.AutoMirrored.Filled.Sort, "Ordenar", tint = contentColor)
                }
                IconButton(onClick = onToggleView) {
                    Icon(
                        imageVector = if (isGrid) Icons.AutoMirrored.Filled.FormatListBulleted else Icons.Default.Apps,
                        contentDescription = "Cambiar Vista",
                        tint = contentColor
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = containerColor,
            titleContentColor = contentColor,
            actionIconContentColor = contentColor,
            navigationIconContentColor = contentColor
        )
    )
}