package com.example.opendocs_reader.features.pdf.presentation.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.opendocs_reader.features.pdf.presentation.viewmodel.PdfViewerViewModel
import com.example.opendocs_reader.shared.components.ViewerBottomBar
import com.example.opendocs_reader.shared.components.ViewerTopBar

@Composable
fun PdfViewerScreen(
    navController: NavController,
    fileUri: String,
    fileName: String,
    viewModel: PdfViewerViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Cargar el PDF al iniciar
    LaunchedEffect(fileUri) {
        viewModel.loadPdf(fileUri)
    }

    Scaffold(
        topBar = {
            ViewerTopBar(
                title = fileName,
                onBackClick = { navController.popBackStack() }
            )
        },
        bottomBar = {
            if (!uiState.isLoading && !uiState.isError) {
                ViewerBottomBar(
                    currentPage = uiState.currentPageIndex,
                    totalPages = uiState.totalPages,
                    onPreviousPage = { viewModel.previousPage() },
                    onNextPage = { viewModel.nextPage() }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.LightGray), // Fondo para contraste del documento
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> CircularProgressIndicator()

                uiState.isError -> Text(text = "Error: ${uiState.errorMessage}", color = Color.Red)

                uiState.currentPageBitmap != null -> {
                    // Lógica básica de Zoom y Paneo
                    var scale by remember { mutableFloatStateOf(1f) }
                    var offset by remember { mutableStateOf(Offset.Zero) }

                    // Resetear zoom al cambiar de página
                    LaunchedEffect(uiState.currentPageIndex) {
                        scale = 1f
                        offset = Offset.Zero
                    }

                    val state = rememberTransformableState { zoomChange, offsetChange, _ ->
                        scale = (scale * zoomChange).coerceIn(1f, 4f)
                        offset += offsetChange
                    }

                    Image(
                        bitmap = uiState.currentPageBitmap!!.asImageBitmap(),
                        contentDescription = "Página PDF",
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offset.x,
                                translationY = offset.y
                            )
                            .transformable(state = state)
                    )
                }
            }
        }
    }
}