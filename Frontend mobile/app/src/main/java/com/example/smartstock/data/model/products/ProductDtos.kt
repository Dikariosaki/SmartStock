package com.example.smartstock.data.model

data class SubcategoryDto(
    val subcategoriaId: Int,
    val categoriaId: Int,
    val nombre: String,
    val estado: Boolean,
)

data class ProductDto(
    val productoId: Int?,
    val subcategoriaId: Int,
    val subcategoriaNombre: String?,
    val codigo: String,
    val nombre: String,
    val descripcion: String?,
    val precioUnitario: Double,
    val estado: Boolean
)

data class ProductCreateRequestDto(
    val subcategoriaId: Int,
    val codigo: String,
    val nombre: String,
    val descripcion: String?,
    val precioUnitario: Double,
    val estado: Boolean = true
)

data class ProductUpdateRequestDto(
    val subcategoriaId: Int,
    val codigo: String,
    val nombre: String,
    val descripcion: String?,
    val precioUnitario: Double
)
