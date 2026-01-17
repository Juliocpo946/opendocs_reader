package com.example.opendocs_reader.features.pdf.presentation.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class PdfUiState(
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val errorMessage: String? = null,
    val totalPages: Int = 0,
    val currentPageIndex: Int = 0,
    val currentPageBitmap: Bitmap? = null
)

class PdfViewerViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(PdfUiState())
    val uiState: StateFlow<PdfUiState> = _uiState.asStateFlow()

    private var pdfRenderer: PdfRenderer? = null
    private var fileDescriptor: ParcelFileDescriptor? = null

    fun loadPdf(fileUri: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val uri = Uri.parse(fileUri)
                val contentResolver = getApplication<Application>().contentResolver

                // Abrimos el descriptor del archivo de forma segura
                fileDescriptor = contentResolver.openFileDescriptor(uri, "r")

                fileDescriptor?.let { fd ->
                    pdfRenderer = PdfRenderer(fd)
                    val pageCount = pdfRenderer?.pageCount ?: 0

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        totalPages = pageCount,
                        currentPageIndex = 0
                    )

                    // Renderizar la primera página
                    renderPage(0)
                } ?: run {
                    _uiState.value = _uiState.value.copy(isLoading = false, isError = true, errorMessage = "No se pudo abrir el archivo")
                }

            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(isLoading = false, isError = true, errorMessage = e.localizedMessage)
            }
        }
    }

    fun nextPage() {
        val currentState = _uiState.value
        if (currentState.currentPageIndex < currentState.totalPages - 1) {
            renderPage(currentState.currentPageIndex + 1)
        }
    }

    fun previousPage() {
        val currentState = _uiState.value
        if (currentState.currentPageIndex > 0) {
            renderPage(currentState.currentPageIndex - 1)
        }
    }

    private fun renderPage(index: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                pdfRenderer?.openPage(index)?.use { page ->
                    // Crear bitmap con alta calidad (se puede ajustar la escala para zoom)
                    val bitmap = Bitmap.createBitmap(
                        page.width * 2, // Escala x2 para mejor definición
                        page.height * 2,
                        Bitmap.Config.ARGB_8888
                    )

                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                    _uiState.value = _uiState.value.copy(
                        currentPageIndex = index,
                        currentPageBitmap = bitmap
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            pdfRenderer?.close()
            fileDescriptor?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}