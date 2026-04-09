# Frontend Android Kotlin XML para consumir el CRUD de `Usuario`

Este tutorial crea una app Android minima en Kotlin con:

- XML
- `RecyclerView`
- Retrofit
- un solo `MainActivity`
- sin arquitectura extra
- sin ViewModel
- sin Hilt
- sin Clean Architecture

La app hara esto:

- listar usuarios al abrir
- crear usuario desde un formulario
- tocar un item para cargarlo en el formulario y editarlo
- eliminar usuario desde la lista

La API que consumira es esta:

- `GET /usuarios`
- `GET /usuarios/{id}`
- `POST /usuarios`
- `PUT /usuarios/{id}`
- `DELETE /usuarios/{id}`

## 1. Antes de empezar

Debes tener el backend de la guia anterior corriendo en:

```text
http://10.0.2.2:8080/
```

`10.0.2.2` es la forma en que el emulador Android ve el `localhost` de tu PC.

Si usas un celular fisico, cambia la URL por la IP LAN de tu computador. Ejemplo:

```text
http://192.168.1.50:8080/
```

## 2. Crear el proyecto Android

En Android Studio:

1. `New Project`
2. Elige `Empty Views Activity`
3. Language: `Kotlin`
4. Minimum SDK: `24` o superior

Puedes usar este paquete:

```text
com.ejemplo.crudandroid
```

Estructura minima:

```text
app/
  src/main/
    java/com/ejemplo/crudandroid/
      MainActivity.kt
      Usuario.kt
      ApiService.kt
      UsuarioAdapter.kt
    res/layout/
      activity_main.xml
      item_usuario.xml
    AndroidManifest.xml
```

## 3. Dependencias

En `app/build.gradle.kts`, agrega estas dependencias si tu proyecto no las trae:

```kotlin
dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
}
```

Luego sincroniza el proyecto.

## 4. Permiso de internet y trafico HTTP

Como vas a consumir una API `http://`, agrega estas dos cosas en `AndroidManifest.xml`:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.AppCompat.Light.NoActionBar"
        android:usesCleartextTraffic="true">

        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

    </application>

</manifest>
```

Lo importante aqui es:

- `INTERNET`
- `android:usesCleartextTraffic="true"`

Sin eso, la app puede fallar aunque la URL este bien.

Si tu proyecto ya trae otro theme por defecto, no lo cambies. Lo unico obligatorio en este paso es agregar el permiso y `usesCleartextTraffic`.

## 5. Modelo `Usuario`

Crea `Usuario.kt`:

```kotlin
package com.ejemplo.crudandroid

data class Usuario(
    val id: Long? = null,
    val nombre: String,
    val email: String
)
```

## 6. Retrofit

Crea `ApiService.kt`:

```kotlin
package com.ejemplo.crudandroid

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {
    @GET("usuarios")
    fun obtenerUsuarios(): Call<List<Usuario>>

    @GET("usuarios/{id}")
    fun obtenerUsuario(@Path("id") id: Long): Call<Usuario>

    @POST("usuarios")
    fun crearUsuario(@Body usuario: Usuario): Call<Usuario>

    @PUT("usuarios/{id}")
    fun actualizarUsuario(
        @Path("id") id: Long,
        @Body usuario: Usuario
    ): Call<Usuario>

    @DELETE("usuarios/{id}")
    fun eliminarUsuario(@Path("id") id: Long): Call<Void>
}

object ApiClient {
    private const val BASE_URL = "http://10.0.2.2:8080/"

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
```

> Si usas dispositivo fisico, cambia `BASE_URL` por la IP LAN de tu PC.

## 7. Layout principal

Crea `activity_main.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp">

    <TextView
        android:id="@+id/tvModo"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Creando usuario"
        android:textSize="20sp"
        android:textStyle="bold" />

    <EditText
        android:id="@+id/etNombre"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="12dp"
        android:hint="Nombre" />

    <EditText
        android:id="@+id/etEmail"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"
        android:hint="Email"
        android:inputType="textEmailAddress" />

    <Button
        android:id="@+id/btnGuardar"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="12dp"
        android:text="Guardar" />

    <Button
        android:id="@+id/btnCancelarEdicion"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"
        android:text="Cancelar edicion"
        android:visibility="gone" />

    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="16dp"
        android:text="Usuarios guardados"
        android:textStyle="bold" />

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/rvUsuarios"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_marginTop="8dp"
        android:layout_weight="1" />

</LinearLayout>
```

## 8. Layout del item

Crea `item_usuario.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginBottom="8dp"
    android:background="#EEEEEE"
    android:orientation="horizontal"
    android:padding="12dp">

