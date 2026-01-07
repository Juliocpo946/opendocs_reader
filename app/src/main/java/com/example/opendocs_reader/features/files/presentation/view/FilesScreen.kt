package com.example.opendocs_reader.features.files.presentation.view

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.example.opendocs_reader.features.files.presentation.components.FileGridItem
import com.example.opendocs_reader.features.files.presentation.components.FileListItem
import com.example.opendocs_reader.features.files.presentation.viewmodel.FilesViewModel
import com.example.opendocs_reader.shared.components.FilesTopBar
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

    // Sincronizar Pager con ViewModel
    LaunchedEffect(pagerState.currentPage) {
        viewModel.fetchFiles(categories[pagerState.currentPage].id)
    }

    Scaffold(
        topBar = {
            FilesTopBar(
                title = categories[pagerState.currentPage].name,
                isGrid = isGrid,
                onBackClick = { navController.popBackStack() },
                onToggleView = { viewModel.toggleViewMode() },
                onSortClick = { },
                onSearchClick = { },
                onPremiumClick = { }
            )
        }
    ) { paddingValues ->

        Column(modifier = Modifier.padding(paddingValues)) {
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
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = { Text(text = category.name) }
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) {
                if (files.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No se encontraron archivos", color = Color.Gray)
                    }
                } else {
                    if (isGrid) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(files) { file ->
                                FileGridItem(file) { /* click action */ }
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.Top,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(files) { file ->
                                FileListItem(file) { /* click action */ }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}