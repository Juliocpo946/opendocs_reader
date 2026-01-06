package com.example.opendocs_reader.features.home.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.opendocs_reader.features.home.domain.model.StorageStats
import com.example.opendocs_reader.features.home.domain.repository.FileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: FileRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(StorageStats())
    val uiState: StateFlow<StorageStats> = _uiState.asStateFlow()

    private val _hasPermission = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            repository.getCachedStats().collect { cachedStats ->
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

    fun checkPermissions() {
        val isGranted = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            android.os.Environment.isExternalStorageManager()
        } else {
            true
        }
        updatePermissionStatus(isGranted)
    }

    private fun refreshData() {
        viewModelScope.launch {
            repository.scanAndRefreshStats()
        }
    }
}

class HomeViewModelFactory(private val repository: FileRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}