package com.example.smartstock.domain.model

data class SubcategoryOption(
    val id: Int,
    val categoryId: Int,
    val name: String,
    val status: Boolean,
)

data class Product(
    val id: Int,
    val subcategoryId: Int,
    val subcategoryName: String?,
    val code: String,
    val name: String,
    val description: String?,
    val unitPrice: Double,
    val status: Boolean
)

data class CreateProductRequest(
    val subcategoryId: Int,
    val code: String,
    val name: String,
    val description: String?,
    val unitPrice: Double,
    val status: Boolean = true
)

data class UpdateProductRequest(
    val subcategoryId: Int,
    val code: String,
    val name: String,
    val description: String?,
    val unitPrice: Double
)
