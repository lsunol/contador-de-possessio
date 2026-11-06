# Arquitectura Técnica - Contador de Posesión

## Visión General Arquitectónica

```
┌─────────────────────────────────────────────────────────────────┐
│                        APP MODULE                               │
├─────────────────────────────────────────────────────────────────┤
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────┐  │
│  │   UI Layer   │  │ ViewModel    │  │   Domain Layer       │  │
│  │  (Compose)   │◀─▶│  (StateFlow) │◀─▶│  UseCases + Models   │  │
│  └──────────────┘  └──────────────┘  └──────────┬───────────┘  │
│                                                  │              │
│  ┌──────────────┐  ┌──────────────┐             │              │
│  │  DI (Hilt)   │  │  Data Layer  │─────────────┘              │
│  │  Modules     │  │ (Repository) │                            │
│  └──────────────┘  └──────────────┘                            │
└─────────────────────────────────────────────────────────────────┘
```

---

## Estructura de Paquetes

```
dev.lluissunol.vuitsegons
├── app/                          # Módulo principal
│   ├── di/
│   │   ├── AppModule.kt          # Hilt modules: SoundPool, DataStore, Timer
│   │   └── Qualifiers.kt         # @Named("warningSound") etc.
│   ├── ui/
│   │   ├── main/
│   │   │   ├── MainActivity.kt
│   │   │   ├── MainViewModel.kt
│   │   │   └── MainScreen.kt     # Compose UI principal
│   │   ├── settings/
│   │   │   ├── SettingsActivity.kt
│   │   │   ├── SettingsViewModel.kt
│   │   │   └── SettingsScreen.kt
│   │   ├── components/
│   │   │   ├── SevenSegmentDisplay.kt    # Componente 7-segment personalizado
│   │   │   ├── Segment.kt                # Barra individual animada
│   │   │   ├── PauseOverlay.kt           # Overlay "||" parpadeante
│   │   │   └── TouchArea.kt              # Área touch full-screen
│   │   └── theme/
│   │       ├── Theme.kt
│   │       ├── Color.kt                  # Colores configurables + defaults
│   │       └── Type.kt
│   └── util/
│       ├── OrientationHelper.kt          # reversePortrait utils
│       └── WakeLockManager.kt            # PARTIAL_WAKE_LOCK
│
├── core/                         # Módulo shared (opcional, para futuro)
│   ├── domain/
│   │   ├── model/
│   │   │   ├── TimerState.kt           # Sealed class: Running/Paused/Expired
│   │   │   ├── Settings.kt             # Data class completa settings
│   │   │   └── SoundType.kt            # Enum: BEEP_1, BEEP_2, BEEP_3, WHISTLE
│   │   ├── repository/
│   │   │   └── SettingsRepository.kt   # Interface
│   │   └── usecase/
│   │       ├── GetSettingsUseCase.kt
│   │       ├── SaveSettingsUseCase.kt
│   │       ├── StartTimerUseCase.kt
│   │       ├── PauseTimerUseCase.kt
│   │       └── ResetTimerUseCase.kt
│   └── data/
│       ├── repository/
│       │   └── SettingsRepositoryImpl.kt   # DataStore implementation
│       ├── datasource/
│       │   ├── SettingsDataStore.kt        # Preferences DataStore
│       │   └── SoundPoolDataSource.kt      # SoundPool wrapper
│       └── timer/
│           └── CoroutineTimer.kt           # Timer preciso con Flow
│
└── build.gradle.kts
```

---

## Modelos de Dominio

### TimerState (Sealed Class)
```kotlin
sealed interface TimerState {
    data class Running(
        val remainingMillis: Long,
        val totalMillis: Long,
        val warningThresholdMillis: Long
    ) : TimerState

    data class Paused(
        val remainingMillis: Long,
        val totalMillis: Long
    ) : TimerState

    object Expired : TimerState

    data class Initial(val totalMillis: Long) : TimerState
}
```

