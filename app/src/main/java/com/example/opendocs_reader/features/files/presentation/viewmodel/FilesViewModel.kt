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

    // --- Lógica de Selección ---
    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedIds: StateFlow<Set<Long>> = _selectedIds.asStateFlow()

    // Se activa el modo selección si hay al menos un item seleccionado
    val selectionMode: StateFlow<Boolean> = _selectedIds.map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun fetchFiles(category: String) {
        viewModelScope.launch {
            repository.getFilesByCategory(category).collect {
                _files.value = it
            }
        }
    }

    fun toggleViewMode() {
        _isGridMode.value = !_isGridMode.value
    }

    // Métodos de selección
    fun toggleSelection(fileId: Long) {
        _selectedIds.update { current ->
            if (current.contains(fileId)) current - fileId else current + fileId
        }
    }

    fun selectAll() {
        val allIds = _files.value.map { it.id }.toSet()
        _selectedIds.value = allIds
    }

    fun clearSelection() {
        _selectedIds.value = emptySet()
    }

    fun deleteSelected() {
        // Aquí iría la llamada al repositorio para borrar los archivos reales
        // Por ahora solo limpiamos la selección visualmente
        val idsToDelete = _selectedIds.value
        // repository.deleteFiles(idsToDelete)
        clearSelection()
        // Refrescar lista...
    }
}