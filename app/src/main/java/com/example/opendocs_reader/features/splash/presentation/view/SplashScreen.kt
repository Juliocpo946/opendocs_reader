package com.example.opendocs_reader.features.splash.presentation.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.opendocs_reader.core.navigation.Screen
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    // Efecto para esperar y navegar
    LaunchedEffect(key1 = true) {
        delay(2000) // Espera 2 segundos (2000 ms)
        navController.navigate(Screen.Menu.route) {
            // Esto borra la Splash del historial para que al dar "Atrás" no vuelvas a ella
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }

    // UI: "Hola esto es un splash"
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Hola, esto es un Splash",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}