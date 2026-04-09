# Etapa 1: Build
FROM node:22-alpine AS build
WORKDIR /app

# Copiar archivos de dependencias
COPY package.json package-lock.json* pnpm-lock.yaml* ./

# Instalar dependencias
# Detectar si usa pnpm o npm
RUN if [ -f pnpm-lock.yaml ]; then \
        npm install -g pnpm && pnpm install; \
    else \
        npm ci; \
    fi

# Copiar el código fuente
COPY . .

# Construir la aplicación para producción
RUN if [ -f pnpm-lock.yaml ]; then \
        pnpm run build; \
    else \
        npm run build; \
    fi

# Etapa 2: Servidor Web (Nginx)
FROM nginx:alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copiar los archivos compilados desde la etapa de build
# Nota: Ajusta la ruta si tu output path en angular.json es diferente a dist/smart-stock/browser
# Angular 17+ con application builder suele usar dist/project-name/browser
COPY --from=build /app/dist/smart-stock/browser /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
