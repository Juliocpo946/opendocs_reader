package com.example.opendocs_reader.features.files.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.opendocs_reader.core.domain.model.DocFile
import com.example.opendocs_reader.core.domain.model.SortOption
import com.example.opendocs_reader.core.domain.repository.DocRepository
import com.example.opendocs_reader.shared.viewmodel.BaseFileViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FilesViewModel(
    private val repository: DocRepository
) : BaseFileViewModel(repository) {

    private val _allFiles = MutableStateFlow<List<DocFile>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    private val _isSearchActive = MutableStateFlow(false)
    private val _sortOption = MutableStateFlow(SortOption.DATE_NEWEST)

    val files: StateFlow<List<DocFile>> = combine(_allFiles, _searchQuery, _sortOption) { files, query, sort ->
        val filtered = if (query.isBlank()) files else files.filter { it.name.contains(query, ignoreCase = true) }
        sortFiles(filtered, sort)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isSearchActive = _isSearchActive.asStateFlow()
    val searchQuery = _searchQuery.asStateFlow()
    val sortOption = _sortOption.asStateFlow()

    private var currentCategory: String = "Todos"

    fun fetchFiles(category: String) {
        currentCategory = category
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            repository.getFilesByCategory(currentCategory).collect {
                _allFiles.value = it
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

    // Sobrecargas corregidas
    fun toggleFavorite(file: DocFile) = super.toggleFavorite(file) { refresh() }
    fun renameFile(file: DocFile, newName: String) = super.renameFile(file, newName) { refresh() }
    fun deleteFile(file: DocFile) = super.deleteFile(file) { refresh() }
    fun deleteSelected() = super.deleteSelected(_allFiles.value) { refresh() }
    fun selectAll() = super.selectAll(_allFiles.value)

    fun getSelectedFiles(): List<DocFile> = _allFiles.value.filter { selectedIds.value.contains(it.id) }
}