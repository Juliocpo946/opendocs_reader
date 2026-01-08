package com.example.opendocs_reader.features.home.presentation.view

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.opendocs_reader.core.data.repository.StatsRepositoryImpl
import com.example.opendocs_reader.core.navigation.Screen
import com.example.opendocs_reader.features.home.presentation.components.DocCategoryGrid
import com.example.opendocs_reader.features.home.presentation.components.StorageInfoBanner
import com.example.opendocs_reader.features.home.presentation.viewmodel.HomeViewModel
import com.example.opendocs_reader.features.home.presentation.viewmodel.HomeViewModelFactory

@Composable
fun HomeScreen(rootNavController: NavController) {
    val context = LocalContext.current

    // Inyección del repositorio
    val repository = remember { StatsRepositoryImpl(context) }

    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(repository)
    )

    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Launcher para permisos
    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.refreshStats()
    }

    // Refrescar estadísticas cada vez que la pantalla se vuelve visible (ON_RESUME)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshStats()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Documentos",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Grid de categorías (Asegúrate de que DocCategoryGrid use uiState.favoritesCount)
        DocCategoryGrid(
            stats = uiState,
            onCategoryClick = { category ->
                rootNavController.navigate(Screen.Files.createRoute(category))
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Detalles de Almacenamiento",
            style = MaterialTheme.typography.titleSmall,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))

        StorageInfoBanner(stats = uiState)

        // Botón de permisos si hace falta
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

@Composable
fun hasStoragePermission(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Environment.isExternalStorageManager()
    } else {
        true
    }
}