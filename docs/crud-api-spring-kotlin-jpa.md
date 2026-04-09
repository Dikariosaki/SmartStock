# CRUD basico con Spring Boot, Kotlin, JPA y MySQL

Este tutorial construye una API CRUD minima para `Usuario` con el menor numero de clases posible:

- `CrudBasicoApplication.kt`
- `Usuario.kt`
- `UsuarioRepository.kt`
- `UsuarioController.kt`
- `application.properties`

La idea es hacerlo funcional, simple y rapido:

- sin arquitectura por capas
- sin DTOs
- sin seguridad
- usando la entidad directo en el controller a proposito

## 1. Lo que vas a construir

Modelo del tutorial:

```json
{
  "id": 1,
  "nombre": "Juan",
  "email": "juan@mail.com"
}
```

Endpoints:

- `GET /usuarios`
- `GET /usuarios/{id}`
- `POST /usuarios`
- `PUT /usuarios/{id}`
- `DELETE /usuarios/{id}`

Al final tendras:

- MySQL 8 corriendo en Docker
- Spring Boot conectado a MySQL
- tabla `usuarios` creada automaticamente por JPA
- Swagger disponible en `http://localhost:8080/swagger-ui.html`

## 2. Crear el proyecto

La forma mas facil es crear el proyecto con **Spring Initializr** y despues reemplazar el contenido por el de esta guia.

Usa estas opciones:

- Project: `Maven`
- Language: `Kotlin`
- Spring Boot: `4.0.3`
- Group: `com.ejemplo`
- Artifact: `crud-basico-api`
- Name: `crud-basico-api`
- Package name: `com.ejemplo.crudbasico`
- Java: `21`

Dependencias:

- `Spring Web`
- `Spring Data JPA`
- `Validation`
- `MySQL Driver`

Despues de generar el proyecto, deja esta estructura:

```text
crud-basico-api/
  pom.xml
  mvnw
  mvnw.cmd
  src/
    main/
      kotlin/
        com/ejemplo/crudbasico/
          CrudBasicoApplication.kt
          Usuario.kt
          UsuarioRepository.kt
          UsuarioController.kt
      resources/
        application.properties
```

## 3. Crear la base de datos con Docker

### Opcion mas simple

Este comando levanta MySQL 8 y crea la base `crud_basico` automaticamente:

```bash
docker run --name mysql-crud -e MYSQL_ROOT_PASSWORD=secret -e MYSQL_DATABASE=crud_basico -p 3306:3306 -d mysql:8.0
```

Revisar que este arriba:

```bash
docker ps
docker logs mysql-crud
```

Entrar a MySQL para revisar:

```bash
docker exec -it mysql-crud mysql -uroot -psecret
```

Ya dentro de MySQL:

```sql
SHOW DATABASES;
USE crud_basico;
SHOW TABLES;
```

Todavia no veras la tabla `usuarios`. JPA la creara cuando levantes Spring Boot.

## 4. `pom.xml`

Reemplaza tu `pom.xml` por este:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>4.0.3</version>
        <relativePath />
    </parent>

    <groupId>com.ejemplo</groupId>
    <artifactId>crud-basico-api</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>crud-basico-api</name>

    <properties>
        <java.version>21</java.version>
        <kotlin.version>2.2.21</kotlin.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>2.8.3</version>
        </dependency>

        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>

        <dependency>
            <groupId>org.jetbrains.kotlin</groupId>
            <artifactId>kotlin-reflect</artifactId>
        </dependency>

        <dependency>
            <groupId>org.jetbrains.kotlin</groupId>
            <artifactId>kotlin-stdlib</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <sourceDirectory>${project.basedir}/src/main/kotlin</sourceDirectory>
        <testSourceDirectory>${project.basedir}/src/test/kotlin</testSourceDirectory>

        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>

            <plugin>
                <groupId>org.jetbrains.kotlin</groupId>
                <artifactId>kotlin-maven-plugin</artifactId>
                <configuration>
                    <args>
                        <arg>-Xjsr305=strict</arg>
                    </args>
                    <compilerPlugins>
                        <plugin>spring</plugin>
                        <plugin>jpa</plugin>
                    </compilerPlugins>
                </configuration>
                <dependencies>
                    <dependency>
                        <groupId>org.jetbrains.kotlin</groupId>
                        <artifactId>kotlin-maven-allopen</artifactId>
                        <version>${kotlin.version}</version>
                    </dependency>
                    <dependency>
                        <groupId>org.jetbrains.kotlin</groupId>
                        <artifactId>kotlin-maven-noarg</artifactId>
                        <version>${kotlin.version}</version>
                    </dependency>
                </dependencies>
            </plugin>
        </plugins>
    </build>

</project>
```

## 5. Configurar la conexion a MySQL

Crea `src/main/resources/application.properties`:

```properties
spring.application.name=crud-basico-api
server.port=8080

spring.datasource.url=jdbc:mysql://localhost:3306/crud_basico?serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=secret
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
```

Puntos importantes:

- `ddl-auto=update` hace que JPA cree o actualice la tabla.
- `server.port=8080` deja la API en `http://localhost:8080`.
- `swagger-ui.html` deja Swagger en una URL corta y facil.

## 6. Crear el codigo

### `CrudBasicoApplication.kt`

```kotlin
package com.ejemplo.crudbasico

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CrudBasicoApplication

fun main(args: Array<String>) {
    runApplication<CrudBasicoApplication>(*args)
}
```

### `Usuario.kt`

```kotlin
package com.ejemplo.crudbasico

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

@Entity
@Table(name = "usuarios")
class Usuario(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @field:NotBlank(message = "El nombre es obligatorio")
    var nombre: String = "",

    @field:Email(message = "El email no es valido")
    @field:NotBlank(message = "El email es obligatorio")
    var email: String = ""
)
```

