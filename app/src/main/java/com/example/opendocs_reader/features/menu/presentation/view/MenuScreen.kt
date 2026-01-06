package com.example.opendocs_reader.features.menu.presentation.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.opendocs_reader.features.home.presentation.view.HomeScreen
import com.example.opendocs_reader.shared.components.OpenDocsBottomBar
import com.example.opendocs_reader.shared.components.OpenDocsTopBar

@Composable
fun MenuScreen() {
    // Estado para saber qué pestaña está seleccionada (0=Inicio, 1=Reciente, 2=Ajustes)
    var selectedItemIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = { OpenDocsTopBar() },
        bottomBar = {
            // Pasamos el estado y la función para actualizarlo al BottomBar
            OpenDocsBottomBar(
                selectedIndex = selectedItemIndex,
                onItemSelected = { index -> selectedItemIndex = index }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Cambiamos el contenido según el índice seleccionado
            when (selectedItemIndex) {
                0 -> HomeScreen() // Nuestra nueva pantalla
                1 -> PlaceholderScreen("Pantalla de Recientes")
                2 -> PlaceholderScreen("Pantalla de Ajustes")
            }
        }
    }
}

// Un composable temporal para las pantallas que aún no hacemos
@Composable
fun PlaceholderScreen(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = text)
    }
}