package com.example.smartstock.data.repository

import com.example.smartstock.data.api.ProductApiService
import com.example.smartstock.data.api.SubcategoryApiService
import com.example.smartstock.data.model.toDomain
import com.example.smartstock.data.model.toDto
import com.example.smartstock.domain.model.CreateProductRequest
import com.example.smartstock.domain.model.Product
import com.example.smartstock.domain.model.SubcategoryOption
import com.example.smartstock.domain.model.UpdateProductRequest
import com.example.smartstock.domain.repository.ProductRepository
import javax.inject.Inject

class ProductRepositoryImpl
    @Inject
    constructor(
        private val productApiService: ProductApiService,
        private val subcategoryApiService: SubcategoryApiService,
    ) : ProductRepository {
        override suspend fun getProducts(): List<Product> = productApiService.getProducts().map { it.toDomain() }

        override suspend fun getSubcategories(): List<SubcategoryOption> =
            subcategoryApiService.getSubcategories().map { it.toDomain() }

        override suspend fun getProductById(id: Int): Product = productApiService.getProductById(id).toDomain()

        override suspend fun createProduct(request: CreateProductRequest): Product =
            productApiService.createProduct(request.toDto()).toDomain()

        override suspend fun updateProduct(
            id: Int,
            request: UpdateProductRequest
        ): Product = productApiService.updateProduct(id, request.toDto()).toDomain()

        override suspend fun activateProduct(id: Int) {
            productApiService.activateProduct(id)
        }

        override suspend fun deactivateProduct(id: Int) {
            productApiService.deactivateProduct(id)
        }
    }
