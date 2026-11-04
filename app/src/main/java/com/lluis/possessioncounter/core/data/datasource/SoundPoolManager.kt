package com.lluis.possessioncounter.core.data.datasource

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.lluis.possessioncounter.core.domain.model.SoundType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gestor de audio de baja latencia usando SoundPool.
 * Ideal para sonidos cortos que requieren reproducción inmediata (<20ms).
 */
@Singleton
class SoundPoolManager @Inject constructor(
    private val context: Context
) {

    // Configuración de SoundPool
    private val soundPool = SoundPool.Builder()
        .setMaxStreams(4) // 3 beeps + 1 whistle simultáneos max
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED) // Respeta modo silencio salvo override
                .build()
        )
        .build()

    // Mapa de SoundType -> soundId cargado
    private val soundIdMap = mutableMapOf<SoundType, Int>()
    private val loadStatus = mutableMapOf<SoundType, Boolean>()

    // Scope para cargas asíncronas
    private val loadScope = CoroutineScope(Dispatchers.IO)

    init {
        loadAllSounds()
    }

    /**
     * Carga todos los sonidos en background
     */
    private fun loadAllSounds() {
        loadScope.launch {
            SoundType.values().forEach { soundType ->
                try {
                    val soundId = soundPool.load(context, getResourceId(soundType), 1)
                    soundIdMap[soundType] = soundId
                    loadStatus[soundType] = true
                } catch (e: Exception) {
                    loadStatus[soundType] = false
                }
            }
        }
    }

    /**
     * Obtiene el resource ID (R.raw.xxx) para un SoundType
     */
    private fun getResourceId(soundType: SoundType): Int {
        return context.resources.getIdentifier(
            soundType.resourceName,
            "raw",
            context.packageName
        )
    }

    /**
     * Reproduce un sonido con volumen específico
     * @param soundType Tipo de sonido a reproducir
     * @param volume Volumen 0.0 - 1.0 (se multiplica por masterVolume externamente)
     * @return streamId si se reprodujo, -1 si error
     */
    fun play(soundType: SoundType, volume: Float = 1.0f): Int {
        val soundId = soundIdMap[soundType]
            ?: return -1 // No cargado aún

        val clampedVolume = volume.coerceIn(0.0f, 1.0f)
        return soundPool.play(
            soundId,
            clampedVolume, // leftVolume
            clampedVolume, // rightVolume
            1,             // priority
            0,             // loop (0 = no loop)
            1.0f           // rate (1.0 = normal)
        )
    }

    /**
     * Reproduce una secuencia de sonidos con delays
     * @param sequence Lista de (SoundType, delayMs, volume)
     */
    fun playSequence(sequence: List<Triple<SoundType, Long, Float>>) {
        loadScope.launch {
            sequence.forEach { (soundType, delayMs, volume) ->
                if (delayMs > 0) {
                    kotlinx.coroutines.delay(delayMs)
                }
                play(soundType, volume)
            }
        }
    }

    /**
     * Reproduce la secuencia completa de expiración:
     * 3 beeps de advertencia (1 por segundo) + 1 silbato final
     */
    fun playExpirationSequence(
        warningSound: SoundType,
        whistleSound: SoundType,
        masterVolume: Float,
        onWarningBeep: (Int) -> Unit = {}, // callback para vibrar en cada beep
        onWhistle: () -> Unit = {}          // callback para vibrar fuerte al final
    ) {
        loadScope.launch {
            // 3 beeps de advertencia: a los 0ms, 1000ms, 2000ms
            repeat(3) { i ->
                val delay = i * 1000L
                if (delay > 0) kotlinx.coroutines.delay(delay)
                val streamId = play(warningSound, masterVolume)
                if (streamId != -1) onWarningBeep(i + 1)
            }

            // Silbato final a los 3000ms
            kotlinx.coroutines.delay(3000)
            val streamId = play(whistleSound, masterVolume)
            if (streamId != -1) onWhistle()
        }
    }

    /**
     * Verifica si un sonido está cargado
     */
    fun isLoaded(soundType: SoundType): Boolean = loadStatus[soundType] == true

    /**
     * Libera recursos (llamar en Application.onTerminate o similar)
     */
    fun release() {
        soundPool.release()
        soundIdMap.clear()
        loadStatus.clear()
        loadScope.cancel()
    }
}