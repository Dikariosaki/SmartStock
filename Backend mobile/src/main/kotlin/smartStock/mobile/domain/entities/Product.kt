package smartStock.mobile.domain.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name = "Producto")
data class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "producto_id")
    val id: Int? = null,
    @Column(name = "subcategoria_id", nullable = false)
    val subcategoryId: Int,
    @Column(name = "codigo", nullable = false, unique = true)
    val code: String,
    @Column(name = "nombre", nullable = false)
    val name: String,
    @Column(name = "descripcion")
    val description: String? = null,
    @Column(name = "precio_unitario", nullable = false)
    val unitPrice: BigDecimal,
    @Column(name = "estado")
    val status: Boolean = true,
) {
    constructor() : this(null, 0, "", "", null, BigDecimal.ZERO, true)
}
