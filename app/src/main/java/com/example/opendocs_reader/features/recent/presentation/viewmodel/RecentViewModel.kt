package com.example.opendocs_reader.features.recent.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.opendocs_reader.core.domain.model.DocFile
import com.example.opendocs_reader.core.domain.model.SortOption
import com.example.opendocs_reader.core.domain.repository.DocRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class RecentUiState(
    val files: List<DocFile> = emptyList(),
    val isLoading: Boolean = true
)

class RecentViewModel(private val repository: DocRepository) : ViewModel() {

    private val _allFiles = MutableStateFlow<List<DocFile>>(emptyList())
    private val _isLoading = MutableStateFlow(true)
    private val _searchQuery = MutableStateFlow("")
    private val _isSearchActive = MutableStateFlow(false)
    private val _sortOption = MutableStateFlow(SortOption.DEFAULT)

    val uiState: StateFlow<RecentUiState> = combine(_allFiles, _isLoading, _searchQuery, _sortOption) { files, loading, query, sort ->
        val filtered = if (query.isBlank()) files else files.filter { it.name.contains(query, ignoreCase = true) }
        val sorted = sortFiles(filtered, sort)
        RecentUiState(files = sorted, isLoading = loading)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RecentUiState(isLoading = true))

    val isSearchActive = _isSearchActive.asStateFlow()
    val searchQuery = _searchQuery.asStateFlow()
    val sortOption = _sortOption.asStateFlow()

    private val _isGridMode = MutableStateFlow(false)
    val isGridMode: StateFlow<Boolean> = _isGridMode.asStateFlow()

    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedIds: StateFlow<Set<Long>> = _selectedIds.asStateFlow()

    val selectionMode: StateFlow<Boolean> = _selectedIds.map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getRecentFiles().collect { files ->
                _allFiles.value = files
                _isLoading.value = false
            }
        }
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
    fun onSearchQueryChange(query: String) { _searchQuery.value = query }
    fun onSearchClose() {
        _isSearchActive.value = false
        _searchQuery.value = ""
        clearSelection()
    }

    fun toggleFavorite(file: DocFile) {
        viewModelScope.launch {
            repository.toggleFavorite(file)
            refresh()
        }
    }

    // Acciones de Archivo
    fun renameFile(file: DocFile, newName: String) {
        viewModelScope.launch {
            if (repository.renameFile(file, newName)) refresh()
        }
    }

    fun deleteFile(file: DocFile) {
        viewModelScope.launch {
            if (repository.deleteFiles(listOf(file))) refresh()
        }
    }

    fun deleteSelected() {
        val selected = _allFiles.value.filter { _selectedIds.value.contains(it.id) }
        viewModelScope.launch {
            if (repository.deleteFiles(selected)) {
                clearSelection()
                refresh()
            }
        }
    }

    fun toggleViewMode() { _isGridMode.value = !_isGridMode.value }
    fun toggleSelection(fileId: Long) {
        _selectedIds.update { current -> if (current.contains(fileId)) current - fileId else current + fileId }
    }
    fun selectAll() { _selectedIds.value = _allFiles.value.map { it.id }.toSet() }
    fun clearSelection() { _selectedIds.value = emptySet() }

    fun getSelectedFiles(): List<DocFile> = _allFiles.value.filter { _selectedIds.value.contains(it.id) }

    fun openFile(file: DocFile) { viewModelScope.launch { repository.addToRecents(file); refresh() } }
}