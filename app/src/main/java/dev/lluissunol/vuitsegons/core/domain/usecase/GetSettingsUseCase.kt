package dev.lluissunol.vuitsegons.core.domain.usecase

import dev.lluissunol.vuitsegons.core.domain.model.Settings
import dev.lluissunol.vuitsegons.core.domain.repository.SettingsRepository
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