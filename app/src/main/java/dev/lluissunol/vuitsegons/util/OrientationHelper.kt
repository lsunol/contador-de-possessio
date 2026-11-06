package dev.lluissunol.vuitsegons.util

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.Surface

/**
 * Utilidades para manejo de orientación 180° (reversePortrait).
 * El árbitro lleva el móvil colgado del cuello boca abajo.
 */
object OrientationHelper {

    /** Constante para orientación normal */
    const val ORIENTATION_NORMAL = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

    /** Constante para orientación 180° (boca abajo) */
    const val ORIENTATION_REVERSE = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT

    /** Constante para sensor-based (auto-rotar pero solo portrait/reverse) */
    const val ORIENTATION_SENSOR_PORTRAIT = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT

    /**
     * Aplica orientación a una Activity
     */
    fun setOrientation(activity: Activity, reverse: Boolean) {
        activity.requestedOrientation = if (reverse) ORIENTATION_REVERSE else ORIENTATION_NORMAL
    }

    /**
     * Obtiene la rotación actual de la pantalla en grados (0, 90, 180, 270)
     */
    fun getDisplayRotation(activity: Activity): Int {
        return when (activity.windowManager.defaultDisplay.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> 0
        }
    }

    /**
     * Verifica si la pantalla está actualmente en orientación reversa (180°)
     */
    fun isReversePortrait(activity: Activity): Boolean {
        return activity.resources.configuration.orientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
    }
}