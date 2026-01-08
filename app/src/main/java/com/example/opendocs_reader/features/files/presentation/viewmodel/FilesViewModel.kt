package com.example.opendocs_reader.features.files.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.opendocs_reader.core.domain.model.DocFile
import com.example.opendocs_reader.core.domain.repository.DocRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FilesViewModel(private val repository: DocRepository) : ViewModel() {

    private val _allFiles = MutableStateFlow<List<DocFile>>(emptyList()) // Lista completa original
    private val _searchQuery = MutableStateFlow("")
    private val _isSearchActive = MutableStateFlow(false)

    // Lista combinada (filtrada si hay búsqueda)
    val files: StateFlow<List<DocFile>> = combine(_allFiles, _searchQuery) { files, query ->
        if (query.isBlank()) files
        else files.filter { it.name.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isSearchActive = _isSearchActive.asStateFlow()
    val searchQuery = _searchQuery.asStateFlow()

    private val _isGridMode = MutableStateFlow(false)
    val isGridMode: StateFlow<Boolean> = _isGridMode.asStateFlow()

    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedIds: StateFlow<Set<Long>> = _selectedIds.asStateFlow()

    val selectionMode: StateFlow<Boolean> = _selectedIds.map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private var currentCategory: String = "Todos"

    fun fetchFiles(category: String) {
        currentCategory = category
        viewModelScope.launch {
            repository.getFilesByCategory(category).collect {
                _allFiles.value = it
            }
        }
    }

    // --- Lógica de Búsqueda ---
    fun onSearchTrigger() { _isSearchActive.value = true }
    fun onSearchQueryChange(query: String) { _searchQuery.value = query }
    fun onSearchClose() {
        _isSearchActive.value = false
        _searchQuery.value = ""
    }

    fun toggleFavorite(file: DocFile) {
        viewModelScope.launch {
            repository.toggleFavorite(file)
            // Actualización local rápida
            if (currentCategory == "Favoritos") {
                _allFiles.update { list -> list.filter { it.id != file.id } }
            } else {
                _allFiles.update { list ->
                    list.map { if (it.id == file.id) it.copy(isFavorite = !it.isFavorite) else it }
                }
            }
        }
    }

    fun toggleViewMode() { _isGridMode.value = !_isGridMode.value }

    fun toggleSelection(fileId: Long) {
        _selectedIds.update { current -> if (current.contains(fileId)) current - fileId else current + fileId }
    }

    fun selectAll() { _selectedIds.value = files.value.map { it.id }.toSet() }
    fun clearSelection() { _selectedIds.value = emptySet() }
    fun deleteSelected() { clearSelection() }
}