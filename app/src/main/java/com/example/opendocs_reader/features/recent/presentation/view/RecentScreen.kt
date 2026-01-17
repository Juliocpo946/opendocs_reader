package com.example.opendocs_reader.features.recent.presentation.view

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.opendocs_reader.features.recent.presentation.viewmodel.RecentViewModel
import com.example.opendocs_reader.shared.components.*
import com.example.opendocs_reader.core.utils.FileUtils
import com.example.opendocs_reader.core.utils.FileActionsUtils
import com.example.opendocs_reader.core.domain.model.DocFile

@Composable
fun RecentScreen(
    navController: NavController,
    viewModel: RecentViewModel
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val isGrid by viewModel.isGridMode.collectAsState()
    val selectedIds by viewModel.selectedIds.collectAsState()
    val selectionMode by viewModel.selectionMode.collectAsState()
    val isSearchActive by viewModel.isSearchActive.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()

    var showSortSheet by remember { mutableStateOf(false) }
    var selectedFileForOptions by remember { mutableStateOf<DocFile?>(null) }
    var showBatchDeleteDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()

    LaunchedEffect(Unit) { viewModel.refresh() }

    LaunchedEffect(sortOption, searchQuery) {
        listState.scrollToItem(0)
        gridState.scrollToItem(0)
    }

    BackHandler(enabled = isSearchActive || selectionMode) {
        if (selectionMode) viewModel.clearSelection() else viewModel.onSearchClose()
    }

    FileOperationManager(
        context = context,
        selectedFile = selectedFileForOptions,
        onDismissSheet = { selectedFileForOptions = null },
        showBatchDelete = showBatchDeleteDialog,
        onDismissBatchDelete = { showBatchDeleteDialog = false },
        selectedCount = selectedIds.size,
        onFavorite = { viewModel.toggleFavorite(it) },
        onRename = { file, name -> viewModel.renameFile(file, name) },
        onDelete = { viewModel.deleteFile(it) },
        onBatchDeleteConfirm = { viewModel.deleteSelected() }
    )

    if (showSortSheet) {
        SortBottomSheet(
            currentSort = sortOption,
            onSortSelected = { viewModel.onSortChange(it); showSortSheet = false },
            onDismiss = { showSortSheet = false }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            OpenDocsTopBar(
                title = "Recientes",
                isSearchActive = isSearchActive,
                searchQuery = searchQuery,
                onSearchTrigger = { viewModel.onSearchTrigger() },
                onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
                onSearchClose = { viewModel.onSearchClose() },
                selectionMode = selectionMode,
                selectedCount = selectedIds.size,
                onClearSelection = { viewModel.clearSelection() },
                onSelectAll = { viewModel.selectAll() },
                onDelete = { showBatchDeleteDialog = true },
                onShare = { FileActionsUtils.shareFiles(context, viewModel.getSelectedFiles()) },
                isGrid = isGrid,
                onToggleView = { viewModel.toggleViewMode() },
                onPremiumClick = { },
                onSortClick = { showSortSheet = true }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                uiState.files.isEmpty() -> {
                    Text(
                        text = if(searchQuery.isNotEmpty()) "Sin resultados" else "No has abierto archivos recientemente",
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    if (isGrid) {
                        LazyVerticalGrid(
                            state = gridState,
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
                                    onClick = { FileActionsUtils.openFile(navController, file) },
                                    onLongClick = { viewModel.toggleSelection(file.id) },
                                    onFavoriteClick = { viewModel.toggleFavorite(file) },
                                    onMenuClick = { selectedFileForOptions = file }
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(items = uiState.files, key = { it.id }) { file ->
                                FileListItem(
                                    file = file,
                                    isSelected = selectedIds.contains(file.id),
                                    selectionMode = selectionMode,
                                    subtitleOverride = "Visto: ${FileUtils.formatDate(file.lastAccessed)}",
                                    onClick = { FileActionsUtils.openFile(navController, file) },
                                    onLongClick = { viewModel.toggleSelection(file.id) },
                                    onFavoriteClick = { viewModel.toggleFavorite(file) },
                                    onMenuClick = { selectedFileForOptions = file }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}