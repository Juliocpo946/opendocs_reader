package com.example.opendocs_reader.features.recent.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.opendocs_reader.core.domain.model.DocFile
import com.example.opendocs_reader.core.domain.repository.DocRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// Estado encapsulado para manejar la UI limpiamente
data class RecentUiState(
    val files: List<DocFile> = emptyList(),
    val isLoading: Boolean = true
)

class RecentViewModel(private val repository: DocRepository) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)

    // Cacheamos el flujo de archivos. 'stateIn' mantiene el último valor.
    // WhileSubscribed(5000) mantiene los datos vivos 5 segs si la UI se destruye (ej: girar pantalla)
    val uiState: StateFlow<RecentUiState> = combine(
        repository.getRecentFiles().onEach { _isLoading.value = false }, // Al recibir datos, dejamos de cargar
        _isLoading
    ) { files, isLoading ->
        RecentUiState(files = files, isLoading = isLoading)
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

    // Ya no necesitamos un bloque init {} explícito que llame a fetch,
    // el stateIn inicia la colección automáticamente.

    fun refresh() {
        // Forzar recarga si es necesario (ej: pull to refresh)
        _isLoading.value = true
        // En una implementación con Flow reactivo desde DB (Room), esto es automático.
        // Con SharedPreferences/MediaStore manual, podríamos re-emitir el Flow.
        // Por ahora, el comportamiento reactivo básico funciona bien.
    }

    fun toggleViewMode() { _isGridMode.value = !_isGridMode.value }

    fun toggleSelection(fileId: Long) {
        _selectedIds.update { current ->
            if (current.contains(fileId)) current - fileId else current + fileId
        }
    }

    fun selectAll() {
        _selectedIds.value = uiState.value.files.map { it.id }.toSet()
    }

    fun clearSelection() { _selectedIds.value = emptySet() }

    fun deleteSelected() {
        clearSelection()
        // Aquí deberías llamar al repo para refrescar la lista
    }

    fun openFile(file: DocFile) {
        viewModelScope.launch {
            repository.addToRecents(file)
            // Nota: Como repository.getRecentFiles() en nuestra implementación actual
            // no observa cambios en realtime de SharedPreferences,
            // idealmente deberíamos tener un mecanismo de trigger para recargar.
            // Una solución rápida es recargar la pantalla al volver a ella (en el Screen).
        }
    }
}