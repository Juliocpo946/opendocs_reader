package com.example.opendocs_reader.shared.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun OpenDocsBottomBar(
    selectedIndex: Int,           // Recibe el índice actual
    onItemSelected: (Int) -> Unit // Avisa al padre cuando tocan uno
) {
    val items = listOf("Inicio", "Reciente", "Ajustes")
    val icons = listOf(
        Icons.Default.Home,
        Icons.Default.DateRange,
        Icons.Default.Settings
    )

    NavigationBar {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = { Icon(icons[index], contentDescription = item) },
                label = { Text(item) },
                selected = selectedIndex == index, // Usa el parámetro recibido
                onClick = { onItemSelected(index) } // Llama a la función recibida
            )
        }
    }
}