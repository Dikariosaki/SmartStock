package smartStock.mobile.domain.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "Tarea")
data class Task(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tarea_id")
    val id: Int? = null,
    @Column(name = "titulo", nullable = false)
    val title: String,
    @Column(name = "descripcion")
    val description: String? = null,
    @Column(name = "asignado_a")
    val assignedTo: Int? = null,
    @Column(name = "fecha_creacion", nullable = false, insertable = false, updatable = false)
    val createdAt: LocalDateTime? = null,
    @Column(name = "fecha_fin")
    val finishedAt: LocalDateTime? = null,
    @Column(name = "estado")
    val status: Boolean = true,
) {
    constructor() : this(null, "", null, null, null, null, true)
}
