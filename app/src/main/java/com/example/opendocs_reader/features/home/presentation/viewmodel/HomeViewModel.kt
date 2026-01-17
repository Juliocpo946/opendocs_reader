package com.example.opendocs_reader.features.home.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.opendocs_reader.core.domain.model.DocFile
import com.example.opendocs_reader.core.domain.model.SortOption
import com.example.opendocs_reader.core.domain.model.StorageStats
import com.example.opendocs_reader.core.domain.repository.DocRepository
import com.example.opendocs_reader.core.domain.repository.StatsRepository
import com.example.opendocs_reader.shared.viewmodel.BaseFileViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

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

class HomeViewModel(
    private val statsRepository: StatsRepository,
    private val docRepository: DocRepository
) : BaseFileViewModel(docRepository) {

    private val _uiState = MutableStateFlow(StorageStats())
    val uiState: StateFlow<StorageStats> = _uiState.asStateFlow()

    private val _searchResults = MutableStateFlow<List<DocFile>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    private val _isSearchActive = MutableStateFlow(false)
    private val _sortOption = MutableStateFlow(SortOption.DATE_NEWEST)

    val searchResults: StateFlow<List<DocFile>> = combine(_searchResults, _sortOption) { files, sort ->
        sortFiles(files, sort)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isSearchActive = _isSearchActive.asStateFlow()
    val searchQuery = _searchQuery.asStateFlow()
    val sortOption = _sortOption.asStateFlow()

    init {
        viewModelScope.launch {
            statsRepository.getCachedStats().collect { stats ->
                _uiState.value = stats
            }
        }
        refreshStats()
    }

    fun refreshStats() {
        viewModelScope.launch {
            statsRepository.scanAndRefreshStats()
        }
    }

    private fun refreshSearch() {
        if (_searchQuery.value.isNotEmpty()) {
            performSearch(_searchQuery.value)
        }
        refreshStats()
    }

    fun onSearchTrigger() { _isSearchActive.value = true }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        if (query.isNotEmpty()) performSearch(query) else _searchResults.value = emptyList()
    }

    private fun performSearch(query: String) {
        viewModelScope.launch {
            _searchResults.value = docRepository.searchFiles(query)
        }
    }

    fun onSearchClose() {
        _isSearchActive.value = false
        _searchQuery.value = ""
        _searchResults.value = emptyList()
        clearSelection()
    }

    fun onSortChange(option: SortOption) { _sortOption.value = option }

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

    // Sobrecargas corregidas
    fun toggleFavorite(file: DocFile) = super.toggleFavorite(file) { refreshSearch() }
    fun renameFile(file: DocFile, newName: String) = super.renameFile(file, newName) { refreshSearch() }
    fun deleteFile(file: DocFile) = super.deleteFile(file) { refreshSearch() }

    // Aquí inyectamos searchResults en lugar de allFiles
    fun deleteSelected() = super.deleteSelected(_searchResults.value) { refreshSearch() }
    fun selectAll() = super.selectAll(_searchResults.value)

    fun getSelectedFiles(): List<DocFile> = _searchResults.value.filter { selectedIds.value.contains(it.id) }
}