package com.example.opendocs_reader.shared.theme

import android.app.Activity
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Definiciones de colores estáticas (No las toques)
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight
)

@Composable
fun Opendocs_readerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // 1. Elegimos el destino (hacia dónde vamos)
    val targetScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // 2. ANIMACIÓN MAESTRA (800ms)
    val animSpec = tween<Color>(durationMillis = 800)

    // 3. Animamos CADA color individualmente.
    // Al hacerlo aquí, MaterialTheme.colorScheme.primary devolverá el valor intermedio (animado).
    val primary by animateColorAsState(targetScheme.primary, animSpec, label = "primary")
    val onPrimary by animateColorAsState(targetScheme.onPrimary, animSpec, label = "onPrimary")
    val primaryContainer by animateColorAsState(targetScheme.primaryContainer, animSpec, label = "primaryContainer")
    val onPrimaryContainer by animateColorAsState(targetScheme.onPrimaryContainer, animSpec, label = "onPrimaryContainer")

    val background by animateColorAsState(targetScheme.background, animSpec, label = "background")
    val onBackground by animateColorAsState(targetScheme.onBackground, animSpec, label = "onBackground")

    val surface by animateColorAsState(targetScheme.surface, animSpec, label = "surface")
    val onSurface by animateColorAsState(targetScheme.onSurface, animSpec, label = "onSurface")

    // 4. Reconstruimos el esquema de colores con los valores "vivos" (animados)
    // Usamos copy para mantener los que no animamos explícitamente, pero sobrescribimos los importantes
    val animatedColorScheme = targetScheme.copy(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        background = background,
        onBackground = onBackground,
        surface = surface,
        onSurface = onSurface
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // La barra de estado seguirá el color background animado
            window.statusBarColor = animatedColorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    // 5. Entregamos el esquema ANIMADO a toda la app
    MaterialTheme(
        colorScheme = animatedColorScheme,
        typography = Typography,
        content = content
    )
}