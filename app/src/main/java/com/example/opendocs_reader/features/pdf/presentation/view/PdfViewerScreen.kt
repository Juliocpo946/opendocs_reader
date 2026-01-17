package com.example.opendocs_reader.features.pdf.presentation.view

import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.net.Uri
import android.view.WindowManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.opendocs_reader.core.data.repository.DocRepositoryImpl
import com.example.opendocs_reader.core.domain.model.DocFile
import com.example.opendocs_reader.core.utils.FileActionsUtils
import com.example.opendocs_reader.features.pdf.presentation.viewmodel.PdfViewerViewModel
import com.example.opendocs_reader.features.pdf.presentation.viewmodel.PdfViewerViewModelFactory
import com.example.opendocs_reader.shared.components.*
import kotlinx.coroutines.launch

@Composable
fun PdfViewerScreen(
    navController: NavController,
    fileUri: String,
    fileName: String,
    fileId: Long,
    fileMime: String,
    fileSize: Long,
    fileDate: Long
) {
    val context = LocalContext.current
    val repository = remember { DocRepositoryImpl(context) }
    val viewModel: PdfViewerViewModel = viewModel(
        factory = PdfViewerViewModelFactory(context.applicationContext as android.app.Application, repository)
    )

    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    val listState = rememberLazyListState()
    val pagerState = rememberPagerState(pageCount = { uiState.totalPages })

    var isParentScrollEnabled by remember { mutableStateOf(true) }

    var showConfigSheet by remember { mutableStateOf(false) }
    var showOptionsSheet by remember { mutableStateOf(false) }
    var showThumbnailsSheet by remember { mutableStateOf(false) }
    var showPropertiesDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(fileUri) {
        viewModel.loadPdf(fileUri)
        val cleanPath = if (fileUri.startsWith("file://")) fileUri.substring(7) else Uri.parse(fileUri).path ?: fileUri
        val docFile = DocFile(
            id = fileId, name = fileName, path = cleanPath, size = fileSize, dateAdded = fileDate, mimeType = fileMime,
            extension = fileName.substringAfterLast('.', "")
        )
        viewModel.setFileContext(docFile)
    }

    val activity = context as? Activity
    DisposableEffect(uiState.keepScreenOn) {
        if (uiState.keepScreenOn) activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        else activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose { activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }
    DisposableEffect(uiState.isLandscape) {
        activity?.requestedOrientation = if (uiState.isLandscape) ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        onDispose { activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED }
    }

    val colorFilter = if (uiState.viewerTheme == ViewerTheme.SEPIA) ColorFilter.tint(Color(0x335D4037), BlendMode.SrcAtop) else null
    val background = Color(0xFFEEEEEE)

    val currentPage by remember {
        derivedStateOf {
            if (uiState.scrollMode == ScrollMode.VERTICAL)
                listState.layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: 0
            else
                pagerState.currentPage
        }
    }

    if (showConfigSheet) {
        ViewerConfigSheet(
            onDismiss = { showConfigSheet = false },
            keepScreenOn = uiState.keepScreenOn,
            onToggleScreenOn = { viewModel.toggleScreenOn(it) },
            isLandscape = uiState.isLandscape,
            onToggleOrientation = { viewModel.toggleOrientation() },
            scrollMode = uiState.scrollMode,
            onScrollModeChange = { viewModel.setScrollMode(it) },
            currentTheme = uiState.viewerTheme,
            onThemeChange = { viewModel.setTheme(it) }
        )
    }

    if (showThumbnailsSheet) {
        ViewerThumbnailsSheet(
            totalPages = uiState.totalPages,
            onPageSelected = { page ->
                scope.launch {
                    if (uiState.scrollMode == ScrollMode.VERTICAL) listState.scrollToItem(page)
                    else pagerState.scrollToPage(page)
                }
                showThumbnailsSheet = false
            },
            onDismiss = { showThumbnailsSheet = false }
        )
    }

    if (showOptionsSheet && uiState.currentFile != null) {
        FileOptionsSheet(
            file = uiState.currentFile!!,
            onDismiss = { showOptionsSheet = false },
            onFavorite = { viewModel.toggleFavorite(); showOptionsSheet = false },
            onShare = { FileActionsUtils.shareFile(context, uiState.currentFile!!); showOptionsSheet = false },
            onRename = { showOptionsSheet = false; showRenameDialog = true },
            onDelete = { showOptionsSheet = false; showDeleteDialog = true },
            onProperties = { showOptionsSheet = false; showPropertiesDialog = true },
            onShortcut = { FileActionsUtils.createShortcut(context, uiState.currentFile!!); showOptionsSheet = false }
        )
    }

    if (showPropertiesDialog && uiState.currentFile != null) FilePropertiesDialog(file = uiState.currentFile!!, onDismiss = { showPropertiesDialog = false })
    if (showRenameDialog && uiState.currentFile != null) RenameFileDialog(currentName = uiState.currentFile!!.name, onDismiss = { showRenameDialog = false }, onConfirm = { newName -> viewModel.renameFile(newName) { showRenameDialog = false } })
    if (showDeleteDialog && uiState.currentFile != null) DeleteConfirmationDialog(count = 1, onDismiss = { showDeleteDialog = false }, onConfirm = { viewModel.deleteFile { navController.popBackStack() } })

    Scaffold(
        topBar = { ViewerTopBar(title = uiState.currentFile?.name ?: fileName, onBackClick = { navController.popBackStack() }, onSearchClick = { }) },
        bottomBar = {
            if (!uiState.isLoading && !uiState.isError) {
                ViewerBottomBar(
                    currentPage = currentPage,
                    totalPages = uiState.totalPages,
                    onConfigClick = { showConfigSheet = true },
                    onThumbnailsClick = { showThumbnailsSheet = true },
                    onPrintClick = { },
                    onPresentClick = { },
                    onMoreClick = { showOptionsSheet = true }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues).background(background), contentAlignment = Alignment.Center) {
            if (uiState.isLoading) CircularProgressIndicator()
            else if (uiState.isError) Text(text = "Error: ${uiState.errorMessage}", color = MaterialTheme.colorScheme.error)
            else {
                if (uiState.scrollMode == ScrollMode.VERTICAL) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 16.dp),
                        userScrollEnabled = isParentScrollEnabled
                    ) {
                        items(uiState.totalPages) { index ->
                            PdfPageItem(
                                pageIndex = index,
                                viewModel = viewModel,
                                colorFilter = colorFilter,
                                onZoomStateChange = { isZoomed -> isParentScrollEnabled = !isZoomed }
                            )
                        }
                    }
                } else {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        userScrollEnabled = isParentScrollEnabled
                    ) { index ->
                        PdfPageItem(
                            pageIndex = index,
                            viewModel = viewModel,
                            colorFilter = colorFilter,
                            onZoomStateChange = { isZoomed -> isParentScrollEnabled = !isZoomed }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PdfPageItem(
    pageIndex: Int,
    viewModel: PdfViewerViewModel,
    colorFilter: ColorFilter?,
    onZoomStateChange: (Boolean) -> Unit
) {
    val bitmap by produceState<Bitmap?>(initialValue = null, key1 = pageIndex) { value = viewModel.renderPage(pageIndex) }

    Box(modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp), contentAlignment = Alignment.Center) {
        if (bitmap != null) {
            ZoomableImage(
                bitmap = bitmap!!,
                colorFilter = colorFilter,
                onZoomChange = onZoomStateChange
            )
        } else {
            CircularProgressIndicator(modifier = Modifier.padding(32.dp))
        }
    }
}

@Composable
fun ZoomableImage(
    bitmap: Bitmap,
    colorFilter: ColorFilter?,
    onZoomChange: (Boolean) -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // [CORRECCIÓN] Usamos Box en lugar de BoxWithConstraints para evitar el lint warning
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    if (scale > 1f) {
                        change.consume()
                        val newOffset = offset + dragAmount
                        val containerWidth = size.width.toFloat()
                        val containerHeight = bitmap.height * (containerWidth / bitmap.width)
                        val imageWidth = containerWidth * scale
                        val imageHeight = containerHeight * scale
                        val maxOffsetX = ((imageWidth - containerWidth) / 2f).coerceAtLeast(0f)
                        val maxOffsetY = ((imageHeight - containerHeight) / 2f).coerceAtLeast(0f)

                        offset = Offset(
                            x = newOffset.x.coerceIn(-maxOffsetX, maxOffsetX),
                            y = newOffset.y.coerceIn(-maxOffsetY, maxOffsetY)
                        )
                    }
                }
            }
            .pointerInput(Unit) {
                detectTransformGestures { _, _, zoom, _ ->
                    val newScale = (scale * zoom).coerceIn(1f, 3f)
                    if (newScale != scale) {
                        scale = newScale
                        onZoomChange(scale > 1.01f)

                        val containerWidth = size.width.toFloat()
                        val containerHeight = bitmap.height * (containerWidth / bitmap.width)
                        val imageWidth = containerWidth * scale
                        val imageHeight = containerHeight * scale
                        val maxOffsetX = ((imageWidth - containerWidth) / 2f).coerceAtLeast(0f)
                        val maxOffsetY = ((imageHeight - containerHeight) / 2f).coerceAtLeast(0f)

                        offset = Offset(
                            x = offset.x.coerceIn(-maxOffsetX, maxOffsetX),
                            y = offset.y.coerceIn(-maxOffsetY, maxOffsetY)
                        )
                    }
                }
            }
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            colorFilter = colorFilter,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                }
        )
    }
}