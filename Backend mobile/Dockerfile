# --- Etapa 1: Construcción (Build) ---
# Usamos una imagen con JDK 24 para compilar
FROM eclipse-temurin:24-jdk-noble AS build
WORKDIR /workspace

# Copiamos la configuración de Maven
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Damos permisos de ejecución al wrapper
RUN chmod +x mvnw

# Descargamos dependencias (Capa cacheada si no cambia el pom.xml)
RUN ./mvnw dependency:go-offline

# Copiamos el código fuente y compilamos
COPY src ./src
RUN ./mvnw clean package -DskipTests

# --- Etapa 2: Ejecución (Run) ---
# Usamos una imagen JRE (Runtime Environment) más ligera, basada en Alpine o una distro mínima si es posible
# Nota: Para Java 24, las imágenes "jre" específicas pueden ser limitadas aún, usamos la jdk-noble slim o similar
FROM eclipse-temurin:24-jre-noble
WORKDIR /app

# Copiamos solo el JAR construido
COPY --from=build /workspace/target/*.jar app.jar

# Usuario no root por seguridad (opcional pero recomendado)
# RUN addgroup -S spring && adduser -S spring -G spring
# USER spring:spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
