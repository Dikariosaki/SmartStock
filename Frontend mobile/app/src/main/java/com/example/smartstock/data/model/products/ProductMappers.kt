package com.example.smartstock.data.model

import com.example.smartstock.domain.model.CreateProductRequest
import com.example.smartstock.domain.model.Product
import com.example.smartstock.domain.model.SubcategoryOption
import com.example.smartstock.domain.model.UpdateProductRequest

fun ProductDto.toDomain(): Product =
    Product(
        id = productoId ?: 0,
        subcategoryId = subcategoriaId,
        subcategoryName = subcategoriaNombre,
        code = codigo,
        name = nombre,
        description = descripcion,
        unitPrice = precioUnitario,
        status = estado
    )

fun SubcategoryDto.toDomain(): SubcategoryOption =
    SubcategoryOption(
        id = subcategoriaId,
        categoryId = categoriaId,
        name = nombre,
        status = estado,
    )

fun CreateProductRequest.toDto(): ProductCreateRequestDto =
    ProductCreateRequestDto(
        subcategoriaId = subcategoryId,
        codigo = code,
        nombre = name,
        descripcion = description,
        precioUnitario = unitPrice,
        estado = status
    )

fun UpdateProductRequest.toDto(): ProductUpdateRequestDto =
    ProductUpdateRequestDto(
        subcategoriaId = subcategoryId,
        codigo = code,
        nombre = name,
        descripcion = description,
        precioUnitario = unitPrice
    )
