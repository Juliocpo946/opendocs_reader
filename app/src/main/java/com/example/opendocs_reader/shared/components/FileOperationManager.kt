package com.example.opendocs_reader.shared.components

import android.content.Context
import androidx.compose.runtime.*
import com.example.opendocs_reader.core.domain.model.DocFile
import com.example.opendocs_reader.core.utils.FileActionsUtils

@Composable
fun FileOperationManager(
    context: Context,
    selectedFile: DocFile?,
    onDismissSheet: () -> Unit,
    showBatchDelete: Boolean,
    onDismissBatchDelete: () -> Unit,
    selectedCount: Int,
    onFavorite: (DocFile) -> Unit,
    onRename: (DocFile, String) -> Unit,
    onDelete: (DocFile) -> Unit,
    onBatchDeleteConfirm: () -> Unit
) {
    var activeActionFile by remember { mutableStateOf<DocFile?>(null) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showPropertiesDialog by remember { mutableStateOf(false) }

    if (selectedFile != null) {
        FileOptionsSheet(
            file = selectedFile,
            onDismiss = onDismissSheet,
            onFavorite = {
                onFavorite(selectedFile)
                onDismissSheet()
            },
            onShare = {
                FileActionsUtils.shareFile(context, selectedFile)
                onDismissSheet()
            },
            onRename = {
                activeActionFile = selectedFile
                showRenameDialog = true
                onDismissSheet()
            },
            onDelete = {
                activeActionFile = selectedFile
                showDeleteDialog = true
                onDismissSheet()
            },
            onProperties = {
                activeActionFile = selectedFile
                showPropertiesDialog = true
                onDismissSheet()
            },
            onShortcut = {
                FileActionsUtils.createShortcut(context, selectedFile)
                onDismissSheet()
            }
        )
    }

    if (showRenameDialog && activeActionFile != null) {
        RenameFileDialog(
            currentName = activeActionFile!!.name,
            onDismiss = {
                showRenameDialog = false
                activeActionFile = null
            },
            onConfirm = { newName ->
                onRename(activeActionFile!!, newName)
                showRenameDialog = false
                activeActionFile = null
            }
        )
    }

    if (showDeleteDialog && activeActionFile != null) {
        DeleteConfirmationDialog(
            count = 1,
            onDismiss = {
                showDeleteDialog = false
                activeActionFile = null
            },
            onConfirm = {
                onDelete(activeActionFile!!)
                showDeleteDialog = false
                activeActionFile = null
            }
        )
    }

    if (showPropertiesDialog && activeActionFile != null) {
        FilePropertiesDialog(
            file = activeActionFile!!,
            onDismiss = {
                showPropertiesDialog = false
                activeActionFile = null
            }
        )
    }

    if (showBatchDelete) {
        DeleteConfirmationDialog(
            count = selectedCount,
            onDismiss = onDismissBatchDelete,
            onConfirm = {
                onBatchDeleteConfirm()
                onDismissBatchDelete()
            }
        )
    }
}