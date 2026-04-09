package smartStock.mobile.application.dtos

import com.fasterxml.jackson.annotation.JsonProperty

data class RoleResponse(
    @JsonProperty("rolId")
    val id: Int,
    @JsonProperty("nombre")
    val name: String,
)
