package smartStock.mobile.application.dtos

import com.fasterxml.jackson.annotation.JsonProperty

data class UserResponse(
    @JsonProperty("usuarioId")
    val id: Int?,
    @JsonProperty("rolId")
    val roleId: Int,
    @JsonProperty("rolNombre")
    val roleName: String?,
    @JsonProperty("nombre")
    val name: String,
    @JsonProperty("cedula")
    val cedula: Int,
    @JsonProperty("email")
    val email: String,
    @JsonProperty("estado")
    val status: Boolean,
    @JsonProperty("telefono")
    val phone: String?,
)
