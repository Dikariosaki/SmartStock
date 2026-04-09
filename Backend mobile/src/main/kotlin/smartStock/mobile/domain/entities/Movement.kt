package smartStock.mobile.domain.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "Movimiento")
data class Movement(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "movimiento_id")
    val id: Int? = null,
    @Column(name = "inventario_id", nullable = false)
    val inventoryId: Int,
    @Column(name = "orden_id")
    val orderId: Int? = null,
    @Column(name = "usuario_id", nullable = false)
    val userId: Int,
    @Column(name = "proveedor_id")
    val providerId: Int? = null,
    @Column(name = "cliente_id")
    val clientId: Int? = null,
    @Column(name = "tipo", nullable = false)
    val type: String,
    @Column(name = "cantidad", nullable = false)
    val quantity: Int,
    @Column(name = "fecha_movimiento", nullable = false, insertable = false, updatable = false)
    val date: LocalDateTime? = null,
    @Column(name = "lote")
    val batch: String? = null,
    @Column(name = "estado")
    val status: Boolean = true,
) {
    constructor() : this(null, 0, null, 0, null, null, "", 0, null, null, true)
}
