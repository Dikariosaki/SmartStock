package smartStock.mobile.domain.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "Subcategoria")
data class Subcategory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subcategoria_id")
    val id: Int? = null,
    @Column(name = "categoria_id", nullable = false)
    val categoryId: Int,
    @Column(name = "nombre", nullable = false)
    val name: String,
    @Column(name = "estado")
    val status: Boolean = true,
) {
    constructor() : this(null, 0, "", true)
}
