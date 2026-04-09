package smartStock.mobile.application.dtos

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class AuthLoginRequest(
    @field:NotBlank(message = "El email es obligatorio")
    @field:Email(message = "Debe ser un email válido")
    @JsonProperty("email")
    val email: String,
    @field:NotBlank(message = "La contraseña es obligatoria")
    @JsonProperty("password")
    val password: String,
)

data class AuthLoginResponse(
    @JsonProperty("token")
    val token: String,
    @JsonProperty("usuarioId")
    val userId: Int,
    @JsonProperty("rolId")
    val roleId: Int,
    @JsonProperty("rolNombre")
    val roleName: String,
    @JsonProperty("nombre")
    val name: String,
    @JsonProperty("email")
    val email: String,
)
