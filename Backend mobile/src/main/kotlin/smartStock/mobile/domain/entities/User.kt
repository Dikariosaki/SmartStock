package smartStock.mobile.domain.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "Usuario")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usuario_id")
    val id: Int? = null,
    @Column(name = "rol_id", nullable = false)
    val roleId: Int,
    @Column(name = "nombre", nullable = false)
    val name: String,
    @Column(name = "Cedula", nullable = false)
    val cedula: Int,
    @Column(name = "email", nullable = false, unique = true)
    val email: String,
    @Column(name = "password_hash", nullable = false)
    val passwordHash: String,
    @Column(name = "estado")
    val status: Boolean = true,
    @Column(name = "telefono")
    val phone: String? = null,
) {
    // No-arg constructor for JPA
    constructor() : this(null, 0, "", 0, "", "", true, null)
}
