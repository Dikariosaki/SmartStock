package smartStock.mobile.application.dtos

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class TaskResponse(
    @JsonProperty("tareaId") val id: Int?,
    @JsonProperty("titulo") val title: String,
    @JsonProperty("descripcion") val description: String?,
    @JsonProperty("asignadoA") val assignedTo: Int?,
    @JsonProperty("asignadoNombre") val assignedToName: String?,
    @JsonProperty("fechaCreacion") val createdAt: LocalDateTime,
    @JsonProperty("fechaFin") val finishedAt: LocalDateTime?,
    @JsonProperty("estado") val status: Boolean,
)

data class TaskCreateRequest(
    @JsonProperty("titulo") val title: String,
    @JsonProperty("descripcion") val description: String?,
    @JsonProperty("asignadoA") val assignedTo: Int?,
)

data class TaskUpdateRequest(
    @JsonProperty("titulo") val title: String,
    @JsonProperty("descripcion") val description: String?,
    @JsonProperty("asignadoA") val assignedTo: Int?,
    @JsonProperty("estado") val status: Boolean,
)