### Settings (Data Class)
```kotlin
@Parcelize
data class Settings(
    // Tiempo
    val baseDurationSeconds: Int = 8,
    val warningThresholdSeconds: Int = 3,

    // Audio
    val masterVolume: Float = 1.0f,        // 0.0 - 1.0
    val playTickOnReset: Boolean = true,
    val playWarningBeeps: Boolean = true,
    val playFinalWhistle: Boolean = true,
    val warningSoundType: SoundType = SoundType.BEEP_CLASSIC,
    val whistleSoundType: SoundType = SoundType.WHISTLE_CLASSIC,
    val overrideDoNotDisturb: Boolean = false,

    // Hápticos
    val vibrateOnReset: Boolean = true,
    val vibrateOnWarning: Boolean = true,
    val vibrateOnExpire: Boolean = true,
    val vibrationPattern: VibrationPattern = VibrationPattern.STANDARD,

    // Visual
    val activeColor: ColorInt = 0xFF00FF00.toInt(),      // Verde
    val warningColor: ColorInt = 0xFFFFA500.toInt(),     // Ámbar
    val expiredColor: ColorInt = 0xFFFF0000.toInt(),     // Rojo
    val backgroundColor: ColorInt = 0xFF000000.toInt(),  // Negro
    val keepScreenOn: Boolean = true,
    val digitSize: DigitSize = DigitSize.FULL_SCREEN,
    val forceReversePortrait: Boolean = true,

    // Comportamiento
    val resetGesture: ResetGesture = ResetGesture.SINGLE_TAP,
    val pauseGesture: PauseGesture = PauseGesture.DOUBLE_TAP,
    val autoStart: Boolean = true
) : Parcelable
```

---

## Capa de Datos: DataStore + SoundPool

### SettingsDataStore (Preferences DataStore)
```kotlin
class SettingsDataStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val dataStore = context.preferencesDataStore("settings")
    
    // Flow<Settings> para reactive UI
    val settingsFlow: Flow<Settings> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs -> prefs.toSettings() }
    
    suspend fun save(settings: Settings) { ... }
}
```

### SoundPoolManager (Low Latency Audio)
```kotlin
class SoundPoolManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val soundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    // Sound IDs cargados en init
    private val soundIds = mutableMapOf<SoundType, Int>()
    
    init { loadSounds() }

    fun play(soundType: SoundType, volume: Float = 1.0f) {
        soundIds[soundType]?.let { id ->
            soundPool.play(id, volume, volume, 1, 0, 1.0f)
        }
    }

    fun playSequence(sequence: List<Pair<SoundType, Long>>) { ... } // Con delays
}
```

**Assets de audio requeridos (res/raw/):**
- `beep_classic.ogg` - 100ms, 880Hz
- `beep_soft.ogg` - 100ms, 660Hz  
- `beep_digital.ogg` - 50ms, square wave
- `whistle_classic.ogg` - 500ms, descendente 1200→600Hz
- `whistle_short.ogg` - 300ms, agudo
- `whistle_long.ogg` - 800ms, estilo árbitro fútbol
- `tick_reset.ogg` - 30ms, click suave

---

## Timer Preciso (CoroutineTimer)

```kotlin
class CoroutineTimer @Inject constructor(
    private val scope: CoroutineScope
) {
    fun startTimer(
        durationMillis: Long,
        warningThresholdMillis: Long,
        onTick: (remainingMillis: Long) -> Unit,
        onWarning: (remainingMillis: Long) -> Unit,
        onExpire: () -> Unit
    ): Job = scope.launch(Dispatchers.Default) {
        val startTime = SystemClock.uptimeMillis()
        val endTime = startTime + durationMillis
        val warningTime = endTime - warningThresholdMillis
        var warningTriggered = false

        while (true) {
            val now = SystemClock.uptimeMillis()
            val remaining = maxOf(0L, endTime - now)
            
            onTick(remaining)
            
            if (!warningTriggered && now >= warningTime) {
                warningTriggered = true
                onWarning(remaining)
            }
            
            if (remaining == 0L) {
                onExpire()
                break
            }
            
            // Sleep preciso: próximo segundo o 16ms para smooth UI
            val nextTick = minOf(now + 16, endTime)
            delay(nextTick - now)
        }
    }
}
```

