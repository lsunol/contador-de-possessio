package com.lluis.possessioncounter.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lluis.possessioncounter.PossessionCounterApplication
import com.lluis.possessioncounter.core.domain.model.TimerState
import com.lluis.possessioncounter.core.domain.model.getRemainingSeconds
import com.lluis.possessioncounter.ui.components.PauseOverlay
import com.lluis.possessioncounter.ui.components.SevenSegmentDisplay
import com.lluis.possessioncounter.ui.settings.SettingsActivity
import com.lluis.possessioncounter.ui.theme.Theme

private fun Int.toColor() = Color(this)

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        (application as PossessionCounterApplication).container.viewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Edge-to-edge para aprovechar toda la pantalla
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        hideSystemBars()
        setContent {
            Theme {
                MainScreen(viewModel = viewModel)
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemBars()
    }

    // Modo inmersivo: la app ocupa toda la pantalla; las barras reaparecen un momento al deslizar desde el borde
    private fun hideSystemBars() {
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.systemBarsBehavior =
            androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_VOLUME_DOWN -> {
                // Delegar al ViewModel según configuración
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }
}

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier
) {
    // Observar estado del timer (respaldado por mutableStateOf en el ViewModel)
    val uiState = viewModel.uiState

    val context = LocalContext.current

    // Pausar al salir de la pantalla (abrir ajustes) o de la app
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner, viewModel) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_PAUSE) viewModel.pauseIfRunning()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Observar settings para colores
    val settings by viewModel.settingsFlow.collectAsStateWithLifecycle(
        initialValue = com.lluis.possessioncounter.core.domain.model.Settings.DEFAULT
    )

    // Determinar color actual según estado
    val currentColor = when (uiState) {
        is TimerState.Running -> if (uiState.isWarning) settings.warningColor.toColor() else settings.activeColor.toColor()
        is TimerState.Paused -> settings.warningColor.toColor() // Ámbar en pausa
        TimerState.Expired -> settings.expiredColor.toColor()
        is TimerState.Initial -> settings.activeColor.toColor()
    }

    // Determinar si mostrar overlay de pausa
    val showPauseOverlay = uiState is TimerState.Paused

    // Valor a mostrar (segundos restantes)
    val displayValue = uiState.getRemainingSeconds()

    Surface(
        modifier = modifier
            .fillMaxSize()
            .background(settings.backgroundColor.toColor())
            .pointerInput(Unit) {
                // Detectar gestos en toda la pantalla
                detectTapGestures(
                    onTap = { viewModel.onSingleTap() },
                    onDoubleTap = { viewModel.onDoubleTap() },
                    onLongPress = { viewModel.onLongPress() }
                )
            },
        color = settings.backgroundColor.toColor()
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Display principal 7-segment
            SevenSegmentDisplay(
                value = displayValue,
                color = currentColor,
                backgroundColor = settings.backgroundColor.toColor(),
                segmentWidth = 18f * settings.digitSize.scale,
                segmentGap = 3f * settings.digitSize.scale,
                digitSpacing = 16f * settings.digitSize.scale,
                animate = true,
                digitRotation = if (settings.forceReversePortrait) 180 else 0,
                sizeFraction = 0.80f + 0.12f * (settings.digitSize.scale / 1.2f)
            )

            // Overlay de pausa
            PauseOverlay(
                isVisible = showPauseOverlay,
                color = settings.warningColor.toColor()
            )

            // Botón de settings (esquina, discreto)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(top = 12.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                IconButton(
                    onClick = { context.startActivity(Intent(context, SettingsActivity::class.java)) },
                    modifier = Modifier
                        .size(48.dp)
                        .alpha(1.0f)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Settings,
                        contentDescription = "Ajustos",
                        tint = currentColor
                    )
                }
            }
        }
    }
}