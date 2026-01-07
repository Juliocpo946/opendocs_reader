package com.example.opendocs_reader.shared.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Search
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
    onBackClick: () -> Unit, // <--- Nuevo parámetro
    onToggleView: () -> Unit,
    onSortClick: () -> Unit,
    onSearchClick: () -> Unit,
    onPremiumClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp
                )
            )
        },
        navigationIcon = { // <--- Botón de regresar
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Regresar",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        actions = {
            IconButton(onClick = onPremiumClick) {
                Icon(
                    imageVector = Icons.Default.Diamond,
                    contentDescription = "Premium",
                    tint = Color(0xFF00BCD4)
                )
            }
            IconButton(onClick = onSearchClick) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            IconButton(onClick = onSortClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Sort,
                    contentDescription = "Ordenar",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            IconButton(onClick = onToggleView) {
                Icon(
                    imageVector = if (isGrid) Icons.Default.FormatListBulleted else Icons.Default.Apps,
                    contentDescription = "Cambiar Vista",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}