---

## UI: SevenSegmentDisplay (Compose)

### Diseño del Dígito 7-Segment
```
   ┌─── a ───┐
   │         │
 f │         │ b
   │    g    │
   │         │
 e │         │ c
   │         │
   └─── d ───┘   ● dp (punto decimal, no usado)
```

### Mapeo Segmentos → Dígitos
```kotlin
private val DIGIT_SEGMENTS = mapOf(
    0 to setOf('a','b','c','d','e','f'),
    1 to setOf('b','c'),
    2 to setOf('a','b','g','e','d'),
    3 to setOf('a','b','g','c','d'),
    4 to setOf('f','g','b','c'),
    5 to setOf('a','f','g','c','d'),
    6 to setOf('a','f','g','e','c','d'),
    7 to setOf('a','b','c'),
    8 to setOf('a','b','c','d','e','f','g'),
    9 to setOf('a','b','c','d','f','g')
)
```

### Componente Compose
```kotlin
@Composable
fun SevenSegmentDisplay(
    value: Int,                    // 0-99 (segundos)
    color: Color,
    segmentWidth: Dp = 8.dp,
    segmentGap: Dp = 2.dp,
    animate: Boolean = true
) {
    // Renderiza 2 dígitos (decenas + unidades)
    // Cada segmento = RoundedCornerShape + animateColorAsState
    // Rotación 180°: canvas.rotate(180f, centerX, centerY)
}
```

---

