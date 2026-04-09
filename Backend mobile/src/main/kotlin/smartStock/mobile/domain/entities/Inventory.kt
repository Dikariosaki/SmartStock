package smartStock.mobile.domain.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "Inventario")
data class Inventory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventario_id")
    val id: Int? = null,
    @Column(name = "producto_id", nullable = false)
    val productId: Int,
    @Column(name = "ubicacion")
    val location: String? = null,
    @Column(name = "cantidad", nullable = false)
    val quantity: Int,
    @Column(name = "punto_reorden")
    val reorderPoint: Int = 0,
    @Column(name = "estado")
    val status: Boolean = true,
) {
    constructor() : this(null, 0, null, 0, 0, true)
}
