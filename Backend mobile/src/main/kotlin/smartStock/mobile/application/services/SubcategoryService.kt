package smartStock.mobile.application.services

import org.springframework.stereotype.Service
import smartStock.mobile.application.dtos.SubcategoryResponse
import smartStock.mobile.application.interfaces.ISubcategoryRepository

@Service
class SubcategoryService(
    private val subcategoryRepository: ISubcategoryRepository,
) {
    fun getAll(): List<SubcategoryResponse> {
        return subcategoryRepository.findAll()
            .mapNotNull { subcategory ->
                subcategory.id?.let { subcategoryId ->
                    SubcategoryResponse(
                        id = subcategoryId,
                        categoryId = subcategory.categoryId,
                        name = subcategory.name,
                        status = subcategory.status,
                    )
                }
            }
            .sortedBy { it.name.lowercase() }
    }
}
