package dev.lluissunol.vuitsegons.core.data.datasource

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.lluissunol.vuitsegons.core.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.settingsPreferences by preferencesDataStore("possession_counter_settings")

// Claves para DataStore Preferences
private object SettingsKeys {
    // Tiempo
    val BASE_DURATION = intPreferencesKey("base_duration_seconds")
    val WARNING_THRESHOLD = intPreferencesKey("warning_threshold_seconds")

    // Audio
    val MASTER_VOLUME = stringPreferencesKey("master_volume")
    val PLAY_TICK = stringPreferencesKey("play_tick_on_reset")
    val PLAY_WARNING = stringPreferencesKey("play_warning_beeps")
    val PLAY_WHISTLE = stringPreferencesKey("play_final_whistle")
    val WARNING_SOUND = stringPreferencesKey("warning_sound_type")
    val WHISTLE_SOUND = stringPreferencesKey("whistle_sound_type")
    val OVERRIDE_DND = stringPreferencesKey("override_do_not_disturb")

    // Hápticos
    val VIBRATE_RESET = stringPreferencesKey("vibrate_on_reset")
    val VIBRATE_WARNING = stringPreferencesKey("vibrate_on_warning")
    val VIBRATE_EXPIRE = stringPreferencesKey("vibrate_on_expire")
    val VIBRATION_PATTERN = stringPreferencesKey("vibration_pattern")

    // Visual
    val ACTIVE_COLOR = stringPreferencesKey("active_color")
    val WARNING_COLOR = stringPreferencesKey("warning_color")
    val EXPIRED_COLOR = stringPreferencesKey("expired_color")
    val BACKGROUND_COLOR = stringPreferencesKey("background_color")
    val KEEP_SCREEN_ON = stringPreferencesKey("keep_screen_on")
    val DIGIT_SIZE = stringPreferencesKey("digit_size")
    val FORCE_REVERSE = stringPreferencesKey("force_reverse_portrait")

    // Comportamiento
    val RESET_GESTURE = stringPreferencesKey("reset_gesture")
    val PAUSE_GESTURE = stringPreferencesKey("pause_gesture")
    val AUTO_START = stringPreferencesKey("auto_start")
    val SWAP_TAP_GESTURES = stringPreferencesKey("swap_tap_gestures")
    val RESET_ON_RESUME = stringPreferencesKey("reset_on_resume")

}

/**
 * DataStore para persistencia de Settings usando Preferences DataStore
 */
