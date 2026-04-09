package smartStock.mobile.domain.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "Reporte")
data class Report(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reporte_id")
    val id: Int? = null,
    @Column(name = "titulo", nullable = false)
    val title: String,
    @Column(name = "descripcion")
    val description: String? = null,
    @Column(name = "evidencia", columnDefinition = "json")
    val evidenceJson: String? = null,
    @Column(name = "fecha_creado", nullable = false, insertable = false, updatable = false)
    val createdAt: LocalDateTime? = null,
    @Column(name = "tipo_reporte", nullable = false)
    val type: String,
    @Column(name = "estado")
    val status: Boolean = true,
) {
    constructor() : this(null, "", null, null, null, "", true)
}
