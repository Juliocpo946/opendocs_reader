package com.example.opendocs_reader.features.settings.presentation.view

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.opendocs_reader.core.data.repository.SettingsRepositoryImpl
import com.example.opendocs_reader.core.utils.LocaleUtils
import com.example.opendocs_reader.features.settings.presentation.viewmodel.SettingsViewModel
import com.example.opendocs_reader.features.settings.presentation.viewmodel.SettingsViewModelFactory

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val repository = remember { SettingsRepositoryImpl(context) }

    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(repository)
    )

    val state by viewModel.uiState.collectAsState()

    var showLanguageDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    if (showLanguageDialog) {
        LanguageDialog(
            currentCode = state.language,
            onDismiss = { showLanguageDialog = false },
            onLanguageSelected = { code ->
                viewModel.setLanguage(code)
                showLanguageDialog = false
            }
        )
    }

    if (showThemeDialog) {
        ThemeDialog(
            currentMode = state.themeMode,
            onDismiss = { showThemeDialog = false },
            onThemeSelected = { mode ->
                viewModel.setThemeMode(mode)
                showThemeDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        SectionTitle("General")

        // --- GESTOR DE ARCHIVOS ---
        SettingsItem(
            icon = Icons.Default.FolderOpen,
            title = "Explorar dispositivo",
            subtitle = "Buscar archivos manualmente",
            onClick = {
                try {
                    // Abre el explorador nativo para seleccionar cualquier archivo
                    val intent = Intent(Intent.ACTION_GET_CONTENT)
                    intent.type = "*/*"
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    context.startActivity(Intent.createChooser(intent, "Explorar archivos"))
                } catch (e: Exception) {
                    // Fallback a configuración si no hay app de archivos
                    val intent = Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS)
                    context.startActivity(intent)
                }
            }
        )

        SettingsItem(
            icon = Icons.Default.Share,
            title = "Compartir la app",
            onClick = {
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "¡Prueba OpenDocs Reader!")
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, "Compartir con")
                context.startActivity(shareIntent)
            }
        )

        SettingsSwitchItem(
            icon = Icons.Default.ScreenLockPortrait,
            title = "Mantener pantalla encendida",
            checked = state.keepScreenOn,
            onCheckedChange = { viewModel.toggleKeepScreenOn(it) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        SectionTitle("Visualización")

        // --- SELECTOR DE TEMA ---
        SettingsItem(
            icon = Icons.Default.DarkMode,
            title = "Tema",
            subtitle = when(state.themeMode) {
                "light" -> "Claro"
                "dark" -> "Oscuro"
                else -> "Sistema"
            },
            onClick = { showThemeDialog = true }
        )

        SettingsItem(
            icon = Icons.Default.Language,
            title = "Idiomas",
            subtitle = LocaleUtils.getLanguageName(state.language),
            onClick = { showLanguageDialog = true }
        )

        Spacer(modifier = Modifier.height(24.dp))

        SectionTitle("Ayuda")
        SettingsItem(icon = Icons.AutoMirrored.Filled.Help, title = "Preguntas frecuentes", onClick = {})
        SettingsItem(icon = Icons.Default.PrivacyTip, title = "Términos de uso", onClick = {})
        SettingsItem(icon = Icons.Default.PrivacyTip, title = "Política de privacidad", onClick = {})

        Spacer(modifier = Modifier.height(48.dp))

        VersionFooter(version = state.appVersion)

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ThemeDialog(currentMode: String, onDismiss: () -> Unit, onThemeSelected: (String) -> Unit) {
    val modes = listOf("Sistema" to "system", "Claro" to "light", "Oscuro" to "dark")
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Elegir tema") },
        text = {
            Column {
                modes.forEach { (name, mode) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onThemeSelected(mode) }.padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = (mode == currentMode), onClick = { onThemeSelected(mode) })
                        Text(text = name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun LanguageDialog(currentCode: String, onDismiss: () -> Unit, onLanguageSelected: (String) -> Unit) {
    val languages = mapOf("Español" to "es", "English" to "en", "Français" to "fr")
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleccionar idioma") },
        text = {
            Column {
                languages.forEach { (name, code) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onLanguageSelected(code) }.padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = (code == currentCode), onClick = { onLanguageSelected(code) })
                        Text(text = name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun SectionTitle(title: String) {
    Text(text = title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 8.dp, top = 8.dp))
}

@Composable
private fun SettingsItem(icon: ImageVector, title: String, subtitle: String? = null, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
        Column(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            if (subtitle != null) Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SettingsSwitchItem(icon: ImageVector, title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
        Text(text = title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f).padding(horizontal = 16.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun VersionFooter(version: String) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Versión $version", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}