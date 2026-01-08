package com.example.opendocs_reader.shared.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import com.example.opendocs_reader.LocalAnimatedSurface
import com.example.opendocs_reader.LocalAnimatedOnSurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenDocsTopBar(
    title: String,
    // Control nuevo para ocultar íconos
    showActions: Boolean = true,

    // Estado de Búsqueda
    isSearchActive: Boolean = false,
    searchQuery: String = "",
    onSearchTrigger: () -> Unit = {},
    onSearchQueryChange: (String) -> Unit = {},
    onSearchClose: () -> Unit = {},

    // Estado de Selección
    selectionMode: Boolean = false,
    selectedCount: Int = 0,
    onClearSelection: () -> Unit = {},
    onSelectAll: () -> Unit = {},
    onDelete: () -> Unit = {},

    // Estado de Vista
    isGrid: Boolean = false,
    onToggleView: (() -> Unit)? = null,

    // Acciones Generales
    onPremiumClick: () -> Unit = {}
) {
    val focusRequester = remember { FocusRequester() }

    val containerColor = if (selectionMode) MaterialTheme.colorScheme.primaryContainer else LocalAnimatedSurface.current
    val contentColor = if (selectionMode) MaterialTheme.colorScheme.onPrimaryContainer else LocalAnimatedOnSurface.current

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            focusRequester.requestFocus()
        }
    }

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = containerColor,
            titleContentColor = contentColor,
            actionIconContentColor = contentColor,
            navigationIconContentColor = contentColor
        ),
        title = {
            if (selectionMode) {
                Text(text = "$selectedCount seleccionados", style = MaterialTheme.typography.titleLarge)
            } else if (isSearchActive) {
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Buscar...", color = contentColor.copy(alpha = 0.7f)) },
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = contentColor,
                        focusedTextColor = contentColor,
                        unfocusedTextColor = contentColor
                    )
                )
            } else {
                Text(text = title, style = MaterialTheme.typography.titleLarge)
            }
        },
        navigationIcon = {
            if (selectionMode) {
                IconButton(onClick = onClearSelection) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar selección")
                }
            } else if (isSearchActive) {
                IconButton(onClick = onSearchClose) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cerrar búsqueda")
                }
            }
        },
        actions = {
            if (selectionMode) {
                IconButton(onClick = onSelectAll) { Icon(Icons.Default.SelectAll, contentDescription = "Todo") }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Eliminar") }
            } else if (isSearchActive) {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Limpiar")
                    }
                }
            } else if (showActions) {
                // Modo Normal: Solo mostramos estos si showActions es true
                IconButton(onClick = onPremiumClick) {
                    Icon(Icons.Default.Diamond, contentDescription = "Premium", tint = Color(0xFF00BCD4))
                }
                IconButton(onClick = onSearchTrigger) {
                    Icon(Icons.Default.Search, contentDescription = "Buscar")
                }

                if (onToggleView != null) {
                    IconButton(onClick = onToggleView) {
                        Icon(
                            imageVector = if (isGrid) Icons.AutoMirrored.Filled.FormatListBulleted else Icons.Default.Apps,
                            contentDescription = "Cambiar vista"
                        )
                    }
                }
            }
        }
    )
}