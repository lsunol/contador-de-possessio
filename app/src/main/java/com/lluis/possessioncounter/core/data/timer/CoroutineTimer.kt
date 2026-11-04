package com.lluis.possessioncounter.core.data.timer

import android.os.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Timer preciso basado en corrutinas y SystemClock.uptimeMillis().
 * Garantiza precisión ±50ms usando tiempo monotónico del sistema.
 */
class CoroutineTimer @Inject constructor(
    private val scope: CoroutineScope
) {

    /**
     * Inicia un timer regressivo con callbacks para tick, warning y expire.
     *
     * @param durationMillis Duración total en ms
     * @param warningThresholdMillis Umbral de advertencia en ms (ej: 3000 para últimos 3s)
     * @param onTick Callback cada frame (~16ms) con ms restantes
     * @param onWarning Callback cuando se cruza el umbral de advertencia
     * @param onExpire Callback cuando llega a 0
     * @return Job que se puede cancelar para parar el timer
     */
    fun startTimer(
        durationMillis: Long,
        warningThresholdMillis: Long,
        onTick: (remainingMillis: Long) -> Unit,
        onWarning: (remainingMillis: Long) -> Unit,
        onExpire: () -> Unit
    ): Job {
        return scope.launch {
            val startTime = SystemClock.uptimeMillis()
            val endTime = startTime + durationMillis
            val warningTime = endTime - warningThresholdMillis
            var warningTriggered = false

            while (isActive) {
                val now = SystemClock.uptimeMillis()
                val remaining = maxOf(0L, endTime - now)

                // Callback de tick (UI update)
                onTick(remaining)

                // Detectar cruce de umbral de advertencia
                if (!warningTriggered && now >= warningTime) {
                    warningTriggered = true
                    onWarning(remaining)
                }

                // Verificar expiración
                if (remaining == 0L) {
                    onExpire()
                    break
                }

                // Sleep hasta el siguiente frame (~16ms = 60fps) o fin
                val nextFrame = now + 16
                val sleepUntil = minOf(nextFrame, endTime)
                val sleepTime = sleepUntil - now

                if (sleepTime > 0) {
                    delay(sleepTime)
                }
            }
        }
    }

    /**
     * Versión simplificada que emite Flow de segundos restantes
     */
    fun timerFlow(durationMillis: Long): kotlinx.coroutines.flow.Flow<Long> =
        kotlinx.coroutines.flow.callbackFlow {
            val job = startTimer(
                durationMillis = durationMillis,
                warningThresholdMillis = 0, // No warning en esta versión
                onTick = { remaining ->
                    trySend(remaining)
                },
                onWarning = {},
                onExpire = {
                    trySend(0L)
                    close()
                }
            )
            awaitClose { job.cancel() }
        }

    companion object {
        /** Formatea milisegundos a string "MM:SS" o "SS" */
        fun formatMillis(millis: Long, showMinutes: Boolean = false): String {
            val totalSeconds = (millis + 999) / 1000 // Redondeo hacia arriba
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return if (showMinutes && minutes > 0) {
                "%02d:%02d".format(minutes, seconds)
            } else {
                "%02d".format(seconds)
            }
        }

        /** Formatea solo segundos para display 7-segment (0-99) */
        fun formatSeconds(millis: Long): String {
            val seconds = ((millis + 999) / 1000).toInt().coerceIn(0, 99)
            return "%02d".format(seconds)
        }
    }
}