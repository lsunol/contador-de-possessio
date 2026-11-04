package com.lluis.possessioncounter.core.domain.usecase

import com.lluis.possessioncounter.core.domain.repository.SettingsRepository
import javax.inject.Inject

/**
 * Caso de uso: Resetear configuración a valores por defecto
 */
class ResetSettingsUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke() = repository.resetToDefaults()
}