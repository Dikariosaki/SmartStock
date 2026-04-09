package com.example.smartstock.domain.usecase

import com.example.smartstock.domain.model.CreateProductRequest
import com.example.smartstock.domain.model.Product
import com.example.smartstock.domain.model.SubcategoryOption
import com.example.smartstock.domain.model.UpdateProductRequest
import com.example.smartstock.domain.repository.ProductRepository
import javax.inject.Inject

class GetProductsUseCase @Inject constructor(private val productRepository: ProductRepository) {
    suspend operator fun invoke(): List<Product> = productRepository.getProducts()
}

class GetSubcategoriesUseCase @Inject constructor(private val productRepository: ProductRepository) {
    suspend operator fun invoke(): List<SubcategoryOption> = productRepository.getSubcategories()
}

class GetProductByIdUseCase @Inject constructor(private val productRepository: ProductRepository) {
    suspend operator fun invoke(id: Int): Product = productRepository.getProductById(id)
}

class CreateProductUseCase @Inject constructor(private val productRepository: ProductRepository) {
    suspend operator fun invoke(request: CreateProductRequest): Product = productRepository.createProduct(request)
}

class UpdateProductUseCase @Inject constructor(private val productRepository: ProductRepository) {
    suspend operator fun invoke(id: Int, request: UpdateProductRequest): Product = productRepository.updateProduct(id, request)
}

class ActivateProductUseCase @Inject constructor(private val productRepository: ProductRepository) {
    suspend operator fun invoke(id: Int) = productRepository.activateProduct(id)
}

class DeactivateProductUseCase @Inject constructor(private val productRepository: ProductRepository) {
    suspend operator fun invoke(id: Int) = productRepository.deactivateProduct(id)
}
