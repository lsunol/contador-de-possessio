# Contador de Posesión - Futbol Botons

Aplicación Android para árbitros de futbol botons (futbol de mesa) que controla el tiempo de posesión de cada jugador.

## 🎯 Características Principales

- **Countdown de 8 segundos** (configurable 1-60s) con display 7-segment grande
- **Orientación 180°** (boca abajo) - el árbitro lleva el móvil colgado del cuello
- **Audio de baja latencia** (SoundPool): 3 beeps advertencia + 1 silbato final
- **Hápticos**: Vibración en cada evento (configurable)
- **Totalmente configurable**: Tiempo, sonidos, colores, gestos, vibración
- **Offline-first**: Sin dependencias de Google Play Services
- **APK instalable** (< 10MB release)

## 📱 Capturas de Pantalla

| Principal | Settings | Pausa |
|-----------|----------|-------|
| ![Main](docs/screenshots/main.png) | ![Settings](docs/screenshots/settings.png) | ![Pause](docs/screenshots/pause.png) |

## 🛠 Stack Tecnológico

| Capa | Tecnología |
|------|------------|
| Language | Kotlin 2.0 |
| UI | Jetpack Compose (Material 3) |
| Architecture | Clean Architecture + MVVM |
| DI | Hilt |
| Persistence | DataStore Preferences |
| Audio | SoundPool (low latency) |
| Timing | Coroutines + SystemClock.uptimeMillis() |
| Build | Gradle Kotlin DSL 8.5 |
| Min SDK | API 24 (Android 7.0) |
| Target SDK | API 34 (Android 14) |

## 🚀 Instalación Rápida

### Opción 1: APK Directo (Recomendado)
1. Descarga el último `app-release.apk` de [Releases](https://github.com/lluis/contador-de-possessio/releases)
2. Transfiere al móvil
3. Instala (habilita "Orígenes desconocidos" si es necesario)

### Opción 2: Compilar desde Código
```bash
# Requisitos: JDK 17, Android SDK API 34
git clone https://github.com/lluis/contador-de-possessio.git
cd contador-de-possessio
./gradlew assembleDebug      # APK debug en app/build/outputs/apk/debug/
./gradlew assembleRelease    # APK release firmado en app/build/outputs/apk/release/
```

## 📖 Uso

### Flujo Básico (Partido)
1. Abre la app → Muestra "08" (o tiempo configurado)
2. Jugador hace jugada → **Tap en pantalla** → Contador reinicia a 8
3. Cuenta atrás: 8→7→6→5→4→**3→2→1** (beeps) → **0** (silbato + vibración fuerte)
4. Siguiente jugador → **Tap** → Repite

### Gestos Configurables
| Acción | Por Defecto | Alternativas |
|--------|-------------|--------------|
| Reiniciar | Tap simple | Doble tap, Botón volumen, Botón encendido |
| Pausar | Doble tap | Pulsación larga, Triple tap, Volumen - |

### Pausa
- **Doble tap** (configurable) → Muestra "||" parpadeante en ámbar
- **Tap simple** → Reanuda desde donde paró

## ⚙️ Configuración Completa

Accede desde el icono ⚙️ (esquina superior derecha) o pulsación larga 3s.

### Tiempo
- **Duración base**: 1-60 segundos (default: 8)
- **Advertencia en últimos**: 1-30 segundos (default: 3)

### Audio
- **Volumen maestro**: 0-100%
- **Click al reiniciar**: ON/OFF
- **Beeps advertencia**: ON/OFF + 3 tonos
- **Silbato final**: ON/OFF + 3 tonos
- **Ignorar modo silencio**: ON/OFF

### Hápticos
- Vibrar al reiniciar / en advertencia / al expirar
- **Patrones**: Estándar, Doble, Triple, Largo, Latido

### Visual
- **Colores**: Activo (verde), Advertencia (ámbar), Expirado (rojo), Fondo (negro)
- **Tamaño dígitos**: Pequeño / Mediano / Grande / Pantalla completa
- **Mantener pantalla encendida**: ON/OFF (WakeLock)
- **Forzar 180°**: ON/OFF

### Comportamiento
- Gestos personalizados para reiniciar y pausar
- Auto-iniciar al abrir app

## 🔧 Desarrollo

### Estructura del Proyecto
```
app/src/main/java/com/lluis/possessioncounter/
├── core/
│   ├── domain/           # Modelos, Repository interfaces, UseCases
│   └── data/             # Implementaciones (DataStore, SoundPool, Timer)
├── ui/
│   ├── main/             # MainActivity, MainViewModel, MainScreen
│   ├── settings/         # SettingsActivity, SettingsViewModel, SettingsScreen
│   ├── components/       # SevenSegmentDisplay, PauseOverlay, TouchArea
│   └── theme/            # Theme, Color, Type
├── util/                 # WakeLockManager, OrientationHelper
└── di/                   # Hilt Modules
```

### Comandos Útiles
```bash
# Tests unitarios
./gradlew testDebugUnitTest

# Tests UI (requiere emulador corriendo)
./gradlew connectedDebugAndroidTest

# Lint
./gradlew lintDebug

# Generar APKs
./gradlew assembleDebug assembleRelease

# Limpiar
./gradlew clean
```

### Emulador Recomendado
- Device: Pixel 7 Pro (o similar)
- API: 34 (Android 14)
- ABI: arm64-v8a
- RAM: 2GB+
- GPU: Swiftshader / Host

## 📦 Generar Keystore para Release

```bash
# Debug keystore (ya incluido en repo)
keytool -genkey -v -keystore debug.keystore -alias androiddebugkey -keyalg RSA -keysize 2048 -validity 10000 -storepass android -keypass android -dname "CN=Android Debug,O=Android,C=US"

# Release keystore (NO commitear, usar secrets en CI)
keytool -genkey -v -keystore release.keystore -alias release -keyalg RSA -keysize 2048 -validity 10000 -storepass <password> -keypass <password> -dname "CN=Lluis,O=ContadorPosesion,C=ES"
```

## 🎵 Archivos de Audio Necesarios

Coloca en `app/src/main/res/raw/`:
- `beep_classic.ogg`, `beep_soft.ogg`, `beep_digital.ogg`
- `whistle_classic.ogg`, `whistle_short.ogg`, `whistle_long.ogg`
- `tick_reset.ogg`

Ver [README_AUDIO.md](app/src/main/res/raw/README_AUDIO.md) para generar con Python.

## 🤝 Contribuir

1. Fork el repo
2. Crea rama feature (`git checkout -b feature/nueva-funcionalidad`)
3. Commit cambios (`git commit -am 'Add nueva funcionalidad'`)
4. Push (`git push origin feature/nueva-funcionalidad`)
5. Abre Pull Request

## 📄 Licencia

Apache License 2.0 - ver [LICENSE](LICENSE)

## 👨‍💻 Autor

**Lluís** - Desarrollado para árbitros de futbol botons

---

*¿Problemas? Abre un [Issue](https://github.com/lluis/contador-de-possessio/issues)*