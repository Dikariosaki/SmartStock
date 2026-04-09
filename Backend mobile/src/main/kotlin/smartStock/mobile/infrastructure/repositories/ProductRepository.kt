package smartStock.mobile.infrastructure.repositories

import org.springframework.stereotype.Repository
import smartStock.mobile.application.interfaces.IProductRepository
import smartStock.mobile.domain.entities.Product
import java.util.Optional

@Repository
class ProductRepository(private val jpaRepository: ProductJpaRepository) : IProductRepository {
    override fun findAll(): List<Product> = jpaRepository.findAll()

    override fun findById(id: Int): Optional<Product> = jpaRepository.findById(id)

    override fun save(product: Product): Product = jpaRepository.save(product)

    override fun deleteById(id: Int) = jpaRepository.deleteById(id)
}
