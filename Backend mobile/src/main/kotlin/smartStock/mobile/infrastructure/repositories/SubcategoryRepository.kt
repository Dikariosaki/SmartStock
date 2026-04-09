package smartStock.mobile.infrastructure.repositories

import org.springframework.stereotype.Repository
import smartStock.mobile.application.interfaces.ISubcategoryRepository
import smartStock.mobile.domain.entities.Subcategory
import java.util.Optional

@Repository
class SubcategoryRepository(private val jpaRepository: SubcategoryJpaRepository) : ISubcategoryRepository {
    override fun findAll(): List<Subcategory> = jpaRepository.findAll()

    override fun findById(id: Int): Optional<Subcategory> = jpaRepository.findById(id)
}