class SettingsDataStore @Inject constructor(
    context: Context
) {

    private val dataStore = context.settingsPreferences

    /** Flow reactivo de settings */
    val settingsFlow: Flow<Settings> = dataStore.data
        .catch {
            // Si hay error de corrupción, emitir defaults
            emit(emptyPreferences())
        }
        .map { prefs -> prefs.toSettings() }

    /** Obtener settings actuales (lectura única) */
    suspend fun getSettings(): Settings = dataStore.data
        .first()
        .toSettings()

    /** Guardar settings */
    suspend fun saveSettings(settings: Settings) {
        dataStore.edit { prefs ->
            val validated = settings.validated()
            prefs[SettingsKeys.BASE_DURATION] = validated.baseDurationSeconds
            prefs[SettingsKeys.WARNING_THRESHOLD] = validated.warningThresholdSeconds
            prefs[SettingsKeys.MASTER_VOLUME] = validated.masterVolume.toString()
            prefs[SettingsKeys.PLAY_TICK] = validated.playTickOnReset.toString()
            prefs[SettingsKeys.PLAY_WARNING] = validated.playWarningBeeps.toString()
            prefs[SettingsKeys.PLAY_WHISTLE] = validated.playFinalWhistle.toString()
            prefs[SettingsKeys.WARNING_SOUND] = validated.warningSoundType.name
            prefs[SettingsKeys.WHISTLE_SOUND] = validated.whistleSoundType.name
            prefs[SettingsKeys.OVERRIDE_DND] = validated.overrideDoNotDisturb.toString()
            prefs[SettingsKeys.VIBRATE_RESET] = validated.vibrateOnReset.toString()
            prefs[SettingsKeys.VIBRATE_WARNING] = validated.vibrateOnWarning.toString()
            prefs[SettingsKeys.VIBRATE_EXPIRE] = validated.vibrateOnExpire.toString()
            prefs[SettingsKeys.VIBRATION_PATTERN] = validated.vibrationPattern.name
            prefs[SettingsKeys.ACTIVE_COLOR] = validated.activeColor.toString()
            prefs[SettingsKeys.WARNING_COLOR] = validated.warningColor.toString()
            prefs[SettingsKeys.EXPIRED_COLOR] = validated.expiredColor.toString()
            prefs[SettingsKeys.BACKGROUND_COLOR] = validated.backgroundColor.toString()
            prefs[SettingsKeys.KEEP_SCREEN_ON] = validated.keepScreenOn.toString()
            prefs[SettingsKeys.DIGIT_SIZE] = validated.digitSize.name
            prefs[SettingsKeys.FORCE_REVERSE] = validated.forceReversePortrait.toString()
            prefs[SettingsKeys.RESET_GESTURE] = validated.resetGesture.name
            prefs[SettingsKeys.PAUSE_GESTURE] = validated.pauseGesture.name
            prefs[SettingsKeys.AUTO_START] = validated.autoStart.toString()
            prefs[SettingsKeys.SWAP_TAP_GESTURES] = validated.swapTapGestures.toString()
            prefs[SettingsKeys.RESET_ON_RESUME] = validated.resetOnResume.toString()
        }
    }

    /** Resetear a defaults */
    suspend fun resetToDefaults() {
        dataStore.edit { prefs ->
            val defaults = Settings.DEFAULT
            prefs[SettingsKeys.BASE_DURATION] = defaults.baseDurationSeconds
            prefs[SettingsKeys.WARNING_THRESHOLD] = defaults.warningThresholdSeconds
            prefs[SettingsKeys.MASTER_VOLUME] = defaults.masterVolume.toString()
            prefs[SettingsKeys.PLAY_TICK] = defaults.playTickOnReset.toString()
            prefs[SettingsKeys.PLAY_WARNING] = defaults.playWarningBeeps.toString()
            prefs[SettingsKeys.PLAY_WHISTLE] = defaults.playFinalWhistle.toString()
            prefs[SettingsKeys.WARNING_SOUND] = defaults.warningSoundType.name
            prefs[SettingsKeys.WHISTLE_SOUND] = defaults.whistleSoundType.name
            prefs[SettingsKeys.OVERRIDE_DND] = defaults.overrideDoNotDisturb.toString()
            prefs[SettingsKeys.VIBRATE_RESET] = defaults.vibrateOnReset.toString()
            prefs[SettingsKeys.VIBRATE_WARNING] = defaults.vibrateOnWarning.toString()
            prefs[SettingsKeys.VIBRATE_EXPIRE] = defaults.vibrateOnExpire.toString()
            prefs[SettingsKeys.VIBRATION_PATTERN] = defaults.vibrationPattern.name
            prefs[SettingsKeys.ACTIVE_COLOR] = defaults.activeColor.toString()
            prefs[SettingsKeys.WARNING_COLOR] = defaults.warningColor.toString()
            prefs[SettingsKeys.EXPIRED_COLOR] = defaults.expiredColor.toString()
            prefs[SettingsKeys.BACKGROUND_COLOR] = defaults.backgroundColor.toString()
            prefs[SettingsKeys.KEEP_SCREEN_ON] = defaults.keepScreenOn.toString()
            prefs[SettingsKeys.DIGIT_SIZE] = defaults.digitSize.name
            prefs[SettingsKeys.FORCE_REVERSE] = defaults.forceReversePortrait.toString()
            prefs[SettingsKeys.RESET_GESTURE] = defaults.resetGesture.name
            prefs[SettingsKeys.PAUSE_GESTURE] = defaults.pauseGesture.name
            prefs[SettingsKeys.AUTO_START] = defaults.autoStart.toString()
            prefs[SettingsKeys.SWAP_TAP_GESTURES] = defaults.swapTapGestures.toString()
            prefs[SettingsKeys.RESET_ON_RESUME] = defaults.resetOnResume.toString()
        }
    }
}

/**
 * Extensiones para conversión Preferences <-> Settings
 */
