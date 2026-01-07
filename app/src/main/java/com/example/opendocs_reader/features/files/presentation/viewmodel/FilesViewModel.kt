package com.example.opendocs_reader.features.files.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.opendocs_reader.core.domain.model.DocFile
import com.example.opendocs_reader.core.domain.repository.DocRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FilesViewModel(private val repository: DocRepository) : ViewModel() {

    private val _files = MutableStateFlow<List<DocFile>>(emptyList())
    val files: StateFlow<List<DocFile>> = _files.asStateFlow()

    private val _isGridMode = MutableStateFlow(false)
    val isGridMode: StateFlow<Boolean> = _isGridMode.asStateFlow()

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
}