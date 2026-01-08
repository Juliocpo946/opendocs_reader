package com.example.opendocs_reader.features.home.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.opendocs_reader.core.domain.model.DocFile
import com.example.opendocs_reader.core.domain.model.StorageStats
import com.example.opendocs_reader.core.domain.repository.DocRepository
import com.example.opendocs_reader.core.domain.repository.StatsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val statsRepository: StatsRepository,
    private val docRepository: DocRepository // Necesitamos el DocRepo para buscar
) : ViewModel() {

    private val _uiState = MutableStateFlow(StorageStats())
    val uiState: StateFlow<StorageStats> = _uiState.asStateFlow()

    // Estado Búsqueda Global
    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive = _isSearchActive.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<DocFile>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            statsRepository.getCachedStats().collect { stats ->
                _uiState.value = stats
            }
        }
        refreshStats()
    }

    fun refreshStats() {
        viewModelScope.launch { statsRepository.scanAndRefreshStats() }
    }

    // --- Búsqueda Global ---
    fun onSearchTrigger() { _isSearchActive.value = true }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()

        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        // Debounce de 300ms para no saturar DB
        searchJob = viewModelScope.launch {
            delay(300)
            val results = docRepository.searchFiles(query)
            _searchResults.value = results
        }
    }

    fun onSearchClose() {
        _isSearchActive.value = false
        _searchQuery.value = ""
        _searchResults.value = emptyList()
    }

    // Toggle favorito en resultados de búsqueda
    fun toggleFavorite(file: DocFile) {
        viewModelScope.launch {
            docRepository.toggleFavorite(file)
            // Actualizar visualmente la lista de resultados
            _searchResults.value = _searchResults.value.map {
                if(it.id == file.id) it.copy(isFavorite = !it.isFavorite) else it
            }
            refreshStats() // Actualizar contadores también
        }
    }
}

class HomeViewModelFactory(
    private val statsRepository: StatsRepository,
    private val docRepository: DocRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(statsRepository, docRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}