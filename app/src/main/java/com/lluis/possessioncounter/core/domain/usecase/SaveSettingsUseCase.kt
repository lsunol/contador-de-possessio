package com.lluis.possessioncounter.core.domain.usecase

import com.lluis.possessioncounter.core.domain.model.Settings
import com.lluis.possessioncounter.core.domain.repository.SettingsRepository
import javax.inject.Inject

/**
 * Caso de uso: Guardar configuración
 */
class SaveSettingsUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(settings: Settings) = repository.saveSettings(settings.validated())
}