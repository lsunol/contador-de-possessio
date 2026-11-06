package dev.lluissunol.vuitsegons.core.domain.repository

import dev.lluissunol.vuitsegons.core.domain.model.Settings
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz para persistir y obtener configuración.
 * Implementada por DataStore en la capa de datos.
 */
interface SettingsRepository {

    /** Flow reactivo de settings (emite cambios automáticamente) */
    val settingsFlow: Flow<Settings>

    /** Obtener settings actuales (suspend para lectura única) */
    suspend fun getSettings(): Settings

    /** Guardar settings (persiste y emite en flow) */
    suspend fun saveSettings(settings: Settings)

    /** Resetear a valores por defecto */
    suspend fun resetToDefaults()
}