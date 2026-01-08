package com.example.opendocs_reader.shared.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

// Esta variable "viajará" por toda la app llevando el color de fondo exacto del momento
val LocalAnimatedBackground = compositionLocalOf { Color.Unspecified }
val LocalAnimatedContent = compositionLocalOf { Color.Unspecified }
val LocalAnimatedContainer = compositionLocalOf { Color.Unspecified }