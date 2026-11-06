package dev.lluissunol.vuitsegons.core.domain.usecase

import dev.lluissunol.vuitsegons.core.domain.repository.SettingsRepository
import javax.inject.Inject

/**
 * Caso de uso: Resetear configuración a valores por defecto
 */
class ResetSettingsUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke() = repository.resetToDefaults()
}