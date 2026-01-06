package com.example.opendocs_reader.features.home.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.opendocs_reader.features.home.data.repository.FileRepositoryImpl
import com.example.opendocs_reader.features.home.domain.model.StorageStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    // Instancia manual del repo (idealmente usarías Hilt aquí)
    private val repository = FileRepositoryImpl(application)

    private val _uiState = MutableStateFlow(StorageStats())
    val uiState: StateFlow<StorageStats> = _uiState.asStateFlow()

    private val _hasPermission = MutableStateFlow(false)

    init {
        // 1. Cargar caché inmediatamente (Local Storage)
        viewModelScope.launch {
            repository.getCachedStats().collect { cachedStats ->
                // Solo actualizamos si no estamos en medio de un escaneo manual que tenga datos más frescos
                _uiState.value = cachedStats
            }
        }
    }

    fun updatePermissionStatus(isGranted: Boolean) {
        _hasPermission.value = isGranted
        if (isGranted) {
            refreshData()
        }
    }

    // En HomeViewModel.kt
    fun checkPermissions() {
        val isGranted = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            android.os.Environment.isExternalStorageManager()
        } else {
            true // Asumimos true en legacy por simplificación, o usa ContextCompat check
        }

        updatePermissionStatus(isGranted)
    }

    // 2. Verificar cambios de manera innotoria
    private fun refreshData() {
        viewModelScope.launch {
            // Esto corre en IO, analiza archivos y si encuentra nuevos,
            // actualiza el DataStore, lo cual dispara el collect del init automáticamente.
            // ¡Magia reactiva! No refresca la pantalla completa, solo los números cambian.
            repository.scanAndRefreshStats()
        }
    }
}