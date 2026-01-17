package com.example.opendocs_reader.shared.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.opendocs_reader.core.domain.model.DocFile
import com.example.opendocs_reader.core.domain.repository.DocRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

open class BaseFileViewModel(
    private val repository: DocRepository
) : ViewModel() {

    private val _isGridMode = MutableStateFlow(false)
    val isGridMode = _isGridMode.asStateFlow()

    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedIds = _selectedIds.asStateFlow()

    val selectionMode = _selectedIds.map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun toggleViewMode() {
        _isGridMode.value = !_isGridMode.value
    }

    fun toggleSelection(fileId: Long) {
        _selectedIds.update { current ->
            if (current.contains(fileId)) current - fileId else current + fileId
        }
    }

    // Agregamos 'open' para permitir override
    open fun selectAll(files: List<DocFile>) {
        _selectedIds.value = files.map { it.id }.toSet()
    }

    fun clearSelection() {
        _selectedIds.value = emptySet()
    }

    // Agregamos 'open' para permitir override
    open fun toggleFavorite(file: DocFile, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.toggleFavorite(file)
            onComplete()
        }
    }

    // Agregamos 'open' para permitir override
    open fun renameFile(file: DocFile, newName: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.renameFile(file, newName)
            onComplete()
        }
    }

    // Agregamos 'open' para permitir override
    open fun deleteFile(file: DocFile, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteFiles(listOf(file))
            onComplete()
        }
    }

    // Agregamos 'open' para permitir override
    open fun deleteSelected(allFiles: List<DocFile>, onComplete: () -> Unit = {}) {
        val selected = allFiles.filter { _selectedIds.value.contains(it.id) }
        viewModelScope.launch {
            if (repository.deleteFiles(selected)) {
                clearSelection()
                onComplete()
            }
        }
    }
}