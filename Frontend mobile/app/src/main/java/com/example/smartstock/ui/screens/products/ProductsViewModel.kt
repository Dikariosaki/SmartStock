package com.example.smartstock.ui.screens.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartstock.domain.model.CreateProductRequest
import com.example.smartstock.domain.model.UpdateProductRequest
import com.example.smartstock.domain.usecase.ActivateProductUseCase
import com.example.smartstock.domain.usecase.CreateProductUseCase
import com.example.smartstock.domain.usecase.DeactivateProductUseCase
import com.example.smartstock.domain.usecase.GetProductByIdUseCase
import com.example.smartstock.domain.usecase.GetProductsUseCase
import com.example.smartstock.domain.usecase.GetSubcategoriesUseCase
import com.example.smartstock.domain.usecase.UpdateProductUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ProductsViewModel
    @Inject
    constructor(
        private val getProductsUseCase: GetProductsUseCase,
        private val getSubcategoriesUseCase: GetSubcategoriesUseCase,
        private val getProductByIdUseCase: GetProductByIdUseCase,
        private val createProductUseCase: CreateProductUseCase,
        private val updateProductUseCase: UpdateProductUseCase,
        private val activateProductUseCase: ActivateProductUseCase,
        private val deactivateProductUseCase: DeactivateProductUseCase
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(ProductsUiState())
        val uiState: StateFlow<ProductsUiState> = _uiState.asStateFlow()

        init {
            loadProducts()
            loadSubcategories()
        }

        fun loadProducts() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                runCatching { getProductsUseCase() }
                    .onSuccess { products ->
                        _uiState.update { it.copy(isLoading = false, products = products) }
                    }
                    .onFailure { throwable ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = throwable.message ?: "No se pudieron cargar los productos"
                            )
                        }
                    }
            }
        }

        fun getProductById(id: Int) {
            viewModelScope.launch {
                runCatching { getProductByIdUseCase(id) }
                    .onFailure { throwable ->
                        _uiState.update { it.copy(errorMessage = throwable.message ?: "No se pudo cargar el producto") }
                    }
            }
        }

        fun createProduct(request: CreateProductRequest) {
            viewModelScope.launch {
                runCatching { createProductUseCase(request) }
                    .onSuccess { loadProducts() }
                    .onFailure { throwable ->
                        _uiState.update { it.copy(errorMessage = throwable.message ?: "No se pudo crear el producto") }
                    }
            }
        }

        fun updateProduct(
            id: Int,
            request: UpdateProductRequest
        ) {
            viewModelScope.launch {
                runCatching { updateProductUseCase(id, request) }
                    .onSuccess { loadProducts() }
                    .onFailure { throwable ->
                        _uiState.update { it.copy(errorMessage = throwable.message ?: "No se pudo actualizar el producto") }
                    }
            }
        }

        fun toggleProductStatus(
            id: Int,
            isActive: Boolean
        ) {
            viewModelScope.launch {
                runCatching {
                    if (isActive) deactivateProductUseCase(id) else activateProductUseCase(id)
                }.onSuccess {
                    loadProducts()
                }.onFailure { throwable ->
                    _uiState.update { it.copy(errorMessage = throwable.message ?: "No se pudo actualizar el estado") }
                }
            }
        }

        fun loadSubcategories() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoadingSubcategories = true) }
                runCatching { getSubcategoriesUseCase() }
                    .onSuccess { subcategories ->
                        _uiState.update {
                            it.copy(
                                isLoadingSubcategories = false,
                                subcategories = subcategories.filter { subcategory -> subcategory.status },
                            )
                        }
                    }
                    .onFailure { throwable ->
                        _uiState.update {
                            it.copy(
                                isLoadingSubcategories = false,
                                errorMessage = throwable.message ?: "No se pudieron cargar las subcategorias",
                            )
                        }
                    }
            }
        }
    }
