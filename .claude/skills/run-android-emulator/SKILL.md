---
name: run-android-emulator
description: Arranca el emulador Android de este proyecto, compila la app "Contador de Posesión" y la instala y lanza para poder probarla. Usar cuando el usuario pida ejecutar, probar, instalar o ver la app Android funcionando (p.ej. "prueba la app", "arranca un emulador", "instala el APK").
---

# Arrancar emulador e instalar/probar la app (contador-de-possessio)

Este archivo está duplicado en `.claude/skills/run-android-emulator/SKILL.md`
para que el harness de Claude Code lo descubra automáticamente. Si editas
este playbook, actualiza también esa copia — en este entorno Windows los
symlinks no funcionan de forma fiable sin privilegios elevados (un `ln -s`
cae silenciosamente a una copia de directorio en vez de crear un enlace
real), así que se mantienen dos copias en vez de un enlace.

Esta es una app Android nativa (Kotlin + Jetpack Compose, Gradle), **no**
una app web ni un CLI. El flujo "run" genérico de browser/CLI no aplica.

## Datos específicos de este proyecto

- SDK de Android: variable de entorno `ANDROID_HOME`
  (normalmente `C:\Users\<usuario>\AppData\Local\Android\Sdk`).
- `applicationId`: `dev.lluissunol.vuitsegons`
  (ver `app/build.gradle.kts`).
- El build **debug** añade el sufijo `.debug`, así que el paquete instalado
  es `dev.lluissunol.vuitsegons.debug`.
- Actividad principal: `dev.lluissunol.vuitsegons.ui.main.MainActivity`.
- APK de debug generado en: `app/build/outputs/apk/debug/app-debug.apk`.
- AVD disponible (comprobar con `emulator -list-avds`, puede cambiar):
  `opentv36` (API 36, `google_apis`, `x86_64`, skin de teléfono 1080x2400
  pese al nombre "tv").

## Pasos

Ejecutar en Bash (Git Bash / MSYS). Todas las rutas usan `$ANDROID_HOME`.

### 1. Comprobar el SDK y listar AVDs

```bash
echo "$ANDROID_HOME"
"$ANDROID_HOME/emulator/emulator" -list-avds
"$ANDROID_HOME/platform-tools/adb" devices
```

Si ya hay un dispositivo `device` (no `offline`) en la lista de `adb
devices`, salta al paso 3 (compilar/instalar).

### 2. Arrancar el emulador en segundo plano

Usar el primer AVD de la lista anterior (o preguntar si hay varios y no
está claro cuál usar). Lanzar SIEMPRE en background con `run_in_background`
(el proceso del emulador no termina solo):

```bash
cd "$ANDROID_HOME/emulator" && nohup ./emulator -avd <nombre_avd> -no-snapshot-load > /tmp/emulator.log 2>&1 &
disown
```

Luego esperar a que arranque del todo (tarda hasta ~2 minutos):

```bash
"$ANDROID_HOME/platform-tools/adb" wait-for-device
for i in $(seq 1 40); do
  boot=$("$ANDROID_HOME/platform-tools/adb" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')
  [ "$boot" = "1" ] && break
  sleep 3
done
"$ANDROID_HOME/platform-tools/adb" devices
```

Avisos benignos esperables en el log (`Failed to load opengl32sw`,
`WARNING | Please update the emulator...`): no son errores, el emulador
cae de forma automática a OpenGL por GPU host (WHPX) y sigue arrancando.

### 3. Compilar el APK de debug

```bash
./gradlew assembleDebug --console=plain
```

Si ya estaba compilado y no hay cambios, Gradle devuelve `UP-TO-DATE` en
todas las tareas — no hace falta `clean`.

### 4. Instalar en el emulador

```bash
"$ANDROID_HOME/platform-tools/adb" install -r app/build/outputs/apk/debug/app-debug.apk
```

### 5. Lanzar la app

```bash
"$ANDROID_HOME/platform-tools/adb" shell monkey -p dev.lluissunol.vuitsegons.debug -c android.intent.category.LAUNCHER 1
```

### 6. Verificar que está en primer plano y mirar la pantalla

```bash
"$ANDROID_HOME/platform-tools/adb" shell dumpsys window | grep -i mCurrentFocus
"$ANDROID_HOME/platform-tools/adb" exec-out screencap -p > /tmp/screenshot.png
cygpath -w /tmp/screenshot.png
```

`mCurrentFocus` debe mostrar
`dev.lluissunol.vuitsegons.debug/dev.lluissunol.vuitsegons.ui.main.MainActivity`.

**Importante**: el Bash tool corre sobre MSYS, así que `/tmp` NO es
`C:\Windows\Temp` ni `C:\Users\<usuario>\AppData\Local\Temp` — se resuelve
a algo como `C:\msys64\tmp`. Para que la herramienta `Read` (que espera
rutas de Windows) pueda abrir el PNG, convierte la ruta con
`cygpath -w <ruta_unix>` y usa esa ruta Windows al leer la imagen.

## Para iterar después de cambiar código

Repetir solo los pasos 3-6 (el emulador ya está arrancado):
`assembleDebug` → `adb install -r` → `monkey` → captura de verificación.
`adb install -r` reinstala conservando datos de la app; usar
`adb uninstall dev.lluissunol.vuitsegons.debug` antes si se necesita una
instalación limpia.
