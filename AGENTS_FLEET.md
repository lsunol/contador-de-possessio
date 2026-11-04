# Flota de Agentes - Contador de Posesión Futbol Botons

## Configuración de Modelos (OpenRouter Gratuitos)

| Agente | Modelo | ID OpenRouter | Rol Principal |
|--------|--------|---------------|---------------|
| **Arquitecto Principal** | Crouton Alpha | `openrouter/stealth/crouton-alpha` | Arquitectura, decisiones técnicas, revisión de código |
| **Desarrollador Android** | Flint (free) | `openrouter/recursive/flint-v1.0:free` | Implementación Kotlin/Compose, UI, lógica de negocio |
| **Especialista Audio/UI** | Xing4.0 29B A4B (free) | `openrouter/xingchen-agi/xing4.0-29b-a4b:free` | SoundPool, 7-segment display, animaciones, haptics |
| **QA & Testing** | Crouton Alpha | `openrouter/stealth/crouton-alpha` | Tests unitarios, UI tests, emulador, APK building |
| **DevOps/Build** | Flint (free) | `openrouter/recursive/flint-v1.0:free` | Gradle, CI/CD, firma APK, emulador setup |

---

## Descripción de Agentes

### 1. 🏗️ Arquitecto Principal (Crouton Alpha)
**Responsabilidades:**
- Definir arquitectura Clean Architecture + MVVM
- Decidir estructura de módulos (app, core, ui, data)
- Revisar PRs y código crítico
- Documentar decisiones técnicas (ADRs)
- Coordinar integración entre módulos

**Skills activados:** `backend-patterns`, `coding-standards`, `api-design`

---

### 2. 📱 Desarrollador Android (Flint)
**Responsabilidades:**
- Implementar `MainActivity` + Compose navigation
- `ViewModel` con `StateFlow`/`MutableStateFlow`
- Repository pattern para Settings (DataStore/SharedPreferences)
- Inyección de dependencias (Hilt o Koin)
- Ciclo de vida: `onStart`, `onStop`, `onPause` para timer

**Skills activados:** `frontend-patterns`, `coding-standards`, `backend-patterns`

---

### 3. 🎨 Especialista Audio/UI (Xing4.0)
**Responsabilidades:**
- **7-Segment Display**: Componente Compose personalizado con 8 barras (7 segmentos + punto decimal)
- **SoundPool**: Carga y reproducción de 3 beeps + 1 silbato con latencia <20ms
- **Haptics**: `VibrationEffect` para feedback táctil en cada beep
- **Orientación 180°**: `android:screenOrientation="reversePortrait"` + rotación canvas
- **Temas**: Material 3 dynamic colors, modo oscuro/claro, colores configurables
- **Animaciones**: Transiciones suaves entre dígitos, pulso al llegar a 0

**Skills activados:** `frontend-patterns`, `coding-standards`

---

### 4. ✅ QA & Testing (Crouton Alpha)
**Responsabilidades:**
- Tests unitarios: `ViewModel`, `TimerRepository`, `SoundManager`
- Tests UI: Compose Testing (semántica, acciones, assertions)
- Configurar emulador: API 34, skin phone, hardware acceleration
- Test en dispositivo real (adb install)
- Generar APK debug/release: `./gradlew assembleDebug assembleRelease`

**Skills activados:** `e2e-testing`, `backend-patterns`

---

### 5. ⚙️ DevOps/Build (Flint)
**Responsabilidades:**
- `build.gradle.kts` configuración: JDK 17, Kotlin 2.0, Compose BOM
- Versionado: `versionCode`, `versionName`, `buildConfigField`
- Signing config: debug keystore + release keystore (generar con keytool)
- GitHub Actions workflow: build + test + artifact APK
- Configurar emulador headless para CI

**Skills activados:** `backend-patterns`, `coding-standards`

---

## Flujo de Trabajo (Workflow)

```
┌─────────────────┐
│  ARQUITECTO     │ ← Define specs, ADRs, revisa PRs
└────────┬────────┘
         │
         ▼
┌─────────────────┐     ┌──────────────────┐
│  DESARROLLADOR  │────▶│  ESPECIALISTA    │
│  ANDROID        │     │  AUDIO/UI        │
└────────┬────────┘     └────────┬─────────┘
         │                       │
         ▼                       ▼
┌─────────────────────────────────────────┐
│           QA & TESTING                  │
│  (Unit tests + UI tests + Emulador)     │
└────────────────────┬────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────┐
│            DEVOPS/BUILD                 │
│  (APK debug/release + CI/CD)            │
└─────────────────────────────────────────┘
```

---

## Comunicación Entre Agentes

- **Contexto compartido**: `AGENTS_FLEET.md` + `SPECS.md` + `ARCHITECTURE.md`
- **Handoffs**: Cada agente documenta outputs en `docs/handoffs/`
- **Revisión**: Arquitecto aprueba antes de merge a main
- **Daily sync**: Resumen de progreso, bloqueos, decisiones

---

## Stack Tecnológico Confirmado

| Capa | Tecnología | Versión |
|------|------------|---------|
| Language | Kotlin | 2.0+ |
| UI | Jetpack Compose | BOM 2024.10+ |
| Architecture | Clean Architecture + MVVM | - |
| DI | Hilt | 2.51+ |
| Persistence | DataStore (Preferences) | 1.1+ |
| Audio | SoundPool (legacy) / AudioTrack | API 21+ |
| Timing | Coroutines + Flow | 1.8+ |
| Build | Gradle Kotlin DSL | 8.5+ |
| Min SDK | API 24 (Android 7.0) | - |
| Target SDK | API 34 (Android 14) | - |

---

## Próximos Pasos

1. **Arquitecto** crea `SPECS.md` y `ARCHITECTURE.md`
2. **Desarrollador** inicializa proyecto Gradle + estructura paquetes
3. **Especialista** crea componente `SevenSegmentDisplay` + `SoundManager`
4. **Integración**: Timer logic + UI + Audio + Settings
5. **QA**: Tests + Emulador + Device test
6. **DevOps**: APK release + CI/CD