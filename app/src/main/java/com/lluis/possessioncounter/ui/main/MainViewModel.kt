package com.lluis.possessioncounter.ui.main

import android.os.SystemClock
import android.os.Vibrator
import android.os.VibrationEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lluis.possessioncounter.core.data.timer.CoroutineTimer
import com.lluis.possessioncounter.core.domain.model.*
import com.lluis.possessioncounter.core.domain.model.isRunning
import com.lluis.possessioncounter.core.domain.usecase.GetSettingsUseCase
import com.lluis.possessioncounter.core.domain.usecase.SaveSettingsUseCase
import com.lluis.possessioncounter.util.WakeLockManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel

class MainViewModel(
    private val getSettings: GetSettingsUseCase,
    private val saveSettings: SaveSettingsUseCase,
    private val timer: CoroutineTimer,
    private val soundManager: com.lluis.possessioncounter.core.data.datasource.SoundPoolManager,
    private val wakeLock: WakeLockManager,
    private val vibrator: Vibrator
) : ViewModel() {

    // ===== STATE =====
    private var _uiState = mutableStateOf<TimerState>(TimerState.Initial(Settings.DEFAULT.baseDurationMillis))
    val uiState: TimerState
        @Suppress("UNUSED_GETTER")
        get() = _uiState.value

    // Timer job actual
    private var timerJob: Job? = null
    private var currentSettings = Settings.DEFAULT
    private var settingsLoaded = false
    private var warningBeepCount = 0

    val settingsFlow = getSettings.settingsFlow

    init {
        observeSettings()
    }

    // ===== SETTINGS OBSERVATION =====
    private fun observeSettings() {
        viewModelScope.launch {
            getSettings.settingsFlow.collect { settings ->
                if (!settingsLoaded) {
                    settingsLoaded = true
                    applyInitialSettings(settings.validated())
                } else if (settings.validated() != currentSettings) {
                    updateSettings(settings)
                }
            }
        }
    }

    /** Arranque: el timer espera a que se hayan leído los ajustes guardados. */
    private fun applyInitialSettings(settings: Settings) {
        currentSettings = settings
        _uiState.value = TimerState.Initial(settings.baseDurationMillis)
        if (settings.keepScreenOn) {
            wakeLock.acquire()
        }
        if (settings.autoStart) {
            startTimer()
        }
    }

    fun updateSettings(newSettings: Settings) {
        currentSettings = newSettings.validated()
        // Reaplicar wakeLock
        if (currentSettings.keepScreenOn) wakeLock.acquire() else wakeLock.release()
        // Si timer corriendo, reiniciar con nueva duración
        if (uiState.isRunning) {
            restartTimer()
        } else if (uiState is TimerState.Initial) {
            _uiState.value = TimerState.Initial(currentSettings.baseDurationMillis)
        }
    }

    // ===== TIMER CONTROL =====
    fun startTimer() {
        cancelTimer()
        val settings = currentSettings
        _uiState.value = TimerState.Running(
            remainingMillis = settings.baseDurationMillis,
            totalMillis = settings.baseDurationMillis,
            warningThresholdMillis = settings.warningThresholdMillis
        )

        timerJob = timer.startTimer(
            durationMillis = settings.baseDurationMillis,
            warningThresholdMillis = settings.warningThresholdMillis,
            onTick = { remaining ->
                // Update UI state on main thread
                _uiState.value = TimerState.Running(
                    remainingMillis = remaining,
                    totalMillis = settings.baseDurationMillis,
                    warningThresholdMillis = settings.warningThresholdMillis
                )
            },
            onWarning = { remaining ->
                onWarningTriggered(remaining)
            },
            onExpire = { onExpire() }
        )
    }

    fun pauseTimer() {
        val current = uiState
        if (current is TimerState.Running) {
            timerJob?.cancel()
            timerJob = null
            _uiState.value = TimerState.Paused(
                remainingMillis = current.remainingMillis,
                totalMillis = current.totalMillis
            )
        } else if (current is TimerState.Paused) {
            // Reanudar desde donde paró
            resumeTimer(current.remainingMillis)
        }
    }

    fun resetTimer() {
        cancelTimer()
        val settings = currentSettings
        _uiState.value = TimerState.Initial(settings.baseDurationMillis)

        // Feedback: tick sound + vibration
        if (settings.playTickOnReset) {
            soundManager.play(SoundType.TICK_RESET, settings.masterVolume)
        }
        if (settings.vibrateOnReset) {
            vibrate(settings.vibrationPattern)
        }

        // Auto-start si configurado
        if (settings.autoStart) {
            startTimer()
        }
    }

    private fun resumeTimer(remainingMillis: Long) {
        val settings = currentSettings
        _uiState.value = TimerState.Running(
            remainingMillis = remainingMillis,
            totalMillis = settings.baseDurationMillis,
            warningThresholdMillis = settings.warningThresholdMillis
        )

        timerJob = timer.startTimer(
            durationMillis = remainingMillis,
            warningThresholdMillis = settings.warningThresholdMillis,
            onTick = { remaining ->
                _uiState.value = TimerState.Running(
                    remainingMillis = remaining,
                    totalMillis = settings.baseDurationMillis,
                    warningThresholdMillis = settings.warningThresholdMillis
                )
            },
            onWarning = { remaining ->
                onWarningTriggered(remaining)
            },
            onExpire = { onExpire() }
        )
    }

    private fun restartTimer() {
        cancelTimer()
        startTimer()
    }

    private fun cancelTimer() {
        timerJob?.cancel()
        timerJob = null
        warningBeepCount = 0
    }

    // ===== EVENT HANDLERS =====
    private fun onWarningTriggered(remaining: Long) {
        val settings = currentSettings
        warningBeepCount++

        // Beep de advertencia
        if (settings.playWarningBeeps) {
            soundManager.play(settings.warningSoundType, settings.masterVolume)
        }

        // Vibración de advertencia
        if (settings.vibrateOnWarning) {
            vibrate(settings.vibrationPattern)
        }
    }

    private fun onExpire() {
        val settings = currentSettings
        _uiState.value = TimerState.Expired

        // Secuencia: 3 beeps (ya sonaron en warning) + silbato final
        // Nota: Los 3 beeps de warning ya se dispararon en onWarningTriggered
        // Aquí solo el silbato final
        if (settings.playFinalWhistle) {
            soundManager.play(settings.whistleSoundType, settings.masterVolume)
        }

        // Vibración fuerte final
        if (settings.vibrateOnExpire) {
            vibrate(VibrationPattern.LONG)
        }
    }

    // ===== GESTURE HANDLING =====
    fun onSingleTap() {
        when (currentSettings.resetGesture) {
            ResetGesture.SINGLE_TAP -> resetTimer()
            ResetGesture.DOUBLE_TAP -> {} // Se maneja en onDoubleTap
            ResetGesture.VOLUME_BUTTON -> {} // Se maneja en Activity
            ResetGesture.POWER_BUTTON -> {} // Se maneja en Activity
        }
    }

    fun onDoubleTap() {
        when (currentSettings.pauseGesture) {
            PauseGesture.DOUBLE_TAP -> pauseTimer()
            PauseGesture.LONG_PRESS -> {} // Se maneja en onLongPress
            PauseGesture.THREE_TAP -> {} // Se manejaría en triple tap
            PauseGesture.VOLUME_DOWN -> {} // Se maneja en Activity
        }
        when (currentSettings.resetGesture) {
            ResetGesture.DOUBLE_TAP -> resetTimer()
            else -> {}
        }
    }

    fun onLongPress() {
        when (currentSettings.pauseGesture) {
            PauseGesture.LONG_PRESS -> pauseTimer()
            else -> {}
        }
    }

    // ===== VIBRATION HELPER =====
    private fun vibrate(pattern: VibrationPattern) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern.pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern.pattern, -1)
        }
    }

    override fun onCleared() {
        super.onCleared()
        cancelTimer()
        wakeLock.release()
    }
}