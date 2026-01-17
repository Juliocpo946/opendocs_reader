package com.example.opendocs_reader.features.home.presentation.view

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.opendocs_reader.core.data.repository.DocRepositoryImpl
import com.example.opendocs_reader.core.data.repository.StatsRepositoryImpl
import com.example.opendocs_reader.core.domain.model.DocFile
import com.example.opendocs_reader.core.navigation.Screen
import com.example.opendocs_reader.core.utils.FileActionsUtils
import com.example.opendocs_reader.features.home.presentation.components.DocCategoryGrid
import com.example.opendocs_reader.features.home.presentation.components.StorageInfoBanner
import com.example.opendocs_reader.features.home.presentation.viewmodel.HomeViewModel
import com.example.opendocs_reader.features.home.presentation.viewmodel.HomeViewModelFactory
import com.example.opendocs_reader.shared.components.*

@Composable
fun HomeScreen(rootNavController: NavController) {
    val context = LocalContext.current
    val statsRepo = remember { StatsRepositoryImpl(context) }
    val docRepo = remember { DocRepositoryImpl(context) }

    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(statsRepo, docRepo)
    )

    val uiState by viewModel.uiState.collectAsState()
    val isSearchActive by viewModel.isSearchActive.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val selectedIds by viewModel.selectedIds.collectAsState()
    val selectionMode by viewModel.selectionMode.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current
    var showSortSheet by remember { mutableStateOf(false) }

    var selectedFileForOptions by remember { mutableStateOf<DocFile?>(null) }
    var showBatchDeleteDialog by remember { mutableStateOf(false) }

    val searchListState = rememberLazyListState()

    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { viewModel.refreshStats() }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshStats()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    BackHandler(enabled = isSearchActive || selectionMode) {
        if (selectionMode) viewModel.clearSelection() else viewModel.onSearchClose()
    }

    LaunchedEffect(sortOption, searchQuery) {
        if (isSearchActive) searchListState.scrollToItem(0)
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
                title = "OpenDocs",
                isSearchActive = isSearchActive,
                searchQuery = searchQuery,
                onSearchTrigger = { viewModel.onSearchTrigger() },
                onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
                onSearchClose = { viewModel.onSearchClose() },
                onPremiumClick = { },
                onSortClick = if (isSearchActive) { { showSortSheet = true } } else null,
                selectionMode = selectionMode,
                selectedCount = selectedIds.size,
                onClearSelection = { viewModel.clearSelection() },
                onSelectAll = { viewModel.selectAll() },
                onDelete = { showBatchDeleteDialog = true },
                onShare = { FileActionsUtils.shareFiles(context, viewModel.getSelectedFiles()) }
            )
        }
    ) { paddingValues ->
        if (isSearchActive) {
            if (searchResults.isEmpty() && searchQuery.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text("No se encontraron resultados", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    state = searchListState,
                    contentPadding = PaddingValues(16.dp),
                    modifier = Modifier.fillMaxSize().padding(paddingValues)
                ) {
                    items(items = searchResults, key = { it.id }) { file ->
                        FileListItem(
                            file = file,
                            isSelected = selectedIds.contains(file.id),
                            selectionMode = selectionMode,
                            onClick = { FileActionsUtils.openFile(rootNavController, file) },
                            onLongClick = { viewModel.toggleSelection(file.id) },
                            onFavoriteClick = { viewModel.toggleFavorite(file) },
                            onMenuClick = { selectedFileForOptions = file }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(text = "Documentos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                DocCategoryGrid(
                    stats = uiState,
                    onCategoryClick = { category -> rootNavController.navigate(Screen.Files.createRoute(category)) }
                )
                Spacer(modifier = Modifier.height(32.dp))
                Text(text = "Detalles de Almacenamiento", style = MaterialTheme.typography.titleSmall, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                StorageInfoBanner(stats = uiState)
                if (!hasStoragePermission()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                                intent.data = Uri.parse("package:${context.packageName}")
                                storagePermissionLauncher.launch(intent)
                            } else {
                                viewModel.refreshStats()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Conceder Permiso de Acceso Total")
                    }
                }
            }
        }
    }
}

@Composable
fun hasStoragePermission(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Environment.isExternalStorageManager() else true
}