package com.example.smartstock.ui.screens.products

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.smartstock.domain.model.CreateProductRequest
import com.example.smartstock.domain.model.Product
import com.example.smartstock.domain.model.SubcategoryOption
import com.example.smartstock.domain.model.UpdateProductRequest
import com.example.smartstock.ui.components.BottomNavBar
import com.example.smartstock.ui.components.EmptyStateCard
import com.example.smartstock.ui.components.FilterChipsRow
import com.example.smartstock.ui.components.FilterOption
import com.example.smartstock.ui.components.FullScreenLoading
import com.example.smartstock.ui.components.ModuleToolbar
import com.example.smartstock.ui.components.PageHeader
import com.example.smartstock.ui.components.Slate200
import com.example.smartstock.ui.components.Slate500
import com.example.smartstock.ui.components.StatusPill
import com.example.smartstock.ui.components.navItemsForRole
import com.example.smartstock.ui.components.smartStockPageBackground

private val productStatusFilters =
    listOf(
        FilterOption("all", "Todos"),
        FilterOption("active", "Activos"),
        FilterOption("inactive", "Inactivos")
    )

@Composable
fun ProductsScreen(
    currentRoute: String,
    roleName: String?,
    onNavigate: (String) -> Unit,
    viewModel: ProductsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val normalizedRole = roleName?.trim()?.lowercase()
    val canManageProducts = normalizedRole in setOf("administrador", "supervisor")

    var showForm by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var searchText by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("all") }

    val shownProducts =
        uiState.products.filter { product ->
            val query = searchText.trim().lowercase()
            val matchesSearch =
                query.isBlank() ||
                    product.name.lowercase().contains(query) ||
                    product.code.lowercase().contains(query)
            val matchesStatus =
                when (selectedStatus) {
                    "active" -> product.status
                    "inactive" -> !product.status
                    else -> true
                }
            matchesSearch && matchesStatus
        }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    if (showForm) {
        ProductFormDialog(
            product = editingProduct,
            subcategories = uiState.subcategories,
            loadingSubcategories = uiState.isLoadingSubcategories,
            onDismiss = {
                showForm = false
                editingProduct = null
            },
            onCreate = { request ->
                viewModel.createProduct(request)
                showForm = false
                editingProduct = null
            },
            onUpdate = { request ->
                val current = editingProduct ?: return@ProductFormDialog
                viewModel.updateProduct(current.id, request)
                showForm = false
                editingProduct = null
            }
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            BottomNavBar(
                items = navItemsForRole(roleName),
                currentRoute = currentRoute,
                onItemClick = { onNavigate(it.route) }
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            FullScreenLoading(innerPadding = innerPadding)
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 320.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .smartStockPageBackground()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    PageHeader(
                        title = "Productos",
                        subtitle = "Gestiona el catálogo, precios unitarios y estado comercial."
                    )
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    ModuleToolbar(
                        searchText = searchText,
                        onSearchTextChange = { searchText = it },
                        searchPlaceholder = "Buscar por nombre o código...",
                        actionLabel = if (canManageProducts) "Nuevo producto" else "Recargar",
                        onActionClick = {
                            if (canManageProducts) {
                                editingProduct = null
                                showForm = true
                            } else {
                                viewModel.loadProducts()
                            }
                        },
                        actionEnabled = canManageProducts || !uiState.isLoading,
                        extraControls = {
                            FilterChipsRow(
                                options = productStatusFilters,
                                selectedKey = selectedStatus,
                                onSelected = { selectedStatus = it }
                            )
                        }
                    )
                }

                items(shownProducts, key = { it.id }) { product ->
                    ProductCard(
                        product = product,
                        canManage = canManageProducts,
                        onEdit = {
                            editingProduct = product
                            showForm = true
                        },
                        onToggleStatus = { viewModel.toggleProductStatus(product.id, product.status) }
                    )
                }

                if (shownProducts.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        EmptyStateCard(
                            title = "No hay productos",
                            message = "No se encontraron productos con los filtros actuales."
                        )
                    }
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    Spacer(modifier = Modifier.height(76.dp))
                }
            }
        }
    }
}

