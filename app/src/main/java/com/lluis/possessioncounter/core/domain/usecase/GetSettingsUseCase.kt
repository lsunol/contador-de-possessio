package com.lluis.possessioncounter.core.domain.usecase

import com.lluis.possessioncounter.core.domain.model.Settings
import com.lluis.possessioncounter.core.domain.repository.SettingsRepository
import javax.inject.Inject

/**
 * Caso de uso: Obtener configuración actual
 */
class GetSettingsUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(): Settings = repository.getSettings()

    /** Flow reactivo para observar cambios */
    val settingsFlow = repository.settingsFlow
}