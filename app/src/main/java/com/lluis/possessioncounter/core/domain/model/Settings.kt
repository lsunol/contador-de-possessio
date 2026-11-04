package com.lluis.possessioncounter.core.domain.model

import android.os.Parcel
import android.os.Parcelable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.parcelize.Parcelize
import kotlin.math.min

/**
 * Tipo de sonido disponible en la aplicación
 */
enum class SoundType(val resourceName: String, val displayName: String) {
    // Beeps de advertencia
    BEEP_CLASSIC("beep_classic", "Clásico"),
    BEEP_SOFT("beep_soft", "Suave"),
    BEEP_DIGITAL("beep_digital", "Digital"),

    // Silbatos finales
    WHISTLE_CLASSIC("whistle_classic", "Clásico"),
    WHISTLE_SHORT("whistle_short", "Corto"),
    WHISTLE_LONG("whistle_long", "Largo"),

    // Sonido de reinicio (tick)
    TICK_RESET("tick_reset", "Click")
}

/**
 * Patrón de vibración predefinido
 */
enum class VibrationPattern(val pattern: LongArray, val displayName: String) {
    STANDARD(longArrayOf(0, 50), "Estándar"),
    DOUBLE(longArrayOf(0, 50, 100, 50), "Doble"),
    TRIPLE(longArrayOf(0, 30, 60, 30, 90, 30), "Triple"),
    LONG(longArrayOf(0, 200), "Largo"),
    HEARTBEAT(longArrayOf(0, 100, 100, 100, 300), "Latido")
}

/**
 * Tamaño de dígitos del display
 */
enum class DigitSize(val scale: Float, val displayName: String) {
    SMALL(0.6f, "Pequeño"),
    MEDIUM(0.8f, "Mediano"),
    LARGE(1.0f, "Grande"),
    FULL_SCREEN(1.2f, "Pantalla completa")
}

/**
 * Gestos para reiniciar el contador
 */
enum class ResetGesture(val displayName: String) {
    SINGLE_TAP("Tap simple"),
    DOUBLE_TAP("Doble tap"),
    VOLUME_BUTTON("Botón volumen"),
    POWER_BUTTON("Botón encendido")
}

/**
 * Gestos para pausar el contador
 */
enum class PauseGesture(val displayName: String) {
    DOUBLE_TAP("Doble tap"),
    LONG_PRESS("Pulsación larga"),
    THREE_TAP("Triple tap"),
    VOLUME_DOWN("Volumen -")
}

/**
 * Configuración completa de la aplicación
 */
@Parcelize
data class Settings(
    // ===== TIEMPO =====
    /** Duración base en segundos (1-60) */
    val baseDurationSeconds: Int = 8,
    /** Umbral de advertencia en segundos (1-30) */
    val warningThresholdSeconds: Int = 3,

    // ===== AUDIO =====
    /** Volumen maestro (0.0 - 1.0) */
    val masterVolume: Float = 1.0f,
    /** Reproducir tick al reiniciar */
    val playTickOnReset: Boolean = true,
    /** Reproducir beeps de advertencia */
    val playWarningBeeps: Boolean = true,
    /** Reproducir silbato final */
    val playFinalWhistle: Boolean = true,
    /** Tipo de sonido para beeps de advertencia */
    val warningSoundType: SoundType = SoundType.BEEP_CLASSIC,
    /** Tipo de sonido para silbato final */
    val whistleSoundType: SoundType = SoundType.WHISTLE_CLASSIC,
    /** Ignorar modo No Molestar / Silencio */
    val overrideDoNotDisturb: Boolean = false,

    // ===== HÁPTICOS =====
    /** Vibrar al reiniciar */
    val vibrateOnReset: Boolean = true,
    /** Vibrar en beeps de advertencia */
    val vibrateOnWarning: Boolean = true,
    /** Vibrar en silbato final (fuerte) */
    val vibrateOnExpire: Boolean = true,
    /** Patrón de vibración */
    val vibrationPattern: VibrationPattern = VibrationPattern.STANDARD,

    // ===== VISUAL =====
    /** Color dígitos activos (ARGB Int) */
    val activeColor: Int = Color.Green.toArgb(),
    /** Color dígitos en advertencia (ARGB Int) */
    val warningColor: Int = Color(255, 165, 0).toArgb(), // Ámbar
    /** Color dígitos expirado (ARGB Int) */
    val expiredColor: Int = Color.Red.toArgb(),
    /** Color fondo (ARGB Int) */
    val backgroundColor: Int = Color.Black.toArgb(),
    /** Mantener pantalla encendida (WakeLock) */
    val keepScreenOn: Boolean = true,
    /** Tamaño de dígitos */
    val digitSize: DigitSize = DigitSize.FULL_SCREEN,
    /** Forzar orientación 180° (boca abajo) */
    val forceReversePortrait: Boolean = true,

    // ===== COMPORTAMIENTO =====
    /** Gesto para reiniciar */
    val resetGesture: ResetGesture = ResetGesture.SINGLE_TAP,
    /** Gesto para pausar */
    val pauseGesture: PauseGesture = PauseGesture.DOUBLE_TAP,
    /** Auto-iniciar al abrir app */
    val autoStart: Boolean = true
) : Parcelable {

    /** Duración base en milisegundos */
    val baseDurationMillis: Long
        get() = baseDurationSeconds * 1000L

    /** Umbral de advertencia en milisegundos */
    val warningThresholdMillis: Long
        get() = warningThresholdSeconds * 1000L

    /** Validar rangos y corregir si es necesario */
    fun validated(): Settings {
        return copy(
            baseDurationSeconds = baseDurationSeconds.coerceIn(1, 60),
            warningThresholdSeconds = warningThresholdSeconds.coerceIn(1, min(30, baseDurationSeconds - 1).coerceAtLeast(1)),
            masterVolume = masterVolume.coerceIn(0.0f, 1.0f)
        )
    }

    /** Configuración por defecto para reset */
    companion object {
        val DEFAULT = Settings()
    }
}