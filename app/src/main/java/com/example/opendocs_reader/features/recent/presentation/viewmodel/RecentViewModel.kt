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

    private val _files = MutableStateFlow<List<DocFile>>(emptyList())
    private val _isLoading = MutableStateFlow(true)

    // Combinamos los flujos locales para emitir el estado UI
    val uiState: StateFlow<RecentUiState> = combine(_files, _isLoading) { files, isLoading ->
        RecentUiState(files, isLoading)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RecentUiState(isLoading = true)
    )

    private val _isGridMode = MutableStateFlow(false)
    val isGridMode: StateFlow<Boolean> = _isGridMode.asStateFlow()

    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedIds: StateFlow<Set<Long>> = _selectedIds.asStateFlow()

    val selectionMode: StateFlow<Boolean> = _selectedIds.map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getRecentFiles().collect { files ->
                _files.value = files
                _isLoading.value = false
            }
        }
    }

    fun toggleFavorite(file: DocFile) {
        viewModelScope.launch {
            repository.toggleFavorite(file)

            // Actualización optimista de la UI para que el corazón cambie al instante
            _files.update { currentFiles ->
                currentFiles.map {
                    if (it.id == file.id) it.copy(isFavorite = !it.isFavorite) else it
                }
            }
        }
    }

    fun toggleViewMode() { _isGridMode.value = !_isGridMode.value }

    fun toggleSelection(fileId: Long) {
        _selectedIds.update { current ->
            if (current.contains(fileId)) current - fileId else current + fileId
        }
    }

    fun selectAll() {
        _selectedIds.value = _files.value.map { it.id }.toSet()
    }

    fun clearSelection() { _selectedIds.value = emptySet() }

    fun deleteSelected() {
        clearSelection()
        // Aquí podrías implementar la lógica para eliminar del historial si el repositorio lo soporta
    }

    fun openFile(file: DocFile) {
        viewModelScope.launch {
            repository.addToRecents(file)
            // Opcional: refrescar lista para que suba arriba
            refresh()
        }
    }
}