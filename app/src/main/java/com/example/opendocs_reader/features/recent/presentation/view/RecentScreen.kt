package com.example.opendocs_reader.features.recent.presentation.view

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.opendocs_reader.features.recent.presentation.viewmodel.RecentViewModel
import com.example.opendocs_reader.shared.components.*
import com.example.opendocs_reader.core.utils.FileUtils

@Composable
fun RecentScreen(
    viewModel: RecentViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val isGrid by viewModel.isGridMode.collectAsState()
    val selectedIds by viewModel.selectedIds.collectAsState()
    val selectionMode by viewModel.selectionMode.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    BackHandler(enabled = selectionMode) {
        viewModel.clearSelection()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            uiState.files.isEmpty() -> {
                Text(
                    text = "No has abierto archivos recientemente",
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            else -> {
                if (isGrid) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(items = uiState.files, key = { it.id }) { file ->
                            FileGridItem(
                                file = file,
                                isSelected = selectedIds.contains(file.id),
                                selectionMode = selectionMode,
                                onClick = { viewModel.openFile(file) },
                                onLongClick = { viewModel.toggleSelection(file.id) },
                                onMenuAction = { }
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(items = uiState.files, key = { it.id }) { file ->
                            FileListItem(
                                file = file,
                                isSelected = selectedIds.contains(file.id),
                                selectionMode = selectionMode,
                                subtitleOverride = "Visto: ${FileUtils.formatDate(file.lastAccessed)}",
                                onClick = { viewModel.openFile(file) },
                                onLongClick = { viewModel.toggleSelection(file.id) },
                                onMenuAction = { }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}