    <LinearLayout
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:orientation="vertical">

        <TextView
            android:id="@+id/tvNombre"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:textStyle="bold" />

        <TextView
            android:id="@+id/tvEmail"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="4dp" />
    </LinearLayout>

    <Button
        android:id="@+id/btnEliminar"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Eliminar" />

</LinearLayout>
```

## 9. Adapter del `RecyclerView`

Crea `UsuarioAdapter.kt`:

```kotlin
package com.ejemplo.crudandroid

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UsuarioAdapter(
    private val onEditar: (Long) -> Unit,
    private val onEliminar: (Long) -> Unit
) : RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder>() {

    private val usuarios = mutableListOf<Usuario>()

    fun actualizarLista(nuevosUsuarios: List<Usuario>) {
        usuarios.clear()
        usuarios.addAll(nuevosUsuarios)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_usuario, parent, false)
        return UsuarioViewHolder(view)
    }

    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {
        holder.bind(usuarios[position])
    }

    override fun getItemCount(): Int = usuarios.size

    inner class UsuarioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNombre: TextView = itemView.findViewById(R.id.tvNombre)
        private val tvEmail: TextView = itemView.findViewById(R.id.tvEmail)
        private val btnEliminar: Button = itemView.findViewById(R.id.btnEliminar)

        fun bind(usuario: Usuario) {
            tvNombre.text = usuario.nombre
            tvEmail.text = usuario.email

            itemView.setOnClickListener {
                usuario.id?.let(onEditar)
            }

            btnEliminar.setOnClickListener {
                usuario.id?.let(onEliminar)
            }
        }
    }
}
```

## 10. `MainActivity.kt`

Crea `MainActivity.kt`:

```kotlin
package com.ejemplo.crudandroid

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private val api = ApiClient.api

    private val adapter = UsuarioAdapter(
        onEditar = { id -> cargarUsuario(id) },
        onEliminar = { id -> eliminarUsuario(id) }
    )

    private lateinit var tvModo: TextView
    private lateinit var etNombre: EditText
    private lateinit var etEmail: EditText
    private lateinit var btnGuardar: Button
    private lateinit var btnCancelarEdicion: Button

    private var usuarioEnEdicionId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvModo = findViewById(R.id.tvModo)
        etNombre = findViewById(R.id.etNombre)
        etEmail = findViewById(R.id.etEmail)
        btnGuardar = findViewById(R.id.btnGuardar)
        btnCancelarEdicion = findViewById(R.id.btnCancelarEdicion)

        val recyclerView = findViewById<RecyclerView>(R.id.rvUsuarios)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        btnGuardar.setOnClickListener {
            guardarUsuario()
        }

        btnCancelarEdicion.setOnClickListener {
            limpiarFormulario()
        }

        cargarUsuarios()
    }

    private fun guardarUsuario() {
        val nombre = etNombre.text.toString().trim()
        val email = etEmail.text.toString().trim()

        if (nombre.isBlank() || email.isBlank()) {
            Toast.makeText(this, "Nombre y email son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        val usuario = Usuario(
            id = usuarioEnEdicionId,
            nombre = nombre,
            email = email
        )

        if (usuarioEnEdicionId == null) {
            crearUsuario(usuario)
        } else {
            actualizarUsuario(usuarioEnEdicionId!!, usuario)
        }
    }

    private fun cargarUsuarios() {
        api.obtenerUsuarios().enqueue(object : Callback<List<Usuario>> {
            override fun onResponse(
                call: Call<List<Usuario>>,
                response: Response<List<Usuario>>
            ) {
                if (response.isSuccessful) {
                    adapter.actualizarLista(response.body().orEmpty())
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "No se pudieron cargar los usuarios",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<Usuario>>, t: Throwable) {
                Toast.makeText(
                    this@MainActivity,
                    "Error de conexion con el backend",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun cargarUsuario(id: Long) {
        api.obtenerUsuario(id).enqueue(object : Callback<Usuario> {
            override fun onResponse(call: Call<Usuario>, response: Response<Usuario>) {
                if (response.isSuccessful && response.body() != null) {
                    val usuario = response.body()!!
                    usuarioEnEdicionId = usuario.id
                    etNombre.setText(usuario.nombre)
                    etEmail.setText(usuario.email)
                    tvModo.text = "Editando usuario"
                    btnGuardar.text = "Actualizar"
                    btnCancelarEdicion.visibility = View.VISIBLE
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "No se pudo cargar el usuario",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<Usuario>, t: Throwable) {
                Toast.makeText(
                    this@MainActivity,
                    "Error de conexion con el backend",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun crearUsuario(usuario: Usuario) {
        api.crearUsuario(usuario).enqueue(object : Callback<Usuario> {
            override fun onResponse(call: Call<Usuario>, response: Response<Usuario>) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        this@MainActivity,
                        "Usuario creado",
                        Toast.LENGTH_SHORT
                    ).show()
                    limpiarFormulario()
                    cargarUsuarios()
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "No se pudo crear el usuario",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<Usuario>, t: Throwable) {
                Toast.makeText(
                    this@MainActivity,
                    "Error de conexion con el backend",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun actualizarUsuario(id: Long, usuario: Usuario) {
        api.actualizarUsuario(id, usuario).enqueue(object : Callback<Usuario> {
            override fun onResponse(call: Call<Usuario>, response: Response<Usuario>) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        this@MainActivity,
                        "Usuario actualizado",
                        Toast.LENGTH_SHORT
                    ).show()
                    limpiarFormulario()
                    cargarUsuarios()
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "No se pudo actualizar el usuario",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<Usuario>, t: Throwable) {
                Toast.makeText(
                    this@MainActivity,
                    "Error de conexion con el backend",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun eliminarUsuario(id: Long) {
        api.eliminarUsuario(id).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        this@MainActivity,
                        "Usuario eliminado",
                        Toast.LENGTH_SHORT
                    ).show()

                    if (usuarioEnEdicionId == id) {
                        limpiarFormulario()
                    }

                    cargarUsuarios()
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "No se pudo eliminar el usuario",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(
                    this@MainActivity,
                    "Error de conexion con el backend",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun limpiarFormulario() {
        usuarioEnEdicionId = null
        etNombre.text.clear()
        etEmail.text.clear()
        tvModo.text = "Creando usuario"
        btnGuardar.text = "Guardar"
        btnCancelarEdicion.visibility = View.GONE
    }
}
```

## 11. Como funciona el flujo

### Listar al abrir

En `onCreate`, la app llama `cargarUsuarios()`.

Eso consume:

```text
GET /usuarios
```

Y llena el `RecyclerView`.

### Crear usuario

1. Escribes `nombre` y `email`.
2. Tocas `Guardar`.
3. Si no hay `usuarioEnEdicionId`, la app llama:

```text
POST /usuarios
```

4. Despues recarga la lista.

### Editar usuario

1. Tocas un item del `RecyclerView`.
2. La app llama:

```text
GET /usuarios/{id}
```

3. Carga el formulario con esos datos.
4. Cambia el boton a `Actualizar`.
5. Al guardar llama:

```text
PUT /usuarios/{id}
```

6. Luego limpia el formulario y recarga la lista.

### Eliminar usuario

1. Tocas `Eliminar`.
2. La app llama:

```text
DELETE /usuarios/{id}
```

3. Despues recarga la lista.

## 12. Prueba manual recomendada

Haz esta secuencia:

1. Levanta el backend Spring.
2. Abre el emulador Android.
3. Ejecuta la app.
4. Verifica que la lista cargue.
5. Crea un usuario.
6. Toca el usuario y editalo.
7. Eliminalo.
8. Revisa en Swagger o MySQL que el cambio de verdad ocurrio.

## 13. Errores comunes

### La app no conecta y usaste `localhost`

En emulador Android, `localhost` apunta al propio emulador, no a tu PC.

Usa:

```text
http://10.0.2.2:8080/
```

### El backend esta apagado

Si la app muestra errores de conexion, revisa que la API siga corriendo.

Prueba desde el navegador de tu PC:

```text
http://localhost:8080/swagger-ui.html
```

### Falta `usesCleartextTraffic`

Si estas usando `http://` y olvidaste esto:

```xml
android:usesCleartextTraffic="true"
```

Android puede bloquear la conexion.

### JSON invalido o validacion fallida

Si mandas un `nombre` vacio o un `email` invalido, el backend puede responder con `400`.

Ejemplo correcto:

```json
{
  "nombre": "Juan",
  "email": "juan@mail.com"
}
```

### La URL cambia en un dispositivo fisico

Si pruebas en celular real, cambia esto:

```kotlin
private const val BASE_URL = "http://10.0.2.2:8080/"
```

Por algo como:

```kotlin
private const val BASE_URL = "http://192.168.1.50:8080/"
```

## 14. Resumen rapido

Con estos archivos ya tienes un frontend Android basico que consume la API CRUD:

- `Usuario.kt`
- `ApiService.kt`
- `UsuarioAdapter.kt`
- `MainActivity.kt`
- `activity_main.xml`
- `item_usuario.xml`

Es un ejemplo simple a proposito. Para aprender o arrancar rapido sirve muy bien. Cuando ya funcione, despues puedes separarlo en capas y mejorarlo.
