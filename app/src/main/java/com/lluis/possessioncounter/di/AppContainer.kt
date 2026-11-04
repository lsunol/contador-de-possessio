package com.lluis.possessioncounter.di

import android.content.Context
import android.os.Vibrator
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.lluis.possessioncounter.core.data.datasource.SettingsDataStore
import com.lluis.possessioncounter.core.data.datasource.SoundPoolManager
import com.lluis.possessioncounter.core.data.repository.SettingsRepositoryImpl
import com.lluis.possessioncounter.core.data.timer.CoroutineTimer
import com.lluis.possessioncounter.core.domain.repository.SettingsRepository
import com.lluis.possessioncounter.core.domain.usecase.GetSettingsUseCase
import com.lluis.possessioncounter.core.domain.usecase.ResetSettingsUseCase
import com.lluis.possessioncounter.core.domain.usecase.SaveSettingsUseCase
import com.lluis.possessioncounter.ui.main.MainViewModel
import com.lluis.possessioncounter.ui.settings.SettingsViewModel
import com.lluis.possessioncounter.util.WakeLockManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Manual dependency container, used while Hilt is disabled in app/build.gradle.kts.
 * Everything is created once, on first use, for the lifetime of the process.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    private val settingsRepository: SettingsRepository by lazy {
        SettingsRepositoryImpl(SettingsDataStore(appContext))
    }
    private val getSettings by lazy { GetSettingsUseCase(settingsRepository) }
    private val saveSettings by lazy { SaveSettingsUseCase(settingsRepository) }
    private val resetSettings by lazy { ResetSettingsUseCase(settingsRepository) }
    private val soundManager by lazy { SoundPoolManager(appContext) }
    private val wakeLock by lazy { WakeLockManager(appContext) }
    private val timer by lazy {
        // Main thread: a tick can never run after the UI has cancelled the timer.
        CoroutineTimer(CoroutineScope(Dispatchers.Main.immediate + SupervisorJob()))
    }

    @Suppress("DEPRECATION") // VibratorManager needs API 31; minSdk is 24
    private val vibrator by lazy {
        appContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    val viewModelFactory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            MainViewModel(getSettings, saveSettings, timer, soundManager, wakeLock, vibrator)
        }
        initializer {
            SettingsViewModel(getSettings, saveSettings, resetSettings)
        }
    }
}
