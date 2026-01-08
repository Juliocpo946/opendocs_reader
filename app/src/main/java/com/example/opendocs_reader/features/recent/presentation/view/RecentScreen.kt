package com.example.opendocs_reader.features.recent.presentation.view

import android.content.Intent
import android.net.Uri
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
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.example.opendocs_reader.R
import com.example.opendocs_reader.features.recent.presentation.viewmodel.RecentViewModel
import com.example.opendocs_reader.shared.components.*
import com.example.opendocs_reader.core.utils.FileUtils
import com.example.opendocs_reader.core.domain.model.DocFile
import java.io.File

@Composable
fun RecentScreen(
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
    var activeActionFile by remember { mutableStateOf<DocFile?>(null) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showPropertiesDialog by remember { mutableStateOf(false) }
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

    if (showSortSheet) {
        SortBottomSheet(
            currentSort = sortOption,
            onSortSelected = { viewModel.onSortChange(it); showSortSheet = false },
            onDismiss = { showSortSheet = false }
        )
    }

    if (selectedFileForOptions != null) {
        val file = selectedFileForOptions!!
        FileOptionsSheet(
            file = file,
            onDismiss = { selectedFileForOptions = null },
            onFavorite = { viewModel.toggleFavorite(file); selectedFileForOptions = null },
            onShare = { shareFileRecent(context, file); selectedFileForOptions = null },
            onRename = { activeActionFile = file; showRenameDialog = true; selectedFileForOptions = null },
            onDelete = { activeActionFile = file; showDeleteDialog = true; selectedFileForOptions = null },
            onProperties = { activeActionFile = file; showPropertiesDialog = true; selectedFileForOptions = null },
            onShortcut = { createShortcutRecent(context, file); selectedFileForOptions = null }
        )
    }

    if (showRenameDialog && activeActionFile != null) {
        RenameFileDialog(
            currentName = activeActionFile!!.name,
            onDismiss = { showRenameDialog = false; activeActionFile = null },
            onConfirm = { newName -> viewModel.renameFile(activeActionFile!!, newName); showRenameDialog = false; activeActionFile = null }
        )
    }

    if (showDeleteDialog && activeActionFile != null) {
        DeleteConfirmationDialog(
            count = 1,
            onDismiss = { showDeleteDialog = false; activeActionFile = null },
            onConfirm = { viewModel.deleteFile(activeActionFile!!); showDeleteDialog = false; activeActionFile = null }
        )
    }

    if (showPropertiesDialog && activeActionFile != null) {
        FilePropertiesDialog(file = activeActionFile!!, onDismiss = { showPropertiesDialog = false; activeActionFile = null })
    }

    if (showBatchDeleteDialog) {
        DeleteConfirmationDialog(
            count = selectedIds.size,
            onDismiss = { showBatchDeleteDialog = false },
            onConfirm = { viewModel.deleteSelected(); showBatchDeleteDialog = false }
        )
    }

    Scaffold(
        // CORRECCIÓN CLAVE: contentWindowInsets = WindowInsets(0.dp)
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
                onShare = { shareFilesRecent(context, viewModel.getSelectedFiles()) },
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
                                    onClick = { viewModel.openFile(file) },
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
                                    onClick = { viewModel.openFile(file) },
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

// Helpers
private fun shareFileRecent(context: android.content.Context, file: DocFile) {
    try {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", File(file.path))
        val intent = ShareCompat.IntentBuilder(context)
            .setType(file.mimeType)
            .setStream(uri)
            .createChooserIntent()
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(intent)
    } catch (e: Exception) { e.printStackTrace() }
}

private fun shareFilesRecent(context: android.content.Context, files: List<DocFile>) {
    if (files.isEmpty()) return
    try {
        val uris = ArrayList<Uri>()
        files.forEach { file -> uris.add(FileProvider.getUriForFile(context, "${context.packageName}.provider", File(file.path))) }
        val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "*/*"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir archivos"))
    } catch (e: Exception) { e.printStackTrace() }
}

private fun createShortcutRecent(context: android.content.Context, file: DocFile) {
    if (ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(FileProvider.getUriForFile(context, "${context.packageName}.provider", File(file.path)), file.mimeType)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        val shortcutInfo = ShortcutInfoCompat.Builder(context, file.id.toString())
            .setShortLabel(file.name).setLongLabel(file.name)
            .setIcon(IconCompat.createWithResource(context, R.mipmap.ic_launcher))
            .setIntent(intent).build()
        ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null)
    }
}