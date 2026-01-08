package com.example.opendocs_reader.features.files.presentation.view

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.opendocs_reader.R
import com.example.opendocs_reader.core.data.repository.DocRepositoryImpl
import com.example.opendocs_reader.core.domain.model.DocFile
import com.example.opendocs_reader.core.utils.CategoryUtils
import com.example.opendocs_reader.shared.components.*
import com.example.opendocs_reader.features.files.presentation.viewmodel.FilesViewModel
import com.example.opendocs_reader.shared.theme.*
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FilesScreen(
    navController: NavController,
    initialCategory: String
) {
    val context = LocalContext.current
    val repository = remember { DocRepositoryImpl(context) }
    val viewModel: FilesViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return FilesViewModel(repository) as T
            }
        }
    )

    val categories = CategoryUtils.categories
    val initialIndex = categories.indexOfFirst { it.id.equals(initialCategory, ignoreCase = true) }.coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = initialIndex, pageCount = { categories.size })
    val scope = rememberCoroutineScope()

    val isGrid by viewModel.isGridMode.collectAsState()
    val files by viewModel.files.collectAsState()
    val isSearchActive by viewModel.isSearchActive.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val selectedIds by viewModel.selectedIds.collectAsState()
    val selectionMode by viewModel.selectionMode.collectAsState()

    var showSortSheet by remember { mutableStateOf(false) }
    var selectedFileForOptions by remember { mutableStateOf<DocFile?>(null) }
    var activeActionFile by remember { mutableStateOf<DocFile?>(null) }

    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showPropertiesDialog by remember { mutableStateOf(false) }
    var showBatchDeleteDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = isSearchActive || selectionMode) {
        if (selectionMode) viewModel.clearSelection() else viewModel.onSearchClose()
    }

    LaunchedEffect(pagerState.currentPage) {
        viewModel.fetchFiles(categories[pagerState.currentPage].id)
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
            onShare = { shareFile(context, file); selectedFileForOptions = null },
            onRename = { activeActionFile = file; showRenameDialog = true; selectedFileForOptions = null },
            onDelete = { activeActionFile = file; showDeleteDialog = true; selectedFileForOptions = null },
            onProperties = { activeActionFile = file; showPropertiesDialog = true; selectedFileForOptions = null },
            onShortcut = { createShortcut(context, file); selectedFileForOptions = null }
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
        topBar = {
            FilesTopBar(
                title = categories[pagerState.currentPage].name,
                isGrid = isGrid,
                selectionMode = selectionMode,
                selectedCount = selectedIds.size,
                isSearchActive = isSearchActive,
                searchQuery = searchQuery,
                onBackClick = { navController.popBackStack() },
                onClearSelection = { viewModel.clearSelection() },
                onSelectAll = { viewModel.selectAll() },
                onDelete = { showBatchDeleteDialog = true },
                onShare = { shareFiles(context, viewModel.getSelectedFiles()) },
                onToggleView = { viewModel.toggleViewMode() },
                onSearchTrigger = { viewModel.onSearchTrigger() },
                onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
                onSearchClose = { viewModel.onSearchClose() },
                onSortClick = { showSortSheet = true },
                onPremiumClick = { }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            if (!isSearchActive) {
                ScrollableTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    edgePadding = 16.dp,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = PrimaryLight,
                    indicator = { tabPositions ->
                        if (pagerState.currentPage < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]), color = PrimaryLight)
                        }
                    }
                ) {
                    categories.forEachIndexed { index, category ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                            text = { Text(text = category.name) }
                        )
                    }
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = !isSearchActive
            ) {
                val listState = rememberLazyListState()
                val gridState = rememberLazyGridState()

                LaunchedEffect(sortOption, searchQuery) {
                    listState.scrollToItem(0)
                    gridState.scrollToItem(0)
                }

                if (files.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(if(searchQuery.isNotEmpty()) "Sin resultados" else "No hay archivos", color = Color.Gray)
                    }
                } else {
                    if (isGrid) {
                        LazyVerticalGrid(
                            state = gridState,
                            columns = GridCells.Fixed(3),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(items = files, key = { it.id }) { file ->
                                FileGridItem(
                                    file = file,
                                    isSelected = selectedIds.contains(file.id),
                                    selectionMode = selectionMode,
                                    onClick = { /* Abrir */ },
                                    onLongClick = { viewModel.toggleSelection(file.id) },
                                    onFavoriteClick = { viewModel.toggleFavorite(file) },
                                    // Cambio: Ahora usamos onMenuClick
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
                            items(items = files, key = { it.id }) { file ->
                                FileListItem(
                                    file = file,
                                    isSelected = selectedIds.contains(file.id),
                                    selectionMode = selectionMode,
                                    onClick = { /* Abrir */ },
                                    onLongClick = { viewModel.toggleSelection(file.id) },
                                    onFavoriteClick = { viewModel.toggleFavorite(file) },
                                    // Cambio: Ahora usamos onMenuClick
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

private fun shareFile(context: android.content.Context, file: DocFile) {
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

private fun shareFiles(context: android.content.Context, files: List<DocFile>) {
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

private fun createShortcut(context: android.content.Context, file: DocFile) {
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