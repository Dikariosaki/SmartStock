#!/bin/bash
set -e

echo "Iniciando verificación de estilo para .NET (Backend Web)..."
echo "Ruta actual antes de cambiar: $(pwd)"

cd "Backend Web"

echo "Ruta actual después de cambiar: $(pwd)"

if ! command -v dotnet &> /dev/null; then
    echo "Error: dotnet no está instalado."
    exit 1
fi

echo "Ejecutando 'dotnet format --verify-no-changes'..."

dotnet format --verify-no-changes --verbosity diagnostic

echo "Verificación de estilo .NET completada con éxito."
