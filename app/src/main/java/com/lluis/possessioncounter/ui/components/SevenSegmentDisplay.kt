package com.lluis.possessioncounter.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
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
    segmentWidth: Float = 8f,      // Grosor de segmentos en dp
    segmentGap: Float = 2f,        // Espacio entre segmentos en dp
    digitSpacing: Float = 12f,     // Espacio entre dígitos en dp
    animate: Boolean = true,       // Animar cambios de color
    modifier: Modifier = Modifier,
    forceReverse: Boolean = false  // Rotar 180° para uso boca abajo
) {
    // Extraer dígitos (decenas y unidades)
    val tens = (value / 10) % 10
    val units = value % 10

    // Color animado para transiciones suaves
    val animatedColor by animateColorAsState(
        targetValue = color,
        animationSpec = tween(150)
    )

    // Color de fondo animado
    val animatedBgColor by animateColorAsState(
        targetValue = backgroundColor.copy(alpha = 0.15f),
        animationSpec = tween(150)
    )

    Row(
        modifier = modifier
            .fillMaxSize()
            .rotate(if (forceReverse) 180f else 0f),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Dígito de decenas (ocultar si 0 y valor < 10)
        if (value >= 10 || tens > 0) {
            DigitView(
                digit = tens,
                segmentColor = animatedColor,
                backgroundColor = animatedBgColor,
                segmentWidth = segmentWidth,
                segmentGap = segmentGap,
                animate = animate
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(digitSpacing.dp))
        }

        // Dígito de unidades
        DigitView(
            digit = units,
            segmentColor = animatedColor,
            backgroundColor = animatedBgColor,
            segmentWidth = segmentWidth,
            segmentGap = segmentGap,
            animate = animate
        )
    }
}

/**
 * Vista de un solo dígito 7-segment
 */
@Composable
private fun DigitView(
    digit: Int,
    segmentColor: Color,
    backgroundColor: Color,
    segmentWidth: Float,
    segmentGap: Float,
    animate: Boolean
) {
    // Definición de qué segmentos están encendidos para cada dígito
    // Segmentos: a=0, b=1, c=2, d=3, e=4, f=5, g=6
    val segmentMap = remember {
        mapOf(
            0 to booleanArrayOf(true, true, true, true, true, true, false),  // 0: a,b,c,d,e,f
            1 to booleanArrayOf(false, true, true, false, false, false, false), // 1: b,c
            2 to booleanArrayOf(true, true, false, true, true, false, true),   // 2: a,b,g,e,d
            3 to booleanArrayOf(true, true, true, true, false, false, true),   // 3: a,b,c,d,g
            4 to booleanArrayOf(false, true, true, false, false, true, true),  // 4: f,g,b,c
            5 to booleanArrayOf(true, false, true, true, false, true, true),   // 5: a,f,g,c,d
            6 to booleanArrayOf(true, false, true, true, true, true, true),    // 6: a,f,g,e,c,d
            7 to booleanArrayOf(true, true, true, false, false, false, false), // 7: a,b,c
            8 to booleanArrayOf(true, true, true, true, true, true, true),     // 8: all
            9 to booleanArrayOf(true, true, true, true, false, true, true)     // 9: a,b,c,d,f,g
        )
    }

    val segmentsOn = segmentMap[digit] ?: booleanArrayOf(false, false, false, false, false, false, false)

    // Dimensiones del dígito (en dp, se convierten a px en Canvas)
    val digitWidth = (segmentWidth * 2 + segmentGap * 2 + 40).dp  // Ancho aprox
    val digitHeight = (segmentWidth * 2 + segmentGap * 2 + 70).dp // Alto aprox

    Box(
        modifier = Modifier
            .size(digitWidth, digitHeight)
            .padding(4.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val sw = segmentWidth.dp.toPx()
            val gap = segmentGap.dp.toPx()

            // Coordenadas clave para los segmentos
            val left = gap
            val right = width - gap
            val top = gap
            val bottom = height - gap
            val midY = height / 2f
            val midX = width / 2f

            // Función para dibujar un segmento
            fun drawSegment(pathBuilder: Path.() -> Unit, isOn: Boolean) {
                drawPath(
                    path = Path().apply(pathBuilder),
                    color = if (isOn) segmentColor else backgroundColor,
                    style = Stroke(width = sw, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }

            // Segmento A (superior horizontal)
            drawSegment({
                moveTo(left + sw, top)
                lineTo(right - sw, top)
            }, segmentsOn[0])

            // Segmento B (superior derecho vertical)
            drawSegment({
                moveTo(right, top + sw)
                lineTo(right, midY - gap)
            }, segmentsOn[1])

            // Segmento C (inferior derecho vertical)
            drawSegment({
                moveTo(right, midY + gap)
                lineTo(right, bottom - sw)
            }, segmentsOn[2])

            // Segmento D (inferior horizontal)
            drawSegment({
                moveTo(right - sw, bottom)
                lineTo(left + sw, bottom)
            }, segmentsOn[3])

            // Segmento E (inferior izquierdo vertical)
            drawSegment({
                moveTo(left, midY + gap)
                lineTo(left, bottom - sw)
            }, segmentsOn[4])

            // Segmento F (superior izquierdo vertical)
            drawSegment({
                moveTo(left, top + sw)
                lineTo(left, midY - gap)
            }, segmentsOn[5])

            // Segmento G (central horizontal)
            drawSegment({
                moveTo(left + sw, midY)
                lineTo(right - sw, midY)
            }, segmentsOn[6])

            // Punto decimal (opcional, no usado en contador)
            // drawSegment({
            //     addCircle(right + gap, bottom + gap, sw / 2f, Path.Direction.CW)
            // }, false, android.graphics.Paint.Style.FILL)
        }
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
