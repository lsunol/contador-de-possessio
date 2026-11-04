# Archivos de Audio Requeridos

Coloca los siguientes archivos `.ogg` en este directorio (`app/src/main/res/raw/`):

## Beeps de Advertencia (3 beeps en los últimos 3 segundos)

| Archivo | Descripción | Duración | Frecuencia |
|---------|-------------|----------|------------|
| `beep_classic.ogg` | Beep clásico estilo reloj | 100ms | 880Hz (La5) |
| `beep_soft.ogg` | Beep suave, menos intrusivo | 100ms | 660Hz (Mi5) |
| `beep_digital.ogg` | Beep digital onda cuadrada | 50ms | 1000Hz |

## Silbatos Finales (al llegar a 0)

| Archivo | Descripción | Duración | Características |
|---------|-------------|----------|-----------------|
| `whistle_classic.ogg` | Silbato clásico árbitro fútbol | 500ms | Descendente 1200→600Hz |
| `whistle_short.ogg` | Silbato corto y agudo | 300ms | 2000Hz constante |
| `whistle_long.ogg` | Silbato largo estilo partido | 800ms | Descendente con vibrato |

## Click de Reinicio

| Archivo | Descripción | Duración |
|---------|-------------|----------|
| `tick_reset.ogg` | Click suave mecánico | 30ms |

---

## Generación Rápida (Python)

```python
import numpy as np
import soundfile as sf

def generate_tone(freq, duration_ms, sample_rate=44100, wave='sine', fade_ms=5):
    t = np.linspace(0, duration_ms/1000, int(sample_rate * duration_ms/1000), False)
    if wave == 'sine':
        audio = np.sin(2 * np.pi * freq * t)
    elif wave == 'square':
        audio = np.sign(np.sin(2 * np.pi * freq * t))
    # Fade in/out para evitar clicks
    fade_samples = int(sample_rate * fade_ms/1000)
    audio[:fade_samples] *= np.linspace(0, 1, fade_samples)
    audio[-fade_samples:] *= np.linspace(1, 0, fade_samples)
    return (audio * 32767).astype(np.int16)

def generate_whistle(freq_start, freq_end, duration_ms, sample_rate=44100):
    t = np.linspace(0, duration_ms/1000, int(sample_rate * duration_ms/1000), False)
    freq = np.linspace(freq_start, freq_end, len(t))
    phase = 2 * np.pi * np.cumsum(freq) / sample_rate
    audio = np.sin(phase)
    # Envelope
    env = np.exp(-t * 3)  # Decay exponencial
    audio *= env
    fade = int(sample_rate * 0.01)
    audio[:fade] *= np.linspace(0, 1, fade)
    return (audio * 32767).astype(np.int16)

# Generar archivos
sf.write('beep_classic.ogg', generate_tone(880, 100), 44100, format='OGG')
sf.write('beep_soft.ogg', generate_tone(660, 100), 44100, format='OGG')
sf.write('beep_digital.ogg', generate_tone(1000, 50, wave='square'), 44100, format='OGG')
sf.write('whistle_classic.ogg', generate_whistle(1200, 600, 500), 44100, format='OGG')
sf.write('whistle_short.ogg', generate_tone(2000, 300), 44100, format='OGG')
sf.write('whistle_long.ogg', generate_whistle(1500, 400, 800), 44100, format='OGG')
sf.write('tick_reset.ogg', generate_tone(1500, 30), 44100, format='OGG')
```

## Especificaciones Técnicas

- **Formato**: OGG Vorbis (mejor compresión que WAV, soporte nativo Android)
- **Sample Rate**: 44.1 kHz
- **Canales**: Mono (1 canal)
- **Bit Depth**: 16-bit
- **Normalización**: -3 dBFS (evitar clipping)
- **Latencia objetivo**: < 20ms con SoundPool

## Testing

Verifica latencia con:
```kotlin
val start = SystemClock.uptimeMillis()
soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
val latency = SystemClock.uptimeMillis() - start
// Debe ser < 20ms
```