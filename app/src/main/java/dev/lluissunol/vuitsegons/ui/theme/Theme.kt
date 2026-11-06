package dev.lluissunol.vuitsegons.ui.theme

import android.app.Activity
import android.content.res.Configuration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lluissunol.vuitsegons.core.domain.model.Settings

/**
 * Tema principal de la aplicación.
 * Envuelve MaterialTheme con colores dinámicos basados en Settings.
 */
@Composable
fun Theme(
    darkTheme: Boolean = false, // Se determina por settings o sistema
    settings: Settings = Settings.DEFAULT,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val uiMode = LocalConfiguration.current.uiMode
    val isSystemInDark = (uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

    val effectiveDark = darkTheme || isSystemInDark

    val colorScheme = AppColorScheme(
        isSystemInDarkMode = effectiveDark,
        activeColor = settings.activeColor,
        warningColor = settings.warningColor,
        expiredColor = settings.expiredColor,
        backgroundColor = settings.backgroundColor
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}

/**
 * Preview helper para desarrollo
 */
@Composable
fun PreviewTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    Theme(
        darkTheme = darkTheme,
        settings = Settings.DEFAULT,
        content = content
    )
}