package com.example.opendocs_reader.features.pdf.presentation.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.opendocs_reader.core.domain.model.DocFile
import com.example.opendocs_reader.core.domain.repository.DocRepository
import com.example.opendocs_reader.shared.components.ScrollMode
import com.example.opendocs_reader.shared.components.ViewerTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

data class PdfUiState(
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val errorMessage: String? = null,
    val totalPages: Int = 0,
    val currentFile: DocFile? = null,
    val viewerTheme: ViewerTheme = ViewerTheme.LIGHT,
    val scrollMode: ScrollMode = ScrollMode.VERTICAL, // Por defecto Vertical
    val keepScreenOn: Boolean = false,
    val isLandscape: Boolean = false,
    val isFavorite: Boolean = false
)

class PdfViewerViewModelFactory(
    private val application: Application,
    private val repository: DocRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PdfViewerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PdfViewerViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class PdfViewerViewModel(
    application: Application,
    private val repository: DocRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(PdfUiState())
    val uiState: StateFlow<PdfUiState> = _uiState.asStateFlow()

    private var pdfRenderer: PdfRenderer? = null
    private var fileDescriptor: ParcelFileDescriptor? = null
    private val rendererMutex = Mutex()

    fun loadPdf(fileUri: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val uri = Uri.parse(fileUri)
                val contentResolver = getApplication<Application>().contentResolver
                fileDescriptor = contentResolver.openFileDescriptor(uri, "r")

                fileDescriptor?.let { fd ->
                    pdfRenderer = PdfRenderer(fd)
                    val pageCount = pdfRenderer?.pageCount ?: 0

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        totalPages = pageCount
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, isError = true, errorMessage = e.localizedMessage)
            }
        }
    }

    suspend fun renderPage(index: Int): Bitmap? = withContext(Dispatchers.IO) {
        rendererMutex.withLock {
            if (pdfRenderer == null || index < 0 || index >= (_uiState.value.totalPages)) return@withContext null

            return@withContext try {
                pdfRenderer?.openPage(index)?.use { page ->
                    val width = page.width * 2
                    val height = page.height * 2
                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    bitmap
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    fun toggleScreenOn(enabled: Boolean) { _uiState.value = _uiState.value.copy(keepScreenOn = enabled) }
    fun toggleOrientation() { _uiState.value = _uiState.value.copy(isLandscape = !_uiState.value.isLandscape) }
    fun setTheme(theme: ViewerTheme) { _uiState.value = _uiState.value.copy(viewerTheme = theme) }
    fun setScrollMode(mode: ScrollMode) { _uiState.value = _uiState.value.copy(scrollMode = mode) }

    fun setFileContext(file: DocFile) {
        _uiState.value = _uiState.value.copy(currentFile = file, isFavorite = file.isFavorite)
    }

    fun toggleFavorite() {
        _uiState.value.currentFile?.let { file ->
            viewModelScope.launch {
                repository.toggleFavorite(file)
                _uiState.value = _uiState.value.copy(isFavorite = !file.isFavorite, currentFile = file.copy(isFavorite = !file.isFavorite))
            }
        }
    }

    fun renameFile(newName: String, onComplete: () -> Unit) {
        _uiState.value.currentFile?.let { file ->
            viewModelScope.launch {
                if (repository.renameFile(file, newName)) {
                    _uiState.value = _uiState.value.copy(currentFile = file.copy(name = newName))
                    onComplete()
                }
            }
        }
    }

    fun deleteFile(onComplete: () -> Unit) {
        _uiState.value.currentFile?.let { file ->
            viewModelScope.launch {
                if (repository.deleteFiles(listOf(file))) onComplete()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        pdfRenderer?.close()
        fileDescriptor?.close()
    }
}