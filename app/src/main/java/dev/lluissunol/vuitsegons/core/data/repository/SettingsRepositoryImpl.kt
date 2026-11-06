package dev.lluissunol.vuitsegons.core.data.repository

import dev.lluissunol.vuitsegons.core.data.datasource.SettingsDataStore
import dev.lluissunol.vuitsegons.core.domain.model.Settings
import dev.lluissunol.vuitsegons.core.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Implementación de SettingsRepository usando DataStore
 */
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: SettingsDataStore
) : SettingsRepository {

    override val settingsFlow: Flow<Settings> = dataStore.settingsFlow

    override suspend fun getSettings(): Settings = dataStore.getSettings()

    override suspend fun saveSettings(settings: Settings) = dataStore.saveSettings(settings)

    override suspend fun resetToDefaults() = dataStore.resetToDefaults()
}