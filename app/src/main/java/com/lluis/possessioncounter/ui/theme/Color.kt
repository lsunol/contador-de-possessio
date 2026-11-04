package com.lluis.possessioncounter.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Colores por defecto de la app (se sobreescriben por settings en runtime)
private val DefaultDarkColors = darkColorScheme(
    primary = Color(0xFF00FF00.toInt()),      // Verde neón
    primaryContainer = Color(0xFF003300.toInt()),
    secondary = Color(0xFFFFA500.toInt()),    // Ámbar
    secondaryContainer = Color(0xFF332200.toInt()),
    tertiary = Color(0xFFFF0000.toInt()),     // Rojo
    tertiaryContainer = Color(0xFF330000.toInt()),
    background = Color.Black,
    surface = Color(0xFF121212.toInt()),
    surfaceContainer = Color(0xFF1E1E1E.toInt()),
    surfaceContainerLow = Color(0xFF1A1A1A.toInt()),
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFB0B0B0.toInt()),
    outline = Color(0xFF444444.toInt()),
    outlineVariant = Color(0xFF333333.toInt()),
    error = Color(0xFFFF6B6B.toInt()),
    errorContainer = Color(0xFF330000.toInt())
)

private val DefaultLightColors = lightColorScheme(
    primary = Color(0xFF00AA00.toInt()),
    primaryContainer = Color(0xFFCCFFCC.toInt()),
    secondary = Color(0xFFCC7700.toInt()),
    secondaryContainer = Color(0xFFFFE0B2.toInt()),
    tertiary = Color(0xFFCC0000.toInt()),
    tertiaryContainer = Color(0xFFFFCCCC.toInt()),
    background = Color(0xFFF5F5F5.toInt()),
    surface = Color.White,
    surfaceContainer = Color(0xFFE8E8E8.toInt()),
    surfaceContainerLow = Color(0xFFF0F0F0.toInt()),
    onSurface = Color.Black,
    onSurfaceVariant = Color(0xFF444444.toInt()),
    outline = Color(0xFFCCCCCC.toInt()),
    outlineVariant = Color(0xFFDDDDDD.toInt()),
    error = Color(0xFFCC0000.toInt()),
    errorContainer = Color(0xFFFFCCCC.toInt())
)

/**
 * Tema de colores que se adapta a settings del usuario en runtime.
 * Los colores reales del display (active/warning/expired/background) vienen de Settings.
 */
@Composable
fun AppColorScheme(
    isSystemInDarkMode: Boolean,
    // Settings colors (ARGB Int)
    activeColor: Int = 0xFF00FF00.toInt(),
    warningColor: Int = 0xFFFFA500.toInt(),
    expiredColor: Int = 0xFFFF0000.toInt(),
    backgroundColor: Int = 0xFF000000.toInt()
): androidx.compose.material3.ColorScheme {
    val baseScheme = if (isSystemInDarkMode) DefaultDarkColors else DefaultLightColors

    val bgColor = Color(backgroundColor)
    // Compute luminance manually (standard formula for sRGB)
    val bgLuminance = (0.299 * bgColor.red + 0.587 * bgColor.green + 0.114 * bgColor.blue)

    return baseScheme.copy(
        primary = Color(activeColor),
        secondary = Color(warningColor),
        tertiary = Color(expiredColor),
        background = bgColor,
        surface = bgColor.copy(alpha = 0.9f),
        surfaceContainer = bgColor.copy(alpha = 0.8f),
        onBackground = if (bgLuminance > 0.5f) Color.Black else Color.White,
        onSurface = if (bgLuminance > 0.5f) Color.Black else Color.White
    )
}