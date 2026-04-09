package smartStock.mobile.application.dtos

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

data class ProductResponse(
    @JsonProperty("productoId") val id: Int?,
    @JsonProperty("subcategoriaId") val subcategoryId: Int,
    @JsonProperty("subcategoriaNombre") val subcategoryName: String?,
    @JsonProperty("codigo") val code: String,
    @JsonProperty("nombre") val name: String,
    @JsonProperty("descripcion") val description: String?,
    @JsonProperty("precioUnitario") val unitPrice: BigDecimal,
    @JsonProperty("estado") val status: Boolean,
)

data class ProductCreateRequest(
    @JsonProperty("subcategoriaId") val subcategoryId: Int,
    @JsonProperty("codigo") val code: String,
    @JsonProperty("nombre") val name: String,
    @JsonProperty("descripcion") val description: String?,
    @JsonProperty("precioUnitario") val unitPrice: BigDecimal,
    @JsonProperty("estado") val status: Boolean = true,
)

data class ProductUpdateRequest(
    @JsonProperty("subcategoriaId") val subcategoryId: Int,
    @JsonProperty("codigo") val code: String,
    @JsonProperty("nombre") val name: String,
    @JsonProperty("descripcion") val description: String?,
    @JsonProperty("precioUnitario") val unitPrice: BigDecimal,
)