## Inyección de Dependencias (Hilt)

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideSoundPoolManager(@ApplicationContext context: Context): SoundPoolManager {
        return SoundPoolManager(context)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(dataStore: SettingsDataStore): SettingsRepository {
        return SettingsRepositoryImpl(dataStore)
    }

    @Provides
    @Singleton
    fun provideCoroutineTimer(@MainDispatcher dispatcher: CoroutineDispatcher): CoroutineTimer {
        return CoroutineTimer(CoroutineScope(dispatcher))
    }

    @Provides
    @Singleton
    fun provideWakeLockManager(@ApplicationContext context: Context): WakeLockManager {
        return WakeLockManager(context)
    }
}
```

---

## ViewModel Principal

```kotlin
@HiltViewModel
class MainViewModel @Inject constructor(
    private val getSettings: GetSettingsUseCase,
    private val startTimer: StartTimerUseCase,
    private val pauseTimer: PauseTimerUseCase,
    private val resetTimer: ResetTimerUseCase,
    private val soundManager: SoundPoolManager,
    private val wakeLock: WakeLockManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<TimerState.Initial(8000))
    val uiState: StateFlow<TimerState> = _uiState

    private var timerJob: Job? = null

    init { observeSettings() }

    fun onScreenTap() { ... }      // Reinicio o reanudación según gesto
    fun onDoubleTap() { ... }      // Pausa
    fun onLongPress() { ... }      // Settings

    private fun startCountdown(settings: Settings) { ... }
    private fun handleExpire() { 
        soundManager.playSequence(listOf(
            SoundType.WARNING_BEEP to 0,
            SoundType.WARNING_BEEP to 1000,
            SoundType.WARNING_BEEP to 2000,
            SoundType.FINAL_WHISTLE to 3000
        ))
    }
}
```

---

## Configuración Gradle (build.gradle.kts)

```kotlin
plugins {
    id("com.android.application") version "8.5.0" apply false
    id("org.jetbrains.kotlin.android") version "2.0.0" apply false
    id("com.google.dagger.hilt.android") version "2.51" apply false
    id("com.google.devtools.ksp") version "2.0.0-1.0.13" apply false
}
```

```kotlin
// app/build.gradle.kts
android {
    namespace = "dev.lluissunol.vuitsegons"
    compileSdk = 34

    defaultConfig {
        applicationId = "dev.lluissunol.vuitsegons"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
        vectorDrawables.useSupportLibrary = true
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.13"
    }

    packagingOptions {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1,LICENSE,NOTICE}/*"
    }
}

dependencies {
    // Core
    implementation(platform("androidx.compose:compose-bom:2024.10.00"))
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.51")
    ksp("com.google.dagger:hilt-compiler:2.51")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.6.0")
    debugImplementation("androidx.fragment:fragment-testing:1.6.0")
}
```

---

## Permisos (AndroidManifest.xml)

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="dev.lluissunol.vuitsegons">

    <!-- WakeLock para mantener pantalla encendida -->
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    
    <!-- Vibración -->
    <uses-permission android:name="android.permission.VIBRATE" />
    
    <!-- Override Do Not Disturb (opcional, requiere consentimiento usuario) -->
    <uses-permission android:name="android.permission.ACCESS_NOTIFICATION_POLICY" />

    <application
        android:name=".PossessionCounterApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:theme="@style/Theme.PossessionCounter"
        android:supportsRtl="true">

        <activity
            android:name=".ui.main.MainActivity"
            android:exported="true"
            android:screenOrientation="reversePortrait"
            android:configChanges="orientation|screenSize|keyboardHidden"
            android:windowSoftInputMode="adjustNothing">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <activity
            android:name=".ui.settings.SettingsActivity"
            android:exported="false"
            android:screenOrientation="reversePortrait"
            android:theme="@style/Theme.PossessionCounter.Settings" />
    </application>
</manifest>
```

---

## Testing Strategy

### Unit Tests
- `SettingsRepositoryImplTest` - DataStore read/write
- `CoroutineTimerTest` - VirtualTimeScheduler, precisión ±50ms
- `SoundPoolManagerTest` - Mock SoundPool, verify play calls
- `MainViewModelTest` - State transitions, gesture handling

### UI Tests (Compose Testing)
- `MainScreenTest` - Render 7-segment, tap actions, pause overlay
- `SettingsScreenTest` - Nav tabs, color pickers, persistence
- `OrientationTest` - Reverse portrait rendering

### Device/Emulator Tests
- API 34 emulator (Pixel 7 Pro skin)
- Physical device: Huawei MateBook X Pro / Android phone
- Test matrix: orientations, background/foreground, screen off

---

## CI/CD (GitHub Actions)

```yaml
# .github/workflows/build.yml
name: Build & Test
on: [push, pull_request]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: 'temurin', java-version: '17' }
      - name: Setup Android SDK
        uses: android-actions/setup-android@v3
      - name: Run Tests
        run: ./gradlew testDebugUnitTest
      - name: Build Debug APK
        run: ./gradlew assembleDebug
      - name: Upload APK
        uses: actions/upload-artifact@v4
        with: { name: app-debug.apk, path: app/build/outputs/apk/debug/*.apk }
```

---

## Checklist de Entrega

- [ ] Proyecto compila sin warnings
- [ ] `./gradlew assembleDebug` genera APK < 10MB
- [ ] `./gradlew assembleRelease` genera APK firmado
- [ ] Tests unitarios > 80% coverage
- [ ] UI tests pasan en emulador API 34
- [ ] APK instala y funciona en dispositivo real
- [ ] Timer precisión verificada ±50ms
- [ ] Audio latencia < 20ms
- [ ] Settings persisten tras kill app
- [ ] Orientación 180° correcta
- [ ] WakeLock funciona (pantalla no se apaga)
- [ ] Documentación README.md completa