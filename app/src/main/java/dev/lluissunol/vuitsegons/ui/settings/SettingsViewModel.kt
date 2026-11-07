package dev.lluissunol.vuitsegons.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.lluissunol.vuitsegons.core.domain.model.Settings
import dev.lluissunol.vuitsegons.core.domain.usecase.GetSettingsUseCase
import dev.lluissunol.vuitsegons.core.domain.usecase.ResetSettingsUseCase
import dev.lluissunol.vuitsegons.core.domain.usecase.SaveSettingsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val getSettings: GetSettingsUseCase,
    private val saveSettings: SaveSettingsUseCase,
    private val resetSettings: ResetSettingsUseCase
) : ViewModel() {

    // ===== STATE =====
    private val _settings = MutableStateFlow(Settings.DEFAULT)
    val settings = _settings.asStateFlow()

    init {
        viewModelScope.launch { _settings.value = getSettings() }
    }

    // ===== ACTIONS =====
    fun updateSettings(newSettings: Settings) {
        val validated = newSettings.validated()
        _settings.value = validated
        viewModelScope.launch { saveSettings(validated) }
    }

    fun updateBaseDuration(seconds: Int) {
        updateSettings(_settings.value.copy(baseDurationSeconds = seconds.coerceIn(1, 60)))
    }

    fun updateWarningThreshold(seconds: Int) {
        val maxWarning = (_settings.value.baseDurationSeconds - 1).coerceAtLeast(1)
        updateSettings(_settings.value.copy(warningThresholdSeconds = seconds.coerceIn(1, maxWarning)))
    }

    fun updateMasterVolume(volume: Float) {
        updateSettings(_settings.value.copy(masterVolume = volume.coerceIn(0f, 1f)))
    }

    fun updateBooleanSetting(
        getter: (Settings) -> Boolean,
        setter: (Settings, Boolean) -> Settings
    ) {
        val current = _settings.value
        updateSettings(setter(current, !getter(current)))
    }

    fun updateSoundType(
        isWarning: Boolean,
        newType: dev.lluissunol.vuitsegons.core.domain.model.SoundType
    ) {
        updateSettings(if (isWarning) {
            _settings.value.copy(warningSoundType = newType)
        } else {
            _settings.value.copy(whistleSoundType = newType)
        })
    }

    fun updateVibrationPattern(pattern: dev.lluissunol.vuitsegons.core.domain.model.VibrationPattern) {
        updateSettings(_settings.value.copy(vibrationPattern = pattern))
    }

    fun updateColor(
        colorType: ColorType,
        color: Int
    ) {
        updateSettings(when (colorType) {
            ColorType.ACTIVE -> _settings.value.copy(activeColor = color)
            ColorType.WARNING -> _settings.value.copy(warningColor = color)
            ColorType.EXPIRED -> _settings.value.copy(expiredColor = color)
            ColorType.BACKGROUND -> _settings.value.copy(backgroundColor = color)
        })
    }

    fun updateDigitSize(size: dev.lluissunol.vuitsegons.core.domain.model.DigitSize) {
        updateSettings(_settings.value.copy(digitSize = size))
    }

    fun updateGesture(
        gestureType: GestureType,
        gesture: Enum<*>
    ) {
        updateSettings(when (gestureType) {
            GestureType.RESET -> _settings.value.copy(resetGesture = gesture as dev.lluissunol.vuitsegons.core.domain.model.ResetGesture)
            GestureType.PAUSE -> _settings.value.copy(pauseGesture = gesture as dev.lluissunol.vuitsegons.core.domain.model.PauseGesture)
        })
    }

    fun updateForceReversePortrait(enabled: Boolean) {
        updateSettings(_settings.value.copy(forceReversePortrait = enabled))
    }

    fun updateKeepScreenOn(enabled: Boolean) {
        updateSettings(_settings.value.copy(keepScreenOn = enabled))
    }

    fun updateAutoStart(enabled: Boolean) {
        updateSettings(_settings.value.copy(autoStart = enabled))
    }

    fun updateSwapTapGestures(enabled: Boolean) {
        updateSettings(_settings.value.copy(swapTapGestures = enabled))
    }

    fun updateResetOnResume(enabled: Boolean) {
        updateSettings(_settings.value.copy(resetOnResume = enabled))
    }

    fun resetToDefaults() {
        _settings.value = Settings.DEFAULT
        viewModelScope.launch { resetSettings() }
    }

    // ===== HELPER ENUMS FOR UI =====
    enum class ColorType { ACTIVE, WARNING, EXPIRED, BACKGROUND }
    enum class GestureType { RESET, PAUSE }
}