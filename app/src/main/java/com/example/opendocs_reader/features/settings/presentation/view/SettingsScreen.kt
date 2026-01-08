package com.example.opendocs_reader.features.settings.presentation.view

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.opendocs_reader.features.settings.presentation.components.LanguageSelectionSheet
import com.example.opendocs_reader.features.settings.presentation.components.PremiumBanner
import com.example.opendocs_reader.features.settings.presentation.components.SectionHeader
import com.example.opendocs_reader.features.settings.presentation.components.SettingsItem
import com.example.opendocs_reader.features.settings.presentation.components.ThemeSelectionSheet
import com.example.opendocs_reader.features.settings.presentation.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel // CAMBIO: Recibimos el ViewModel compartido
) {
    val context = LocalContext.current

    // Observamos el estado del ViewModel compartido
    val currentTheme by viewModel.currentTheme.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()

    var showThemeSheet by remember { mutableStateOf(false) }
    var showLanguageSheet by remember { mutableStateOf(false) }

    if (showThemeSheet) {
        ThemeSelectionSheet(
            currentTheme = currentTheme,
            onThemeSelected = {
                viewModel.setTheme(it) // Esto ahora actualizará el estado global
                showThemeSheet = false
            },
            onDismiss = { showThemeSheet = false }
        )
    }

    if (showLanguageSheet) {
        LanguageSelectionSheet(
            currentLanguage = currentLanguage,
            onLanguageSelected = {
                viewModel.setLanguage(it)
                showLanguageSheet = false
            },
            onDismiss = { showLanguageSheet = false }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            PremiumBanner(onClick = { })
            Spacer(modifier = Modifier.height(24.dp))
        }

        item { SectionHeader("General") }

        item {
            SettingsItem(
                icon = Icons.Default.DarkMode,
                title = "Tema",
                subtitle = currentTheme.label,
                onClick = { showThemeSheet = true }
            )
        }

        item {
            SettingsItem(
                icon = Icons.Default.Language,
                title = "Idioma",
                subtitle = currentLanguage.label,
                onClick = { showLanguageSheet = true }
            )
        }

        item { HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp)) }

        item { SectionHeader("Ayuda") }

        item {
            SettingsItem(
                icon = Icons.Default.Help,
                title = "Preguntas Frecuentes",
                subtitle = "Resuelve tus dudas",
                onClick = { }
            )
        }

        item {
            SettingsItem(
                icon = Icons.Default.Email,
                title = "Contáctanos",
                subtitle = "Reportar un problema",
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:")
                        putExtra(Intent.EXTRA_EMAIL, arrayOf("soporte@opendocs.com"))
                        putExtra(Intent.EXTRA_SUBJECT, "Soporte OpenDocs Reader")
                    }
                    try { context.startActivity(intent) } catch (e: Exception) { }
                }
            )
        }

        item { HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp)) }

        item { SectionHeader("Otros") }

        item {
            SettingsItem(
                icon = Icons.Default.Share,
                title = "Compartir App",
                subtitle = "Invita a tus amigos",
                onClick = {
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        putExtra(Intent.EXTRA_TEXT, "¡Mira esta app para leer documentos! https://play.google.com/store/apps/details?id=${context.packageName}")
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Compartir con"))
                }
            )
        }

        item {
            SettingsItem(
                icon = Icons.Default.Star,
                title = "Califícanos",
                subtitle = "Danos 5 estrellas",
                onClick = {
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${context.packageName}")))
                    } catch (e: Exception) {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")))
                    }
                }
            )
        }

        item {
            SettingsItem(
                icon = Icons.Default.PrivacyTip,
                title = "Política de Privacidad",
                subtitle = null,
                onClick = { }
            )
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "OpenDocs Reader",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Versión 1.0.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}