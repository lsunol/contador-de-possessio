package com.lluis.possessioncounter.util

import android.content.Context
import android.os.PowerManager

/**
 * Gestor de WakeLock para mantener la pantalla encendida durante el partido.
 * Usa PARTIAL_WAKE_LOCK (mantiene CPU activa, pantalla puede apagarse pero app sigue corriendo)
 * Para mantener pantalla realmente encendida, combinar con FLAG_KEEP_SCREEN_ON en Activity.
 */
class WakeLockManager(
    private val context: Context
) {

    private var wakeLock: PowerManager.WakeLock? = null
    private val powerManager: PowerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

    /** Adquiere wake lock parcial */
    fun acquire() {
        if (wakeLock?.isHeld == true) return

        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "PossessionCounter::WakeLock"
        ).apply {
            acquire()
        }
    }

    /** Libera wake lock */
    fun release() {
        wakeLock?.run {
            if (isHeld) release()
            wakeLock = null
        }
    }

    /** Alterna estado */
    fun toggle(): Boolean {
        return if (isHeld) {
            release()
            false
        } else {
            acquire()
            true
        }
    }

    val isHeld: Boolean
        get() = wakeLock?.isHeld == true
}