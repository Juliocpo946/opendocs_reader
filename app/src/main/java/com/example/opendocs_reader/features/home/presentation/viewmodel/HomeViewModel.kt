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

    // Selección en Home (para resultados de búsqueda)
    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedIds: StateFlow<Set<Long>> = _selectedIds.asStateFlow()
    val selectionMode: StateFlow<Boolean> = _selectedIds.map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

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
        clearSelection()
    }

    fun toggleFavorite(file: DocFile) {
        viewModelScope.launch {
            docRepository.toggleFavorite(file)
            // Actualizar localmente la lista de búsqueda
            _rawSearchResults.value = _rawSearchResults.value.map {
                if(it.id == file.id) it.copy(isFavorite = !it.isFavorite) else it
            }
            refreshStats()
        }
    }

    // Funciones de Gestión de Archivos (Renombrar, Borrar)
    fun renameFile(file: DocFile, newName: String) {
        viewModelScope.launch {
            val success = docRepository.renameFile(file, newName)
            if (success) {
                // Actualizar lista localmente
                _rawSearchResults.value = _rawSearchResults.value.map {
                    if (it.id == file.id) it.copy(name = newName) else it
                }
                refreshStats()
            }
        }
    }

    fun deleteFile(file: DocFile) {
        viewModelScope.launch {
            val success = docRepository.deleteFiles(listOf(file))
            if (success) {
                _rawSearchResults.value = _rawSearchResults.value.filter { it.id != file.id }
                refreshStats()
            }
        }
    }

    fun deleteSelected() {
        val selected = _rawSearchResults.value.filter { _selectedIds.value.contains(it.id) }
        viewModelScope.launch {
            val success = docRepository.deleteFiles(selected)
            if (success) {
                _rawSearchResults.value = _rawSearchResults.value.filter { !selected.contains(it) }
                clearSelection()
                refreshStats()
            }
        }
    }

    // Selección
    fun toggleSelection(fileId: Long) {
        _selectedIds.update { current -> if (current.contains(fileId)) current - fileId else current + fileId }
    }
    fun selectAll() {
        _selectedIds.value = searchResults.value.map { it.id }.toSet()
    }
    fun clearSelection() { _selectedIds.value = emptySet() }
    fun getSelectedFiles(): List<DocFile> = _rawSearchResults.value.filter { _selectedIds.value.contains(it.id) }
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