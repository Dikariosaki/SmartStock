#!/bin/bash
set -e

echo "Iniciando verificación de estilo para Frontend Web..."

cd "Frontend Web"

if [ ! -f "package.json" ]; then
    echo "No se encontró package.json, saltando verificación."
    exit 0
fi

# Instalar dependencias si no existen
if [ ! -d "node_modules" ]; then
    echo "Instalando dependencias..."
    npm ci --silent
fi

echo "Ejecutando Linting (ESLint)..."
# Asume que hay un script "lint" en package.json
if npm run | grep -q "lint"; then
    npm run lint
else
    echo "No se encontró script 'lint' en package.json. Ejecutando eslint directamente..."
    # Usamos comillas simples para evitar expansión del shell si fuera necesario,
    # pero npx eslint maneja el glob internamente.
    npx eslint "src/**/*.{ts,html}" --max-warnings=0
fi

echo "Verificación de estilo Frontend Web completada con éxito."
