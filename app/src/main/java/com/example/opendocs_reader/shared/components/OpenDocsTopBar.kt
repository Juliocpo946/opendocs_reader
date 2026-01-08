package com.example.opendocs_reader.shared.components

import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenDocsTopBar(
    title: String,
    showActions: Boolean = true,
    isSearchActive: Boolean = false,
    searchQuery: String = "",
    onSearchTrigger: () -> Unit = {},
    onSearchQueryChange: (String) -> Unit = {},
    onSearchClose: () -> Unit = {},
    selectionMode: Boolean = false,
    selectedCount: Int = 0,
    onClearSelection: () -> Unit = {},
    onSelectAll: () -> Unit = {},
    onDelete: () -> Unit = {},
    onShare: () -> Unit = {},
    isGrid: Boolean = false,
    onToggleView: (() -> Unit)? = null,
    onPremiumClick: () -> Unit = {},
    onSortClick: (() -> Unit)? = null
) {
    val focusRequester = remember { FocusRequester() }

    val targetContainerColor = if (selectionMode) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.background
    val targetContentColor = if (selectionMode) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onBackground

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) focusRequester.requestFocus()
    }

    Surface(
        color = targetContainerColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent,
                titleContentColor = targetContentColor,
                actionIconContentColor = targetContentColor,
                navigationIconContentColor = targetContentColor
            ),
            title = {
                if (selectionMode) Text("$selectedCount seleccionados", style = MaterialTheme.typography.titleLarge)
                else if (isSearchActive) TextField(
                    value = searchQuery, onValueChange = onSearchQueryChange,
                    placeholder = { Text("Buscar...", color = targetContentColor.copy(0.7f)) },
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = targetContentColor,
                        focusedTextColor = targetContentColor,
                        unfocusedTextColor = targetContentColor
                    )
                )
                else Text(title, style = MaterialTheme.typography.titleLarge)
            },
            navigationIcon = {
                if (selectionMode) IconButton(onClick = onClearSelection) { Icon(Icons.Default.Close, "Cerrar") }
                else if (isSearchActive) IconButton(onClick = onSearchClose) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás") }
            },
            actions = {
                if (selectionMode) {
                    IconButton(onClick = onSelectAll) { Icon(Icons.Default.SelectAll, "Todo") }
                    IconButton(onClick = onShare) { Icon(Icons.Default.Share, "Compartir") }
                    IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Eliminar") }
                } else if (isSearchActive) {
                    if (searchQuery.isNotEmpty()) IconButton(onClick = { onSearchQueryChange("") }) { Icon(Icons.Default.Close, "Limpiar") }
                    if (onSortClick != null) IconButton(onClick = onSortClick) { Icon(Icons.AutoMirrored.Filled.Sort, "Ordenar") }
                } else if (showActions) {
                    IconButton(onClick = onPremiumClick) { Icon(Icons.Default.Diamond, "Premium", tint = Color(0xFF00BCD4)) }
                    IconButton(onClick = onSearchTrigger) { Icon(Icons.Default.Search, "Buscar") }
                    if (onSortClick != null) IconButton(onClick = onSortClick) { Icon(Icons.AutoMirrored.Filled.Sort, "Ordenar") }
                    if (onToggleView != null) IconButton(onClick = onToggleView) { Icon(if (isGrid) Icons.AutoMirrored.Filled.FormatListBulleted else Icons.Default.Apps, "Vista") }
                }
            }
        )
    }
}