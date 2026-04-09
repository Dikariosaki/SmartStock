package smartStock.mobile.application.services

import org.springframework.stereotype.Service
import smartStock.mobile.application.dtos.ProductCreateRequest
import smartStock.mobile.application.dtos.ProductResponse
import smartStock.mobile.application.dtos.ProductUpdateRequest
import smartStock.mobile.application.interfaces.IProductRepository
import smartStock.mobile.application.interfaces.ISubcategoryRepository
import smartStock.mobile.domain.entities.Product

@Service
class ProductService(
    private val productRepository: IProductRepository,
    private val subcategoryRepository: ISubcategoryRepository,
) {
    fun getAll(): List<ProductResponse> {
        val subcategoriesById =
            subcategoryRepository.findAll()
                .associateBy({ it.id }, { it.name })

        return productRepository.findAll().map { product ->
            product.toResponse(subcategoriesById[product.subcategoryId])
        }
    }

    fun getById(id: Int): ProductResponse? {
        return productRepository.findById(id).map { product ->
            val subcategoryName = subcategoryRepository.findById(product.subcategoryId).map { it.name }.orElse(null)
            product.toResponse(subcategoryName)
        }.orElse(null)
    }

    fun create(request: ProductCreateRequest): ProductResponse {
        val product =
            Product(
                subcategoryId = request.subcategoryId,
                code = request.code,
                name = request.name,
                description = request.description,
                unitPrice = request.unitPrice,
                status = request.status,
            )
        val savedProduct = productRepository.save(product)
        val subcategoryName = subcategoryRepository.findById(savedProduct.subcategoryId).map { it.name }.orElse(null)
        return savedProduct.toResponse(subcategoryName)
    }

    fun update(
        id: Int,
        request: ProductUpdateRequest,
    ): ProductResponse {
        val existingProduct = productRepository.findById(id).orElseThrow { RuntimeException("Producto no encontrado") }

        val updatedProduct =
            existingProduct.copy(
                subcategoryId = request.subcategoryId,
                code = request.code,
                name = request.name,
                description = request.description,
                unitPrice = request.unitPrice,
            )
        val savedProduct = productRepository.save(updatedProduct)
        val subcategoryName = subcategoryRepository.findById(savedProduct.subcategoryId).map { it.name }.orElse(null)
        return savedProduct.toResponse(subcategoryName)
    }

    fun activate(id: Int) {
        val existingProduct = productRepository.findById(id).orElseThrow { RuntimeException("Producto no encontrado") }
        val updatedProduct = existingProduct.copy(status = true)
        productRepository.save(updatedProduct)
    }

    fun deactivate(id: Int) {
        val existingProduct = productRepository.findById(id).orElseThrow { RuntimeException("Producto no encontrado") }
        val updatedProduct = existingProduct.copy(status = false)
        productRepository.save(updatedProduct)
    }

    private fun Product.toResponse(subcategoryName: String?) =
        ProductResponse(
            id = id,
            subcategoryId = subcategoryId,
            subcategoryName = subcategoryName,
            code = code,
            name = name,
            description = description,
            unitPrice = unitPrice,
            status = status,
        )
}
