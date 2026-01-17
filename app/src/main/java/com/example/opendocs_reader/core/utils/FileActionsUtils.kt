package com.example.opendocs_reader.core.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.navigation.NavController
import com.example.opendocs_reader.R
import com.example.opendocs_reader.core.domain.model.DocFile
import com.example.opendocs_reader.core.navigation.Screen
import java.io.File

object FileActionsUtils {

    fun openFile(navController: NavController, file: DocFile) {
        if (file.extension.equals("pdf", ignoreCase = true)) {
            val validUri = "file://${file.path}"
            navController.navigate(Screen.PdfViewer.createRoute(validUri, file.name))
        }
    }

    fun shareFile(context: Context, file: DocFile) {
        try {
            // La autoridad debe coincidir con lo declarado en el AndroidManifest.xml
            val authority = "${context.packageName}.provider"
            val uri = FileProvider.getUriForFile(context, authority, File(file.path))

            val intent = ShareCompat.IntentBuilder(context)
                .setType(file.mimeType)
                .setStream(uri)
                .setChooserTitle("Compartir ${file.name}")
                .createChooserIntent()
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun shareFiles(context: Context, files: List<DocFile>) {
        if (files.isEmpty()) return
        try {
            val authority = "${context.packageName}.provider"
            val uris = ArrayList<Uri>()

            files.forEach { file ->
                uris.add(FileProvider.getUriForFile(context, authority, File(file.path)))
            }

            val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "*/*" // Puedes ajustar esto si todos son del mismo tipo
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(intent, "Compartir ${files.size} archivos"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun createShortcut(context: Context, file: DocFile) {
        if (ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            try {
                val authority = "${context.packageName}.provider"
                val uri = FileProvider.getUriForFile(context, authority, File(file.path))

                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, file.mimeType)
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                }

                val shortcutInfo = ShortcutInfoCompat.Builder(context, file.id.toString())
                    .setShortLabel(file.name)
                    .setLongLabel(file.name)
                    .setIcon(IconCompat.createWithResource(context, R.mipmap.ic_launcher))
                    .setIntent(intent)
                    .build()

                ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}