package smartStock.mobile.application.dtos

import com.fasterxml.jackson.annotation.JsonProperty

data class SubcategoryResponse(
    @JsonProperty("subcategoriaId")
    val id: Int,
    @JsonProperty("categoriaId")
    val categoryId: Int,
    @JsonProperty("nombre")
    val name: String,
    @JsonProperty("estado")
    val status: Boolean,
)
