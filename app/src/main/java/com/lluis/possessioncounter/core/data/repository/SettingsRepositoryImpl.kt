package com.lluis.possessioncounter.core.data.repository

import com.lluis.possessioncounter.core.data.datasource.SettingsDataStore
import com.lluis.possessioncounter.core.domain.model.Settings
import com.lluis.possessioncounter.core.domain.repository.SettingsRepository
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