package smartStock.mobile.application.dtos

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class MovementResponse(
    @JsonProperty("movimientoId") val id: Int?,
    @JsonProperty("inventarioId") val inventoryId: Int,
    @JsonProperty("usuarioId") val userId: Int,
    @JsonProperty("usuarioNombre") val userName: String?,
    @JsonProperty("productoNombre") val productName: String?,
    @JsonProperty("inventarioUbicacion") val inventoryLocation: String?,
    @JsonProperty("tipo") val type: String,
    @JsonProperty("cantidad") val quantity: Int,
    @JsonProperty("fechaMovimiento") val date: LocalDateTime?,
    @JsonProperty("lote") val batch: String?,
    @JsonProperty("estado") val status: Boolean,
)
