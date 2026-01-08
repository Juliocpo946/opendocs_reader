package com.example.opendocs_reader.features.home.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.opendocs_reader.core.domain.model.DocFile
import com.example.opendocs_reader.core.domain.model.SortOption
import com.example.opendocs_reader.core.domain.model.StorageStats
import com.example.opendocs_reader.core.domain.repository.DocRepository
import com.example.opendocs_reader.core.domain.repository.StatsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(
    private val statsRepository: StatsRepository,
    private val docRepository: DocRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StorageStats())
    val uiState: StateFlow<StorageStats> = _uiState.asStateFlow()

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive = _isSearchActive.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _rawSearchResults = MutableStateFlow<List<DocFile>>(emptyList())
    private val _sortOption = MutableStateFlow(SortOption.DEFAULT)
    val sortOption = _sortOption.asStateFlow()

    // Pipeline para búsqueda
    val searchResults: StateFlow<List<DocFile>> = combine(_rawSearchResults, _sortOption) { files, sort ->
        sortFiles(files, sort)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            statsRepository.getCachedStats().collect { stats -> _uiState.value = stats }
        }
        refreshStats()
    }

    fun refreshStats() {
        viewModelScope.launch { statsRepository.scanAndRefreshStats() }
    }

    private fun sortFiles(files: List<DocFile>, option: SortOption): List<DocFile> {
        return when (option) {
            SortOption.NAME_ASC -> files.sortedBy { it.name }
            SortOption.NAME_DESC -> files.sortedByDescending { it.name }
            SortOption.DATE_NEWEST -> files.sortedByDescending { it.dateAdded }
            SortOption.DATE_OLDEST -> files.sortedBy { it.dateAdded }
            SortOption.TYPE -> files.sortedWith(compareBy({ it.extension }, { it.name }))
            SortOption.DEFAULT -> files
        }
    }

    fun onSortChange(option: SortOption) { _sortOption.value = option }

    fun onSearchTrigger() { _isSearchActive.value = true }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()

        if (query.isBlank()) {
            _rawSearchResults.value = emptyList()
            return
        }

        searchJob = viewModelScope.launch {
            delay(300)
            val results = docRepository.searchFiles(query)
            _rawSearchResults.value = results
        }
    }

    fun onSearchClose() {
        _isSearchActive.value = false
        _searchQuery.value = ""
        _rawSearchResults.value = emptyList()
    }

    fun toggleFavorite(file: DocFile) {
        viewModelScope.launch {
            docRepository.toggleFavorite(file)
            _rawSearchResults.value = _rawSearchResults.value.map {
                if(it.id == file.id) it.copy(isFavorite = !it.isFavorite) else it
            }
            refreshStats()
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