private fun Preferences.toSettings(): Settings {
    return Settings(
        baseDurationSeconds = this[SettingsKeys.BASE_DURATION] ?: Settings.DEFAULT.baseDurationSeconds,
        warningThresholdSeconds = this[SettingsKeys.WARNING_THRESHOLD] ?: Settings.DEFAULT.warningThresholdSeconds,
        masterVolume = this[SettingsKeys.MASTER_VOLUME]?.toFloatOrNull() ?: Settings.DEFAULT.masterVolume,
        playTickOnReset = this[SettingsKeys.PLAY_TICK]?.toBooleanStrictOrNull() ?: Settings.DEFAULT.playTickOnReset,
        playWarningBeeps = this[SettingsKeys.PLAY_WARNING]?.toBooleanStrictOrNull() ?: Settings.DEFAULT.playWarningBeeps,
        playFinalWhistle = this[SettingsKeys.PLAY_WHISTLE]?.toBooleanStrictOrNull() ?: Settings.DEFAULT.playFinalWhistle,
        warningSoundType = this[SettingsKeys.WARNING_SOUND]?.let { SoundType.valueOf(it) } ?: Settings.DEFAULT.warningSoundType,
        whistleSoundType = this[SettingsKeys.WHISTLE_SOUND]?.let { SoundType.valueOf(it) } ?: Settings.DEFAULT.whistleSoundType,
        overrideDoNotDisturb = this[SettingsKeys.OVERRIDE_DND]?.toBooleanStrictOrNull() ?: Settings.DEFAULT.overrideDoNotDisturb,
        vibrateOnReset = this[SettingsKeys.VIBRATE_RESET]?.toBooleanStrictOrNull() ?: Settings.DEFAULT.vibrateOnReset,
        vibrateOnWarning = this[SettingsKeys.VIBRATE_WARNING]?.toBooleanStrictOrNull() ?: Settings.DEFAULT.vibrateOnWarning,
        vibrateOnExpire = this[SettingsKeys.VIBRATE_EXPIRE]?.toBooleanStrictOrNull() ?: Settings.DEFAULT.vibrateOnExpire,
        vibrationPattern = this[SettingsKeys.VIBRATION_PATTERN]?.let { VibrationPattern.valueOf(it) } ?: Settings.DEFAULT.vibrationPattern,
        activeColor = this[SettingsKeys.ACTIVE_COLOR]?.toIntOrNull() ?: Settings.DEFAULT.activeColor,
        warningColor = this[SettingsKeys.WARNING_COLOR]?.toIntOrNull() ?: Settings.DEFAULT.warningColor,
        expiredColor = this[SettingsKeys.EXPIRED_COLOR]?.toIntOrNull() ?: Settings.DEFAULT.expiredColor,
        backgroundColor = this[SettingsKeys.BACKGROUND_COLOR]?.toIntOrNull() ?: Settings.DEFAULT.backgroundColor,
        keepScreenOn = this[SettingsKeys.KEEP_SCREEN_ON]?.toBooleanStrictOrNull() ?: Settings.DEFAULT.keepScreenOn,
        digitSize = this[SettingsKeys.DIGIT_SIZE]?.let { DigitSize.valueOf(it) } ?: Settings.DEFAULT.digitSize,
        forceReversePortrait = this[SettingsKeys.FORCE_REVERSE]?.toBooleanStrictOrNull() ?: Settings.DEFAULT.forceReversePortrait,
        resetGesture = this[SettingsKeys.RESET_GESTURE]?.let { ResetGesture.valueOf(it) } ?: Settings.DEFAULT.resetGesture,
        pauseGesture = this[SettingsKeys.PAUSE_GESTURE]?.let { PauseGesture.valueOf(it) } ?: Settings.DEFAULT.pauseGesture,
        autoStart = this[SettingsKeys.AUTO_START]?.toBooleanStrictOrNull() ?: Settings.DEFAULT.autoStart,
        swapTapGestures = this[SettingsKeys.SWAP_TAP_GESTURES]?.toBooleanStrictOrNull() ?: Settings.DEFAULT.swapTapGestures,
        resetOnResume = this[SettingsKeys.RESET_ON_RESUME]?.toBooleanStrictOrNull() ?: Settings.DEFAULT.resetOnResume
    ).validated()
}