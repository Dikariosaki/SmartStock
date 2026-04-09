package smartStock.mobile.application.interfaces

import smartStock.mobile.domain.entities.Subcategory
import java.util.Optional

interface ISubcategoryRepository {
    fun findAll(): List<Subcategory>

    fun findById(id: Int): Optional<Subcategory>
}
