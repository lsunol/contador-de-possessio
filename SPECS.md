# Especificaciones Funcionales - Contador de Posesión Futbol Botons

## Visión General

Aplicación Android para árbitros de futbol botons (futbol de mesa) que controla el tiempo de posesión de cada jugador. El árbitro lleva el móvil colgado del cuello (boca abajo), por lo que la UI debe funcionar rotada 180°.

---

## Requisitos Funcionales

### RF-01: Contador Regressivo (Countdown)
- **Duración base**: 8 segundos (configurable 1-60s)
- **Visualización**: Grande, tipo 7-segment display (8 barras: 7 segmentos + punto decimal)
- **Comportamiento**: Cuenta atrás permanente (8→7→6...→0)
- **Al llegar a 0**: Se detiene en 0, emite secuencia de audio, vibra

### RF-02: Reinicio por Acción del Árbitro
- **Trigger**: Tap en pantalla (cualquier zona) o botón físico (volumen)
- **Acción**: Reinicia contador a valor configurado inmediatamente
- **Feedback**: Háptico ligero + sonido "tick" opcional

### RF-03: Pausa/Reanudación
- **Trigger**: Doble tap o botón dedicado (configurable)
- **Estados**: RUNNING → PAUSED → RUNNING
- **Visual**: Indicador claro de pausa (icono || parpadeante, color ámbar)

### RF-04: Secuencia de Audio (Crítico)
| Evento | Sonido | Repeticiones | Timing |
|--------|--------|--------------|--------|
| Cada segundo (opcional) | Beep corto | 1 | 50ms |
| **Últimos 3 segundos** (3→2→1) | **Beep leve** | **1 por segundo** | **100ms, tono medio** |
| **Llegada a 0** (exceso tiempo) | **Silbato (whistle)** | **1** | **500ms, tono agudo descendente** |

**Requisitos de audio:**
- Latencia < 20ms (SoundPool)
- Volumen configurable (0-100%)
- Funcionar con pantalla apagada (WakeLock PARTIAL)
- Respetar modo silencio/DND del sistema (opción override)

### RF-05: Orientación 180° (Boca Abajo)
- `android:screenOrientation="reversePortrait"` en Manifest
- Touch coordinates invertidos automáticamente
- 7-segment display legible al revés (diseño simétrico)
- Opción en settings: "Auto-rotar 180°" / "Forzar normal"

### RF-06: Configurabilidad Completa (Settings)

#### Tiempo
- Duración base: 1-60 segundos (default: 8)
- Advertencia sonora: últimos N segundos (default: 3)

#### Audio
- Volumen master: 0-100%
- Sonido "tick" al reiniciar: ON/OFF
- Beeps advertencia: ON/OFF + elegir tono (3 opciones)
- Silbato final: ON/OFF + elegir tono (3 opciones)
- Override modo silencio: ON/OFF

#### Hápticos
- Vibración al reiniciar: ON/OFF
- Vibración en beeps advertencia: ON/OFF
- Vibración fuerte en silbato: ON/OFF
- Patrón vibración personalizable

#### Visual
- Color dígitos activo: ColorPicker (default: verde #00FF00)
- Color dígitos advertencia (≤3s): ColorPicker (default: ámbar #FFA500)
- Color dígitos excedido (0): ColorPicker (default: rojo #FF0000)
- Color fondo: ColorPicker (default: negro #000000)
- Brillo pantalla: Mantener encendida / Sistema / Máximo
- Tamaño dígitos: Pequeño / Mediano / Grande / Pantalla completa

#### Comportamiento
- Reinicio: Tap simple / Doble tap / Botón volumen / Botón encendido
- Pausa: Doble tap / Long press / Botón dedicado
- Auto-iniciar al abrir app: ON/OFF
- Mantener pantalla encendida (WakeLock): ON/OFF

---

## Requisitos No Funcionales

### RNF-01: Rendimiento
- Inicio en frío < 2 segundos
- Frame rate 60fps constante (animaciones 7-segment)
- Latencia audio < 20ms (SoundPool)
- Memoria < 50MB

### RNF-02: Fiabilidad
- Timer preciso (±50ms) usando `SystemClock.uptimeMillis()`
- Persistencia de settings en DataStore
- Recuperación estado tras: rotación, background, reinicio app
- Funcionar sin permisos especiales (excepto WAKE_LOCK opcional)

### RNF-03: Usabilidad
- Touch target mínimo 48dp (toda la pantalla)
- Contraste AA mínimo (WCAG 2.1)
- Funcionar con guantes (touch grueso)
- Feedback inmediato (<100ms) a cualquier acción

### RNF-04: Distribución
- APK installable (debug + release)
- Sin Google Play Services (offline first)
- Min SDK 24 (Android 7.0) - cubre 95%+ dispositivos
- Arquitectura: arm64-v8a + armeabi-v7a (universal)

---

## Flujos de Usuario

### Flujo Principal: Partido
```
1. Árbitro abre app
   ↓
2. Pantalla principal: 7-segment "8" (o valor configurado)
   ↓
3. Jugador A hace jugada → Árbitro TAP pantalla
   ↓
4. Contador reinicia a 8 → Cuenta atrás 8→7→6→5→4→3→2→1→0
   ↓
5. En 3,2,1: Beep leve + vibración opcional cada segundo
   ↓
6. En 0: Silbato + vibración fuerte + pantalla roja
   ↓
7. Jugador B hace jugada → Árbitro TAP → Reinicio a 8
   ↓
8. Repetir hasta fin partido
```

### Flujo: Pausa
```
Partido en curso → Árbitro DOBLE TAP → PAUSA (|| parpadea, color ámbar)
   ↓
Árbitro TAP simple → REANUDA (continúa desde donde paró)
```

### Flujo: Settings
```
Pantalla principal → Long press 3s / Menu 3 puntos → Settings
   ↓
Tabs: Tiempo | Audio | Hápticos | Visual | Comportamiento
   ↓
Cambios → Aplican inmediatamente (preview en tiempo real)
   ↓
Back → Vuelve a principal con nueva config
```

---

## Casos Edge

| Caso | Comportamiento Esperado |
|------|------------------------|
| App en background (Home) | Timer PAUSA, se reanuda al volver |
| Pantalla se apaga (timeout) | WakeLock mantiene encendida si configurado |
| Llamada entrante | Timer PAUSA, notificación sonora si excede |
| Rotación dispositivo | UI se mantiene en reversePortrait |
| Múltiples taps rápidos | Debounce 200ms, un solo reinicio |
| Sonido en modo silencio | Respeta DND salvo override ON |
| Batería baja | Warning visual, reduce brillo si configurado |

---

## Métricas de Éxito

- [ ] APK genera e instala en emulador API 34
- [ ] APK instala en dispositivo real (Huawei MateBook X Pro / móvil)
- [ ] Timer preciso ±50ms verificado con cronómetro externo
- [ ] Audio latencia < 20ms medido
- [ ] 60fps en animaciones 7-segment
- [ ] Settings persisten tras reinicio app
- [ ] Orientación 180° funcional y legible
- [ ] Build release APK < 10MB