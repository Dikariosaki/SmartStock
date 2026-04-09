package smartStock.mobile.application.interfaces

import smartStock.mobile.domain.entities.Product
import java.util.Optional

interface IProductRepository {
    fun findAll(): List<Product>

    fun findById(id: Int): Optional<Product>

    fun save(product: Product): Product

    fun deleteById(id: Int)
}
