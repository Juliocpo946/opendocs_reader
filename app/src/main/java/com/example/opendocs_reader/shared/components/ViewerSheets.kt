package com.example.opendocs_reader.shared.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

enum class ViewerTheme { LIGHT, SEPIA }
enum class ScrollMode { VERTICAL, HORIZONTAL } // Restaurado

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewerConfigSheet(
    onDismiss: () -> Unit,
    keepScreenOn: Boolean,
    onToggleScreenOn: (Boolean) -> Unit,
    isLandscape: Boolean,
    onToggleOrientation: () -> Unit,
    scrollMode: ScrollMode,
    onScrollModeChange: (ScrollMode) -> Unit,
    currentTheme: ViewerTheme,
    onThemeChange: (ViewerTheme) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .navigationBarsPadding()
        ) {
            Text(
                text = "Configuración de Lectura",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            SwitchItem(label = "Pantalla siempre encendida", checked = keepScreenOn, onCheckedChange = onToggleScreenOn)

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Orientación
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleOrientation)
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.ScreenRotation, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Rotar Pantalla", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = if (isLandscape) "Horizontal" else "Vertical",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Modo de Scroll (Vertical vs Horizontal)
            Text("Dirección de desplazamiento", style = MaterialTheme.typography.labelLarge)
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SelectableChip(
                    label = "Vertical",
                    selected = scrollMode == ScrollMode.VERTICAL,
                    onClick = { onScrollModeChange(ScrollMode.VERTICAL) },
                    modifier = Modifier.weight(1f)
                )
                SelectableChip(
                    label = "Horizontal",
                    selected = scrollMode == ScrollMode.HORIZONTAL,
                    onClick = { onScrollModeChange(ScrollMode.HORIZONTAL) },
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Tema
            Text("Filtro de lectura", style = MaterialTheme.typography.labelLarge)
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ThemeCircle(Color.White, "Normal", currentTheme == ViewerTheme.LIGHT) { onThemeChange(ViewerTheme.LIGHT) }
                ThemeCircle(Color(0xFFF4ECD8), "Sepia", currentTheme == ViewerTheme.SEPIA) { onThemeChange(ViewerTheme.SEPIA) }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewerThumbnailsSheet(
    totalPages: Int,
    onPageSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxHeight(0.6f)
                .navigationBarsPadding()
        ) {
            Text(
                text = "Ir a página ($totalPages)",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 80.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(totalPages) { pageIndex ->
                    Box(
                        modifier = Modifier
                            .aspectRatio(0.7f)
                            .background(Color.LightGray, RoundedCornerShape(4.dp))
                            .clickable { onPageSelected(pageIndex) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${pageIndex + 1}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// Componentes auxiliares
@Composable
private fun SwitchItem(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SelectableChip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center) },
        modifier = modifier
    )
}

@Composable
private fun ThemeCircle(color: Color, name: String, selected: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color)
                .clickable(onClick = onClick)
                .then(if (selected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape) else Modifier.border(1.dp, Color.Gray, CircleShape))
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(name, style = MaterialTheme.typography.labelSmall)
    }
}