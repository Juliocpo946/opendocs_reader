package com.example.opendocs_reader.features.files.presentation.view

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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.opendocs_reader.core.data.repository.DocRepositoryImpl
import com.example.opendocs_reader.core.utils.CategoryUtils
import com.example.opendocs_reader.shared.components.FileGridItem
import com.example.opendocs_reader.shared.components.FileListItem
import com.example.opendocs_reader.features.files.presentation.viewmodel.FilesViewModel
import com.example.opendocs_reader.shared.components.FilesTopBar
import com.example.opendocs_reader.shared.components.SortBottomSheet
import com.example.opendocs_reader.shared.theme.*
import kotlinx.coroutines.launch

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

    BackHandler(enabled = isSearchActive || selectionMode) {
        if (selectionMode) viewModel.clearSelection() else viewModel.onSearchClose()
    }

    LaunchedEffect(pagerState.currentPage) {
        viewModel.fetchFiles(categories[pagerState.currentPage].id)
    }

    if (showSortSheet) {
        SortBottomSheet(
            currentSort = sortOption,
            onSortSelected = {
                viewModel.onSortChange(it)
                showSortSheet = false
            },
            onDismiss = { showSortSheet = false }
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
                onDelete = { viewModel.deleteSelected() },
                onShare = { },
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
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                                color = PrimaryLight
                            )
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
                // CORRECCIÓN DE SCROLL: Creamos el estado para la lista y el grid
                val listState = rememberLazyListState()
                val gridState = rememberLazyGridState()

                // "¡Oye! Si cambia el orden o la búsqueda, vete al inicio"
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
                            state = gridState, // Asignamos el estado
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
                                    onMenuAction = { }
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            state = listState, // Asignamos el estado
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
}