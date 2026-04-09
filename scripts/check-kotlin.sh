#!/bin/bash
set -e

echo "Iniciando verificación de estilo para Kotlin (Backend Mobile)..."

cd "Backend mobile"

# Usamos mvnw para ejecutar ktlint si está configurado en el pom, 
# o podemos descargar ktlint directamente si preferimos.
# Aquí asumimos que usaremos el plugin de maven o una descarga directa.

KTLINT_VERSION=1.0.1
KTLINT_JAR="ktlint"

if [ ! -f "$KTLINT_JAR" ]; then
    echo "Descargando ktlint..."
    curl -sSLO https://github.com/pinterest/ktlint/releases/download/$KTLINT_VERSION/ktlint
    chmod a+x ktlint
fi

echo "Ejecutando ktlint..."
./ktlint "src/**/*.kt"

echo "Verificación de estilo Kotlin completada con éxito."
