package com.example.opendocs_reader.features.recent.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.opendocs_reader.core.domain.model.DocFile
import com.example.opendocs_reader.core.domain.model.SortOption
import com.example.opendocs_reader.core.domain.repository.DocRepository
import com.example.opendocs_reader.shared.viewmodel.BaseFileViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class RecentUiState(
    val files: List<DocFile> = emptyList(),
    val isLoading: Boolean = true
)

class RecentViewModel(
    private val repository: DocRepository
) : BaseFileViewModel(repository) {

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

    init { refresh() }

    fun refresh(silent: Boolean = false) {
        viewModelScope.launch {
            if (!silent) _isLoading.value = true
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

    // Sobrecargas para la UI (sin Override, inyectan el refresh y la lista de archivos)
    fun toggleFavorite(file: DocFile) = super.toggleFavorite(file) { refresh(silent = true) }
    fun renameFile(file: DocFile, newName: String) = super.renameFile(file, newName) { refresh() }
    fun deleteFile(file: DocFile) = super.deleteFile(file) { refresh() }

    // Aquí solucionamos el error: La UI llama a deleteSelected() sin args, nosotros le pasamos la lista
    fun deleteSelected() = super.deleteSelected(_allFiles.value) { refresh() }
    fun selectAll() = super.selectAll(_allFiles.value)

    fun getSelectedFiles(): List<DocFile> = _allFiles.value.filter { selectedIds.value.contains(it.id) }
}