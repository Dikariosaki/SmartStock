package com.example.smartstock.domain.repository

import com.example.smartstock.domain.model.CreateProductRequest
import com.example.smartstock.domain.model.Product
import com.example.smartstock.domain.model.SubcategoryOption
import com.example.smartstock.domain.model.UpdateProductRequest

interface ProductRepository {
    suspend fun getProducts(): List<Product>
    suspend fun getSubcategories(): List<SubcategoryOption>
    suspend fun getProductById(id: Int): Product
    suspend fun createProduct(request: CreateProductRequest): Product
    suspend fun updateProduct(id: Int, request: UpdateProductRequest): Product
    suspend fun activateProduct(id: Int)
    suspend fun deactivateProduct(id: Int)
}