@Composable
private fun ProductCard(
    product: Product,
    canManage: Boolean,
    onEdit: () -> Unit,
    onToggleStatus: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Slate200),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Código: ${product.code}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500
                    )
                }
                StatusPill(active = product.status)
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                border = BorderStroke(1.dp, Slate200)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Precio unitario", style = MaterialTheme.typography.bodySmall, color = Slate500)
                    Text(
                        text = "$${"%.2f".format(product.unitPrice)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = product.description ?: "Sin descripción",
                style = MaterialTheme.typography.bodySmall,
                color = Slate500
            )
            Text(
                text = "Subcategoría: ${product.subcategoryName ?: "No definida"}",
                style = MaterialTheme.typography.bodySmall,
                color = Slate500
            )

            if (canManage) {
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val compact = maxWidth < 340.dp
                    if (compact) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = onEdit, modifier = Modifier.fillMaxWidth()) { Text("Editar") }
                            Button(onClick = onToggleStatus, modifier = Modifier.fillMaxWidth()) {
                                Text(if (product.status) "Desactivar" else "Activar")
                            }
                        }
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = onEdit) { Text("Editar") }
                            Button(onClick = onToggleStatus) {
                                Text(if (product.status) "Desactivar" else "Activar")
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = "Solo lectura para tu rol",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductFormDialog(
    product: Product?,
    subcategories: List<SubcategoryOption>,
    loadingSubcategories: Boolean,
    onDismiss: () -> Unit,
    onCreate: (CreateProductRequest) -> Unit,
    onUpdate: (UpdateProductRequest) -> Unit
) {
    var subcategoryId by remember(product) { mutableStateOf(product?.subcategoryId) }
    var subcategoryExpanded by remember { mutableStateOf(false) }
    var code by remember(product) { mutableStateOf(product?.code.orEmpty()) }
    var name by remember(product) { mutableStateOf(product?.name.orEmpty()) }
    var description by remember(product) { mutableStateOf(product?.description.orEmpty()) }
    var unitPrice by remember(product) { mutableStateOf(product?.unitPrice?.toString().orEmpty()) }
    var status by remember(product) { mutableStateOf(product?.status ?: true) }

    val selectedSubcategory = subcategories.firstOrNull { it.id == subcategoryId }
    val price = unitPrice.toDoubleOrNull()
    val canSubmit = subcategoryId != null && price != null && code.isNotBlank() && name.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (product == null) "Crear producto" else "Editar producto") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExposedDropdownMenuBox(
                    expanded = subcategoryExpanded,
                    onExpandedChange = { subcategoryExpanded = !subcategoryExpanded },
                ) {
                    OutlinedTextField(
                        value =
                            when {
                                loadingSubcategories -> "Cargando subcategorias..."
                                selectedSubcategory != null -> selectedSubcategory.name
                                else -> "Selecciona una subcategoria"
                            },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Subcategoria") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = subcategoryExpanded)
                        },
                        modifier =
                            Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                    )

                    DropdownMenu(
                        expanded = subcategoryExpanded,
                        onDismissRequest = { subcategoryExpanded = false },
                    ) {
                        subcategories.forEach { subcategory ->
                            DropdownMenuItem(
                                text = { Text(subcategory.name) },
                                onClick = {
                                    subcategoryId = subcategory.id
                                    subcategoryExpanded = false
                                },
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Código") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = unitPrice,
                    onValueChange = { unitPrice = it },
                    label = { Text("Precio unitario") },
                    singleLine = true
                )

                if (product == null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = status, onCheckedChange = { status = it })
                        Text("Producto activo")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val safeSubcategory = subcategoryId ?: return@TextButton
                    val safePrice = price ?: return@TextButton
                    if (product == null) {
                        onCreate(
                            CreateProductRequest(
                                subcategoryId = safeSubcategory,
                                code = code.trim(),
                                name = name.trim(),
                                description = description.trim().ifBlank { null },
                                unitPrice = safePrice,
                                status = status
                            )
                        )
                    } else {
                        onUpdate(
                            UpdateProductRequest(
                                subcategoryId = safeSubcategory,
                                code = code.trim(),
                                name = name.trim(),
                                description = description.trim().ifBlank { null },
                                unitPrice = safePrice
                            )
                        )
                    }
                },
                enabled = canSubmit
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

