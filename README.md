# SmartStock Monorepo

Guia para clonar, configurar y ejecutar todo el proyecto sin romper el entorno:
- `Backend Web` (.NET 9)
- `Backend mobile` (Spring Boot + Kotlin + Java 24)
- `Frontend Web` (Angular)
- `Frontend mobile` (Android/Jetpack Compose)
- MySQL (Docker)

## 1. Requisitos

Instala estas herramientas antes de iniciar:

1. Docker Desktop
2. .NET SDK 9 (`dotnet --version`)
3. Node.js 20+ y npm (o pnpm)
4. Java 24 para `Backend mobile`
5. Android Studio + Android SDK (para `Frontend mobile`)
6. Un emulador Android activo (recomendado)

## 2. Clonar repositorio

```bash
git clone <URL_DEL_REPO>
cd SmartStock-monorepo
```

## 3. Levantar base de datos (obligatorio)

Desde la raiz:

```bash
docker compose up -d mysql seed
```

Validar:

```bash
docker ps
docker logs smartstock-seed
```

MySQL queda en:
- Host: `localhost`
- Puerto: `4000`
- DB: `proyect_smartstock`
- Usuario: `root`
- Password: `secret`

## 4. Ejecutar Backend Web (.NET)

```bash
cd "Backend Web/SmartStock.API"
dotnet restore
dotnet run
```

URLs utiles:
- API: `http://localhost:5085`
- Swagger: `http://localhost:5085/swagger`

## 5. Ejecutar Backend mobile (Spring Boot)

```bash
cd "Backend mobile"
# Windows
mvnw.cmd -DskipTests compile
mvnw.cmd spring-boot:run
```

Puerto de ejecucion:
- API mobile: `http://localhost:9099`
- Swagger mobile: `http://localhost:9099/swagger-ui/index.html`

## 6. Ejecutar Frontend Web (Angular)

```bash
cd "Frontend Web"
npm install
npm start
```

Por defecto abre en:
- `http://localhost:4200`

## 7. Ejecutar Frontend mobile (Android)

1. Abre `Frontend mobile` en Android Studio.
2. Crea `Frontend mobile/local.properties` a partir de `Frontend mobile/local.properties.example`.
3. Verifica `sdk.dir` en ese archivo.
4. Verifica `Frontend mobile/gradle.properties`:
   - `LOCAL_API_BASE_URL=http://10.0.2.2:9099/`
5. Ejecuta en emulador con:
   - `Run 'app'` desde Android Studio
   - o `gradlew.bat :app:installDebug`

Importante:
- `10.0.2.2` funciona para emulador Android.
- Si usas celular fisico, cambia `LOCAL_API_BASE_URL` por la IP LAN de tu PC.

## 8. Opcion Docker (todo en contenedores)

Desde la raiz:

```bash
docker compose up -d --build
```

Servicios:
- Frontend Web: `http://localhost`
- Backend Web: `http://localhost:8080`
- Backend mobile: `http://localhost:9099`
- MySQL: `localhost:4000`

## 9. Problemas comunes

1. `404` al iniciar sesion en mobile:
   - revisa que `LOCAL_API_BASE_URL` apunte a `http://10.0.2.2:9099/`
   - revisa que `Backend mobile` este arriba

2. Error Android por `local.properties`:
   - crea el archivo y ajusta `sdk.dir`

3. Puerto ocupado:
   - cierra procesos que usen `5085`, `9099`, `4200`, `4000`

4. Maven/Gradle no descargan dependencias:
   - valida internet/proxy corporativo

5. Carpeta `.gradle-cache` en la raiz:
   - es cache local, no es necesaria para versionar
   - se puede borrar sin romper el proyecto
   - comando recomendado:
     - `cd "Frontend mobile" && gradlew.bat --stop`
     - luego borra `.gradle-cache` en la raiz si existe

## 10. Reglas de versionado importantes

No subas archivos locales o temporales:
- `local.properties`
- `.env` reales
- carpetas `build`, `target`, `bin`, `obj`, `node_modules`

Si necesitas ejemplos de configuracion, sube archivos `*.example` (ya incluido `local.properties.example` para Android).