### `UsuarioRepository.kt`

```kotlin
package com.ejemplo.crudbasico

import org.springframework.data.jpa.repository.JpaRepository

interface UsuarioRepository : JpaRepository<Usuario, Long>
```

### `UsuarioController.kt`

```kotlin
package com.ejemplo.crudbasico

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/usuarios")
class UsuarioController(
    private val usuarioRepository: UsuarioRepository
) {

    @GetMapping
    fun listar(): List<Usuario> = usuarioRepository.findAll()

    @GetMapping("/{id}")
    fun obtenerPorId(@PathVariable id: Long): Usuario =
        usuarioRepository.findById(id)
            .orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado")
            }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun crear(@Valid @RequestBody usuario: Usuario): Usuario {
        usuario.id = null
        return usuarioRepository.save(usuario)
    }

    @PutMapping("/{id}")
    fun actualizar(
        @PathVariable id: Long,
        @Valid @RequestBody datos: Usuario
    ): Usuario {
        val usuario = usuarioRepository.findById(id)
            .orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado")
            }

        usuario.nombre = datos.nombre
        usuario.email = datos.email

        return usuarioRepository.save(usuario)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun eliminar(@PathVariable id: Long) {
        val usuario = usuarioRepository.findById(id)
            .orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado")
            }

        usuarioRepository.delete(usuario)
    }
}
```

> Nota: aqui usamos la entidad `Usuario` directamente en el controller. En proyectos reales normalmente se separa con DTOs y services, pero en este tutorial lo hacemos asi porque la meta es un CRUD minimo y facil de entender.

## 7. Levantar la API

Desde la carpeta del proyecto:

```bash
mvnw.cmd spring-boot:run
```

Si estas en Mac o Linux:

```bash
./mvnw spring-boot:run
```

Si todo salio bien:

- API: `http://localhost:8080`
- Swagger: `http://localhost:8080/swagger-ui.html`

## 8. Verificar que JPA creo la tabla

En otra terminal:

```bash
docker exec -it mysql-crud mysql -uroot -psecret -e "USE crud_basico; SHOW TABLES; DESCRIBE usuarios;"
```

Deberias ver la tabla `usuarios`.

Tambien puedes listar datos:

```bash
docker exec -it mysql-crud mysql -uroot -psecret -e "USE crud_basico; SELECT * FROM usuarios;"
```

## 9. Probar el CRUD

### Crear usuario

```bash
curl.exe -X POST http://localhost:8080/usuarios -H "Content-Type: application/json" -d "{\"nombre\":\"Juan\",\"email\":\"juan@mail.com\"}"
```

Respuesta esperada:

```json
{
  "id": 1,
  "nombre": "Juan",
  "email": "juan@mail.com"
}
```

### Listar usuarios

```bash
curl.exe http://localhost:8080/usuarios
```

### Obtener usuario por id

```bash
curl.exe http://localhost:8080/usuarios/1
```

### Actualizar usuario

```bash
curl.exe -X PUT http://localhost:8080/usuarios/1 -H "Content-Type: application/json" -d "{\"nombre\":\"Juan Perez\",\"email\":\"juan.perez@mail.com\"}"
```

### Eliminar usuario

```bash
curl.exe -X DELETE http://localhost:8080/usuarios/1
```

## 10. Probar desde Swagger

1. Abre `http://localhost:8080/swagger-ui.html`
2. Busca el grupo `usuario-controller`
3. Prueba:
   - `POST /usuarios`
   - `GET /usuarios`
   - `GET /usuarios/{id}`
   - `PUT /usuarios/{id}`
   - `DELETE /usuarios/{id}`

Esto es lo mas rapido para probar la API sin escribir comandos.

## 11. Resumen del flujo

1. Docker levanta MySQL.
2. Spring Boot se conecta usando `application.properties`.
3. Hibernate crea la tabla `usuarios`.
4. `UsuarioRepository` guarda y consulta datos.
5. `UsuarioController` expone el CRUD por HTTP.

## 12. Errores comunes

### MySQL no levanta

Revisa:

```bash
docker ps
docker logs mysql-crud
```

Si el contenedor se detuvo, borralo y crealo de nuevo:

```bash
docker rm -f mysql-crud
docker run --name mysql-crud -e MYSQL_ROOT_PASSWORD=secret -e MYSQL_DATABASE=crud_basico -p 3306:3306 -d mysql:8.0
```

### Puerto `3306` ocupado

Otro MySQL ya esta usando ese puerto. Cambia el puerto del contenedor, por ejemplo `3307:3306`, y luego cambia tambien `application.properties`.

Ejemplo:

```properties
spring.datasource.url=jdbc:mysql://localhost:3307/crud_basico?serverTimezone=UTC
```

### Puerto `8080` ocupado

Cambia el puerto en `application.properties`:

```properties
server.port=8081
```

Y entra a Swagger usando:

```text
http://localhost:8081/swagger-ui.html
```

### JSON invalido

Si mandas un JSON mal formado o vacio, Spring respondera con `400 Bad Request`.

Ejemplo correcto:

```json
{
  "nombre": "Juan",
  "email": "juan@mail.com"
}
```

### El backend esta apagado

Si `curl` o Swagger no responden, asegurate de que `mvnw.cmd spring-boot:run` siga corriendo.

## 13. Prueba final recomendada

Haz esta secuencia completa:

1. Levanta MySQL con Docker.
2. Levanta Spring Boot.
3. Crea un usuario.
4. Lista usuarios.
5. Edita el usuario.
6. Borra el usuario.
7. Verifica en MySQL que el cambio ocurrio realmente.

Con eso ya tienes un CRUD basico y funcional con Spring Boot, Kotlin, JPA y MySQL.
