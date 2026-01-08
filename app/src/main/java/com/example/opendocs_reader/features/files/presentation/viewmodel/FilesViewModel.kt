package com.example.opendocs_reader.features.files.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.opendocs_reader.core.domain.model.DocFile
import com.example.opendocs_reader.core.domain.repository.DocRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FilesViewModel(private val repository: DocRepository) : ViewModel() {

    private val _files = MutableStateFlow<List<DocFile>>(emptyList())
    val files: StateFlow<List<DocFile>> = _files.asStateFlow()

    private val _isGridMode = MutableStateFlow(false)
    val isGridMode: StateFlow<Boolean> = _isGridMode.asStateFlow()

    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedIds: StateFlow<Set<Long>> = _selectedIds.asStateFlow()

    val selectionMode: StateFlow<Boolean> = _selectedIds.map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Guardamos la categoría actual para saber cómo refrescar
    private var currentCategory: String = "Todos"

    fun fetchFiles(category: String) {
        currentCategory = category
        viewModelScope.launch {
            repository.getFilesByCategory(category).collect {
                _files.value = it
            }
        }
    }

    fun toggleFavorite(file: DocFile) {
        viewModelScope.launch {
            repository.toggleFavorite(file)

            // Actualización optimista de la UI
            if (currentCategory == "Favoritos") {
                // Si estamos en la pantalla de Favoritos, quitamos el item de la lista
                _files.update { list -> list.filter { it.id != file.id } }
            } else {
                // Si estamos en otra categoría, solo cambiamos el ícono
                _files.update { list ->
                    list.map { if (it.id == file.id) it.copy(isFavorite = !it.isFavorite) else it }
                }
            }
        }
    }

    // ... (resto de métodos: toggleViewMode, toggleSelection, selectAll, etc.) ...
    fun toggleViewMode() { _isGridMode.value = !_isGridMode.value }

    fun toggleSelection(fileId: Long) {
        _selectedIds.update { current ->
            if (current.contains(fileId)) current - fileId else current + fileId
        }
    }

    fun selectAll() {
        val allIds = _files.value.map { it.id }.toSet()
        _selectedIds.value = allIds
    }

    fun clearSelection() { _selectedIds.value = emptySet() }

    fun deleteSelected() {
        val idsToDelete = _selectedIds.value
        clearSelection()
    }
}