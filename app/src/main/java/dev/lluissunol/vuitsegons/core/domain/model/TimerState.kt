package dev.lluissunol.vuitsegons.core.domain.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Estado sellado del temporizador.
 * Representa todos los estados posibles del contador de posesión.
 */
sealed interface TimerState {

    /** Estado inicial antes de empezar */
    data class Initial(val totalMillis: Long) : TimerState

    /** Contador corriendo */
    data class Running(
        val remainingMillis: Long,
        val totalMillis: Long,
        val warningThresholdMillis: Long
    ) : TimerState {
        /** Porcentaje de progreso (0.0 a 1.0) */
        val progress: Float
            get() = if (totalMillis > 0) 1f - (remainingMillis.toFloat() / totalMillis) else 1f

        /** Segundos restantes redondeados hacia arriba */
        val remainingSeconds: Int
            get() = ((remainingMillis + 999) / 1000).toInt()

        /** Si estamos en zona de advertencia */
        val isWarning: Boolean
            get() = remainingMillis <= warningThresholdMillis
    }

    /** Contador en pausa */
    data class Paused(
        val remainingMillis: Long,
        val totalMillis: Long
    ) : TimerState {
        val remainingSeconds: Int
            get() = ((remainingMillis + 999) / 1000).toInt()
    }

    /** Tiempo expirado (llegó a 0) */
    object Expired : TimerState
}

/** Extensión para obtener segundos restantes de forma segura */
fun TimerState.getRemainingSeconds(): Int = when (this) {
    is TimerState.Initial -> (totalMillis / 1000).toInt()
    is TimerState.Running -> remainingSeconds
    is TimerState.Paused -> remainingSeconds
    TimerState.Expired -> 0
}

/** Extensión para saber si el timer está activo (corriendo) */
val TimerState.isRunning: Boolean
    get() = this is TimerState.Running

/** Extensión para saber si está en pausa */
val TimerState.isPaused: Boolean
    get() = this is TimerState.Paused

/** Extensión para saber si expiró */
val TimerState.isExpired: Boolean
    get() = this == TimerState.Expired
