package dev.lluissunol.vuitsegons.core.domain.usecase

import dev.lluissunol.vuitsegons.core.domain.model.Settings
import dev.lluissunol.vuitsegons.core.domain.repository.SettingsRepository
import javax.inject.Inject

/**
 * Caso de uso: Guardar configuración
 */
class SaveSettingsUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(settings: Settings) = repository.saveSettings(settings.validated())
}