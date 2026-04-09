package smartStock.mobile.application.dtos

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class UserUpdateRequest(
    @field:NotNull(message = "El rol es obligatorio")
    @JsonProperty("rolId")
    val roleId: Int,
    @field:NotBlank(message = "El nombre es obligatorio")
    @JsonProperty("nombre")
    val name: String,
    @field:NotNull(message = "La cédula es obligatoria")
    @JsonProperty("cedula")
    val cedula: Int,
    @field:NotBlank(message = "El email es obligatorio")
    @field:Email(message = "Debe ser un email válido")
    @JsonProperty("email")
    val email: String,
    @JsonProperty("password")
    val password: String? = null,
    @JsonProperty("telefono")
    val phone: String? = null,
    @JsonProperty("estado")
    val status: Boolean = true,
)
