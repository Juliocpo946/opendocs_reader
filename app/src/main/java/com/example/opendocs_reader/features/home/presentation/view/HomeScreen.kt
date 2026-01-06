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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.opendocs_reader.features.home.presentation.components.DocCategoryGrid
import com.example.opendocs_reader.features.home.presentation.components.StorageInfoBanner
import com.example.opendocs_reader.features.home.presentation.viewmodel.HomeViewModel

@Composable
fun HomeScreen(viewModel: HomeViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Launcher para ir a Ajustes si falta el permiso "especial"
    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        // Al volver de ajustes, verificamos de nuevo
        viewModel.checkPermissions()
    }

    // Detector de ciclo de vida: Si el usuario sale a dar permiso y vuelve, refrescamos.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkPermissions()
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
        // --- SECCIÓN 1: GRID DE DOCUMENTOS ---
        Text(
            text = "Documentos",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Usamos el componente fragmentado
        DocCategoryGrid(
            stats = uiState,
            onCategoryClick = { category ->
                // Aquí pondrías la navegación a la lista filtrada
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- SECCIÓN 2: BANNER DE DETALLES ---
        Text(
            text = "Detalles de Almacenamiento",
            style = MaterialTheme.typography.titleSmall,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Usamos el componente fragmentado
        StorageInfoBanner(stats = uiState)

        // --- SECCIÓN 3: BOTÓN DE PERMISO (Si falta) ---
        // Si no tiene permisos, mostramos un botón para pedirlo
        if (!hasStoragePermission()) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                        intent.data = Uri.parse("package:${context.packageName}")
                        storagePermissionLauncher.launch(intent)
                    } else {
                        // Lógica para Android 10 o inferior (ya cubierta por el manifest legacy)
                        viewModel.checkPermissions()
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

// Función auxiliar simple para verificar permiso en la UI
@Composable
fun hasStoragePermission(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Environment.isExternalStorageManager()
    } else {
        true // En versiones viejas el Manifest se encarga
    }
}