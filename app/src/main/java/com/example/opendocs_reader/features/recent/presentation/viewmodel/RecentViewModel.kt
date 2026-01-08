package com.example.opendocs_reader.features.recent.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.opendocs_reader.core.domain.model.DocFile
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

    // Filtramos localmente
    val uiState: StateFlow<RecentUiState> = combine(_allFiles, _isLoading, _searchQuery) { files, loading, query ->
        val filtered = if (query.isBlank()) files else files.filter { it.name.contains(query, ignoreCase = true) }
        RecentUiState(files = filtered, isLoading = loading)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RecentUiState(isLoading = true))

    val isSearchActive = _isSearchActive.asStateFlow()
    val searchQuery = _searchQuery.asStateFlow()

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

    // Búsqueda
    fun onSearchTrigger() { _isSearchActive.value = true }
    fun onSearchQueryChange(query: String) { _searchQuery.value = query }
    fun onSearchClose() {
        _isSearchActive.value = false
        _searchQuery.value = ""
    }

    fun toggleFavorite(file: DocFile) {
        viewModelScope.launch {
            repository.toggleFavorite(file)
            _allFiles.update { currentFiles ->
                currentFiles.map { if (it.id == file.id) it.copy(isFavorite = !it.isFavorite) else it }
            }
        }
    }

    fun toggleViewMode() { _isGridMode.value = !_isGridMode.value }
    fun toggleSelection(fileId: Long) {
        _selectedIds.update { current -> if (current.contains(fileId)) current - fileId else current + fileId }
    }
    fun selectAll() { _selectedIds.value = _allFiles.value.map { it.id }.toSet() }
    fun clearSelection() { _selectedIds.value = emptySet() }
    fun deleteSelected() { clearSelection() }
    fun openFile(file: DocFile) { viewModelScope.launch { repository.addToRecents(file); refresh() } }
}