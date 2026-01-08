package com.example.opendocs_reader.features.menu.presentation.view

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.opendocs_reader.core.di.AppModule
import com.example.opendocs_reader.core.navigation.Screen
import com.example.opendocs_reader.features.home.presentation.view.HomeScreen
import com.example.opendocs_reader.features.recent.presentation.view.RecentScreen
import com.example.opendocs_reader.features.recent.presentation.viewmodel.RecentViewModel
import com.example.opendocs_reader.features.settings.presentation.view.SettingsScreen
import com.example.opendocs_reader.shared.components.FilesTopBar
import com.example.opendocs_reader.shared.components.OpenDocsBottomBar
import com.example.opendocs_reader.shared.components.OpenDocsTopBar

@Composable
fun MenuScreen(rootNavController: NavController) {
    val menuNavController = rememberNavController()
    val navBackStackEntry by menuNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val context = LocalContext.current

    val recentViewModel: RecentViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return RecentViewModel(AppModule.provideDocRepository(context)) as T
            }
        }
    )

    val isGrid by recentViewModel.isGridMode.collectAsState()
    val selectedIds by recentViewModel.selectedIds.collectAsState()
    val selectionMode by recentViewModel.selectionMode.collectAsState()

    Scaffold(
        topBar = {
            when (currentRoute) {
                Screen.Recent.route -> {
                    FilesTopBar(
                        title = "Recientes",
                        isGrid = isGrid,
                        selectionMode = selectionMode,
                        selectedCount = selectedIds.size,
                        onBackClick = null, // ESTA LÍNEA OCULTA LA FLECHA
                        onClearSelection = { recentViewModel.clearSelection() },
                        onSelectAll = { recentViewModel.selectAll() },
                        onDelete = { recentViewModel.deleteSelected() },
                        onShare = { },
                        onToggleView = { recentViewModel.toggleViewMode() },
                        onSortClick = { },
                        onSearchClick = { },
                        onPremiumClick = { }
                    )
                }
                Screen.Settings.route -> OpenDocsTopBar(title = "Configuración")
                else -> OpenDocsTopBar(title = "OpenDocs")
            }
        },
        bottomBar = {
            OpenDocsBottomBar(navController = menuNavController)
        }
    ) { paddingValues ->
        NavHost(
            navController = menuNavController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(route = Screen.Home.route) {
                HomeScreen(rootNavController = rootNavController)
            }
            composable(route = Screen.Recent.route) {
                RecentScreen(viewModel = recentViewModel)
            }
            composable(route = Screen.Settings.route) {
                SettingsScreen()
            }
        }
    }
}