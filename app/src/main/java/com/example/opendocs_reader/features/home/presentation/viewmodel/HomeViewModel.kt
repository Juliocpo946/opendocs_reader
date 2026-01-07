package com.example.opendocs_reader.features.home.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.opendocs_reader.core.domain.model.StorageStats
import com.example.opendocs_reader.core.domain.repository.StatsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: StatsRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(StorageStats())
    val uiState: StateFlow<StorageStats> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getCachedStats().collect { stats ->
                _uiState.value = stats
            }
        }
    }

    fun checkPermissions() {
        viewModelScope.launch {
            repository.scanAndRefreshStats()
        }
    }
}

class HomeViewModelFactory(private val repository: StatsRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}