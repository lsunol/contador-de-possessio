package dev.lluissunol.vuitsegons.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Componente 7-Segment Display para mostrar dígitos grandes estilo reloj digital.
 * Cada dígito está compuesto por 7 segmentos (a-g) + punto decimal (dp).
 * 
 * Mapeo de segmentos:
 *    ┌─── a ───┐
 *    │         │
 *  f │         │ b
 *    │    g    │
 *    │         │
 *  e │         │ c
 *    │         │
 *    └─── d ───┘   ● dp
 */
@Composable
fun SevenSegmentDisplay(
    value: Int,                    // Valor a mostrar (0-99)
    color: Color,                  // Color de segmentos encendidos
    backgroundColor: Color = Color.Transparent, // Color de segmentos apagados (opcional)
    segmentWidth: Float = 8f,      // Se mantiene por compatibilidad (el grosor se deriva del alto)
    segmentGap: Float = 2f,        // Se mantiene por compatibilidad
    digitSpacing: Float = 12f,     // Se mantiene por compatibilidad
    animate: Boolean = true,       // Animar cambios de color
    modifier: Modifier = Modifier,
    digitRotation: Int = 0,        // Rotación en grados del CONJUNTO de dígitos (180 = boca abajo)
    sizeFraction: Float = 0.92f    // Fracción del espacio disponible que ocupa el número
) {
    val tens = (value / 10) % 10
    val units = value % 10
    val showTens = value >= 10 || tens > 0

    val animatedColor by animateColorAsState(targetValue = color, animationSpec = tween(150))
    val animatedBgColor by animateColorAsState(
        targetValue = backgroundColor.copy(alpha = 0.15f),
        animationSpec = tween(150)
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            // Única rotación: se aplica una sola vez al conjunto (orden y glifos).
            .rotate(digitRotation.toFloat()),
        contentAlignment = Alignment.Center
    ) {
        val count = if (showTens) 2 else 1
        val aspect = 0.58f          // ancho / alto de un dígito
        val spacingRatio = 0.18f    // separación / ancho de dígito
        val availW = maxWidth * sizeFraction
        val availH = maxHeight * sizeFraction
        val totalWidthPerHeight = count * aspect + (count - 1) * aspect * spacingRatio
        val digitH = minOf(availH, availW / totalWidthPerHeight)
        val digitW = digitH * aspect

        Row(
            horizontalArrangement = Arrangement.spacedBy(digitW * spacingRatio),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showTens) {
                DigitView(tens, animatedColor, animatedBgColor, Modifier.size(digitW, digitH))
            }
            DigitView(units, animatedColor, animatedBgColor, Modifier.size(digitW, digitH))
        }
    }
}

private val SEGMENT_MAP = mapOf(
    0 to booleanArrayOf(true, true, true, true, true, true, false),
    1 to booleanArrayOf(false, true, true, false, false, false, false),
    2 to booleanArrayOf(true, true, false, true, true, false, true),
    3 to booleanArrayOf(true, true, true, true, false, false, true),
    4 to booleanArrayOf(false, true, true, false, false, true, true),
    5 to booleanArrayOf(true, false, true, true, false, true, true),
    6 to booleanArrayOf(true, false, true, true, true, true, true),
    7 to booleanArrayOf(true, true, true, false, false, false, false),
    8 to booleanArrayOf(true, true, true, true, true, true, true),
    9 to booleanArrayOf(true, true, true, true, false, true, true)
)

/** Un dígito 7-segment con segmentos hexagonales de proporciones correctas. */
@Composable
private fun DigitView(
    digit: Int,
    segmentColor: Color,
    backgroundColor: Color,
    modifier: Modifier
) {
    val on = SEGMENT_MAP[digit] ?: BooleanArray(7)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val t = h * 0.10f          // grosor
        val gap = t * 0.12f        // hueco entre segmentos
        val half = t / 2f
        val left = half
        val right = w - half
        val top = half
        val bottom = h - half
        val midY = h / 2f

        fun horizontal(y: Float, lit: Boolean) {
            val x0 = left + gap + half
            val x1 = right - gap - half
            val p = Path().apply {
                moveTo(x0 - half, y)
                lineTo(x0, y - half)
                lineTo(x1, y - half)
                lineTo(x1 + half, y)
                lineTo(x1, y + half)
                lineTo(x0, y + half)
                close()
            }
            drawPath(p, if (lit) segmentColor else backgroundColor)
        }

        fun vertical(x: Float, yStart: Float, yEnd: Float, lit: Boolean) {
            val y0 = yStart + gap + half
            val y1 = yEnd - gap - half
            val p = Path().apply {
                moveTo(x, y0 - half)
                lineTo(x + half, y0)
                lineTo(x + half, y1)
                lineTo(x, y1 + half)
                lineTo(x - half, y1)
                lineTo(x - half, y0)
                close()
            }
            drawPath(p, if (lit) segmentColor else backgroundColor)
        }

        horizontal(top, on[0])                 // a
        vertical(right, top, midY, on[1])      // b
        vertical(right, midY, bottom, on[2])   // c
        horizontal(bottom, on[3])              // d
        vertical(left, midY, bottom, on[4])    // e
        vertical(left, top, midY, on[5])       // f
        horizontal(midY, on[6])                // g
    }
}

/**
 * Overlay de pausa con icono "||" parpadeante
 */
@Composable
fun PauseOverlay(
    isVisible: Boolean,
    color: Color = Color(255, 165, 0), // Ámbar
    modifier: Modifier = Modifier
) {
    if (isVisible) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f)),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            androidx.compose.material3.Text(
                text = "||",
                color = color,
                fontSize = 120.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Área táctil full-screen para detectar gestos
 */
@Composable
fun TouchArea(
    modifier: Modifier = Modifier,
    onSingleTap: () -> Unit = {},
    onDoubleTap: () -> Unit = {},
    onLongPress: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onSingleTap() },
                    onDoubleTap = { onDoubleTap() },
                    onLongPress = { onLongPress() }
                )
            }
    )
}
