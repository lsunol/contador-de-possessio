package com.lluis.possessioncounter.core.data.datasource

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.lluis.possessioncounter.core.domain.model.SoundType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.PI
import kotlin.math.sin

/**
 * Gestor de sonidos sintetizados (sin recursos raw).
 * Genera PCM16 mono con ondas seno/cuadrada y lo reproduce con AudioTrack (MODE_STATIC).
 * Mantiene el nombre/API pública original para no tocar a los llamadores.
 */
@Singleton
class SoundPoolManager @Inject constructor(
    @Suppress("UNUSED_PARAMETER") private val context: Context
) {

    private val sampleRate = 44100

    private val attributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    private val format = AudioFormat.Builder()
        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
        .setSampleRate(sampleRate)
        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
        .build()

    // PCM generado por tipo de sonido (cache)
    private val pcmCache = ConcurrentHashMap<SoundType, ShortArray>()
    private val activeTracks = ConcurrentHashMap.newKeySet<AudioTrack>()
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        // Pre-generar en background
        scope.launch { SoundType.values().forEach { pcmFor(it) } }
    }

    private fun pcmFor(type: SoundType): ShortArray =
        pcmCache.getOrPut(type) { synthesize(type) }

    private fun synthesize(type: SoundType): ShortArray = when (type) {
        SoundType.BEEP_CLASSIC -> tone(880.0, 150, square = false)
        SoundType.BEEP_SOFT -> tone(660.0, 150, square = false, amp = 0.6)
        SoundType.BEEP_DIGITAL -> tone(1000.0, 120, square = true, amp = 0.5)
        SoundType.WHISTLE_CLASSIC -> whistle(2600.0, 2400.0, 700, vibratoHz = 30.0)
        SoundType.WHISTLE_SHORT -> whistle(3000.0, 3000.0, 400, vibratoHz = 25.0)
        SoundType.WHISTLE_LONG -> whistle(2800.0, 2200.0, 1200, vibratoHz = 35.0)
        SoundType.TICK_RESET -> tone(1500.0, 40, square = false, amp = 0.7)
    }

    private fun tone(freq: Double, ms: Int, square: Boolean, amp: Double = 0.8): ShortArray {
        val n = sampleRate * ms / 1000
        val fade = (sampleRate * 0.008).toInt().coerceAtMost(n / 2)
        return ShortArray(n) { i ->
            val s = sin(2.0 * PI * freq * i / sampleRate)
            var v = if (square) (if (s >= 0) 1.0 else -1.0) else s
            val env = when {
                i < fade -> i.toDouble() / fade
                i > n - fade -> (n - i).toDouble() / fade
                else -> 1.0
            }
            v *= env * amp
            (v * Short.MAX_VALUE).toInt().toShort()
        }
    }

    /** Silbato: tono agudo con vibrato rápido (efecto "pea") y ligero glissando. */
    private fun whistle(f0: Double, f1: Double, ms: Int, vibratoHz: Double): ShortArray {
        val n = sampleRate * ms / 1000
        val fade = (sampleRate * 0.015).toInt().coerceAtMost(n / 2)
        var phase = 0.0
        return ShortArray(n) { i ->
            val t = i.toDouble() / sampleRate
            val base = f0 + (f1 - f0) * i / n
            val f = base + 120.0 * sin(2.0 * PI * vibratoHz * t)
            phase += 2.0 * PI * f / sampleRate
            val v = 0.7 * sin(phase) + 0.2 * sin(2.0 * phase)
            val env = when {
                i < fade -> i.toDouble() / fade
                i > n - fade -> (n - i).toDouble() / fade
                else -> 1.0
            }
            (v * env * 0.9 * Short.MAX_VALUE).toInt().toShort()
        }
    }

    /**
     * Reproduce un sonido con volumen específico
     * @param volume Volumen 0.0 - 1.0
     * @return 0 si se lanzó la reproducción, -1 si error
     */
    fun play(soundType: SoundType, volume: Float = 1.0f): Int {
        return try {
            val pcm = pcmFor(soundType)
            val bytes = pcm.size * 2
            val track = AudioTrack(
                attributes, format, bytes, AudioTrack.MODE_STATIC,
                android.media.AudioManager.AUDIO_SESSION_ID_GENERATE
            )
            track.write(pcm, 0, pcm.size)
            track.setVolume(volume.coerceIn(0.0f, 1.0f))
            activeTracks.add(track)
            track.setNotificationMarkerPosition(pcm.size)
            track.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
                override fun onMarkerReached(t: AudioTrack) = dispose(t)
                override fun onPeriodicNotification(t: AudioTrack) {}
            })
            track.play()
            // Red de seguridad por si el marcador no se dispara
            scope.launch {
                delay(pcm.size * 1000L / sampleRate + 500)
                dispose(track)
            }
            0
        } catch (e: Exception) {
            -1
        }
    }

    private fun dispose(track: AudioTrack) {
        if (activeTracks.remove(track)) {
            try {
                track.stop()
            } catch (_: Exception) {
            }
            track.release()
        }
    }

    /**
     * Reproduce una secuencia de sonidos con delays
     * @param sequence Lista de (SoundType, delayMs, volume)
     */
    fun playSequence(sequence: List<Triple<SoundType, Long, Float>>) {
        scope.launch {
            sequence.forEach { (soundType, delayMs, volume) ->
                if (delayMs > 0) delay(delayMs)
                play(soundType, volume)
            }
        }
    }

    /**
     * Secuencia completa de expiración: 3 beeps (1 por segundo) + silbato final
     */
    fun playExpirationSequence(
        warningSound: SoundType,
        whistleSound: SoundType,
        masterVolume: Float,
        onWarningBeep: (Int) -> Unit = {},
        onWhistle: () -> Unit = {}
    ) {
        scope.launch {
            repeat(3) { i ->
                if (i > 0) delay(1000L)
                if (play(warningSound, masterVolume) != -1) onWarningBeep(i + 1)
            }
            delay(1000L)
            if (play(whistleSound, masterVolume) != -1) onWhistle()
        }
    }

    /** Siempre disponible: se sintetiza bajo demanda. */
    fun isLoaded(soundType: SoundType): Boolean = true

    fun release() {
        activeTracks.toList().forEach { dispose(it) }
        pcmCache.clear()
        scope.cancel()
    }
}
