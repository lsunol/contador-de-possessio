#!/bin/bash
# Script para generar debug.keystore si no existe
# Ejecutar desde la raíz del proyecto

KEYSTORE_PATH="debug.keystore"
ALIAS="androiddebugkey"
STOREPASS="android"
KEYPASS="android"
VALIDITY=10000
DNAME="CN=Android Debug,O=Android,C=US"

if [ -f "$KEYSTORE_PATH" ]; then
    echo "✅ debug.keystore ya existe"
    keytool -list -v -keystore "$KEYSTORE_PATH" -storepass "$STOREPASS" | head -20
else
    echo "🔑 Generando debug.keystore..."
    keytool -genkey -v \
        -keystore "$KEYSTORE_PATH" \
        -alias "$ALIAS" \
        -keyalg RSA \
        -keysize 2048 \
        -validity "$VALIDITY" \
        -storepass "$STOREPASS" \
        -keypass "$KEYPASS" \
        -dname "$DNAME"
    
    if [ $? -eq 0 ]; then
        echo "✅ debug.keystore generado correctamente"
        echo "📝 Añadido a .gitignore (no se commitea release keystore)"
    else
        echo "❌ Error generando keystore"
        exit 1
    fi
fi