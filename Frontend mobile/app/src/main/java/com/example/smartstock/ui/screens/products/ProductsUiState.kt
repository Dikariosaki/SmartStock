package com.example.smartstock.ui.screens.products

import com.example.smartstock.domain.model.Product
import com.example.smartstock.domain.model.SubcategoryOption

data class ProductsUiState(
    val isLoading: Boolean = false,
    val isLoadingSubcategories: Boolean = false,
    val products: List<Product> = emptyList(),
    val subcategories: List<SubcategoryOption> = emptyList(),
    val errorMessage: String? = null
)
