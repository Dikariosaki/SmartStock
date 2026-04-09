package smartStock.mobile.application.dtos

import com.fasterxml.jackson.annotation.JsonProperty

data class InventoryResponse(
    @JsonProperty("inventarioId") val id: Int?,
    @JsonProperty("productoId") val productId: Int,
    @JsonProperty("productoNombre") val productName: String?,
    @JsonProperty("ubicacion") val location: String?,
    @JsonProperty("cantidad") val quantity: Int,
    @JsonProperty("puntoReorden") val reorderPoint: Int,
    @JsonProperty("estado") val status: Boolean,
)

data class InventoryUpdateRequest(
    @JsonProperty("ubicacion") val location: String?,
    @JsonProperty("puntoReorden") val reorderPoint: Int,
)

data class MovementStockRequest(
    @JsonProperty("productoId") val productId: Int,
    @JsonProperty("cantidad") val quantity: Int,
    @JsonProperty("usuarioId") val userId: Int,
    // ENTRADA, SALIDA, AVERIA
    @JsonProperty("tipo") val type: String,
    @JsonProperty("lote") val batch: String? = null,
)
