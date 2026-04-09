package com.example.smartstock.ui.screens.inventory

import android.annotation.SuppressLint
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
import com.example.smartstock.domain.model.Inventory
import com.example.smartstock.domain.model.MovementStockRequest
import com.example.smartstock.domain.model.SessionUser
import com.example.smartstock.domain.model.UpdateInventoryRequest
import com.example.smartstock.domain.model.User
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

private val inventoryFilters =
    listOf(
        FilterOption("all", "Todos"),
        FilterOption("active", "Activos"),
        FilterOption("inactive", "Inactivos"),
        FilterOption("low", "Stock bajo"),
    )

@Composable
fun InventoryScreen(
    currentRoute: String,
    roleName: String?,
    currentSession: SessionUser?,
    onNavigate: (String) -> Unit,
    viewModel: InventoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showEditDialog by remember { mutableStateOf(false) }
    var editingInventory by remember { mutableStateOf<Inventory?>(null) }
    var showStockDialog by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("all") }

    val shownInventory =
        uiState.inventories.filter { inventory ->
            val query = searchText.trim().lowercase()
            val matchesSearch =
                query.isBlank() ||
                    (inventory.productName?.lowercase()?.contains(query) == true) ||
                    (inventory.location?.lowercase()?.contains(query) == true)
            val matchesFilter =
                when (selectedFilter) {
                    "active" -> inventory.status
                    "inactive" -> !inventory.status
                    "low" -> inventory.quantity <= inventory.reorderPoint
                    else -> true
                }
            matchesSearch && matchesFilter
        }

    val movementUsers =
        buildList {
            if (uiState.users.isNotEmpty()) {
                addAll(uiState.users)
            }
            if (currentSession != null && none { it.id == currentSession.userId }) {
                add(
                    User(
                        id = currentSession.userId,
                        roleId = currentSession.roleId,
                        roleName = currentSession.roleName,
                        name = currentSession.name,
                        cedula = 0,
                        email = currentSession.email,
                        status = true,
                        phone = null,
                    ),
                )
            }
        }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    if (showEditDialog) {
        InventoryFormDialog(
            inventory = editingInventory,
            onDismiss = {
                showEditDialog = false
                editingInventory = null
            },
            onSubmit = { request ->
                val current = editingInventory ?: return@InventoryFormDialog
                viewModel.updateInventory(current.id, request)
                showEditDialog = false
                editingInventory = null
            },
        )
    }

    if (showStockDialog) {
        StockMovementDialog(
            inventories = uiState.inventories,
            users = movementUsers,
            onDismiss = { showStockDialog = false },
            onSubmit = { request, operation ->
                when (operation) {
                    StockOperation.ENTRY -> viewModel.registerEntry(request)
                    StockOperation.EXIT -> viewModel.registerExit(request)
                    StockOperation.DAMAGE -> viewModel.registerDamage(request)
                }
                showStockDialog = false
            },
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            BottomNavBar(
                items = navItemsForRole(roleName),
                currentRoute = currentRoute,
                onItemClick = { onNavigate(it.route) },
            )
        },
    ) { innerPadding ->
        if (uiState.isLoading) {
            FullScreenLoading(innerPadding = innerPadding)
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 320.dp),
                modifier =
                    Modifier
                        .fillMaxSize()
                        .smartStockPageBackground()
                        .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    PageHeader(
                        title = "Inventario",
                        subtitle = "Controla existencias, ubicación y punto de reorden.",
                    )
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    ModuleToolbar(
                        searchText = searchText,
                        onSearchTextChange = { searchText = it },
                        searchPlaceholder = "Buscar por producto o ubicación...",
                        actionLabel = "Movimiento de stock",
                        onActionClick = { showStockDialog = true },
                        extraControls = {
                            FilterChipsRow(
                                options = inventoryFilters,
                                selectedKey = selectedFilter,
                                onSelected = { selectedFilter = it },
                            )
                        },
                    )
                }

                items(shownInventory, key = { it.id }) { inventory ->
                    InventoryCard(
                        inventory = inventory,
                        onEdit = {
                            editingInventory = inventory
                            showEditDialog = true
                        },
                        onToggleStatus = { viewModel.toggleInventoryStatus(inventory.id, inventory.status) },
                    )
                }

                if (shownInventory.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        EmptyStateCard(
                            title = "Inventario vacío",
                            message = "No se encontraron registros para los filtros seleccionados.",
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

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun InventoryCard(
    inventory: Inventory,
    onEdit: () -> Unit,
    onToggleStatus: () -> Unit,
) {
    val lowStock = inventory.quantity <= inventory.reorderPoint

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Slate200),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = inventory.productName ?: "Producto sin nombre",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "Ubicación: ${inventory.location ?: "Sin definir"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500,
                    )
                }
                StatusPill(active = inventory.status)
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                border = BorderStroke(1.dp, Slate200),
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text("Stock", style = MaterialTheme.typography.bodySmall, color = Slate500)
                        Text(
                            text = inventory.quantity.toString(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (lowStock) Color(0xFFB91C1C) else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Mínimo", style = MaterialTheme.typography.bodySmall, color = Slate500)
                        Text(
                            text = inventory.reorderPoint.toString(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val compact = maxWidth < 340.dp
                if (compact) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = onEdit, modifier = Modifier.fillMaxWidth()) { Text("Editar") }
                        Button(onClick = onToggleStatus, modifier = Modifier.fillMaxWidth()) {
                            Text(if (inventory.status) "Desactivar" else "Activar")
                        }
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = onEdit) { Text("Editar") }
                        Button(onClick = onToggleStatus) {
                            Text(if (inventory.status) "Desactivar" else "Activar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InventoryFormDialog(
    inventory: Inventory?,
    onDismiss: () -> Unit,
    onSubmit: (UpdateInventoryRequest) -> Unit,
) {
    var location by remember(inventory) { mutableStateOf(inventory?.location.orEmpty()) }
    var reorderPoint by remember(inventory) { mutableStateOf(inventory?.reorderPoint?.toString().orEmpty()) }

    val reorder = reorderPoint.toIntOrNull()
    val canSubmit = reorder != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar inventario") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Ubicación") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = reorderPoint,
                    onValueChange = { reorderPoint = it },
                    label = { Text("Punto de reorden") },
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val safeReorder = reorder ?: return@TextButton
                    onSubmit(
                        UpdateInventoryRequest(
                            location = location.trim().ifBlank { null },
                            reorderPoint = safeReorder,
                        ),
                    )
                },
                enabled = canSubmit,
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StockMovementDialog(
    inventories: List<Inventory>,
    users: List<User>,
    onDismiss: () -> Unit,
    onSubmit: (MovementStockRequest, StockOperation) -> Unit,
) {
    val uniqueProducts = inventories.distinctBy { it.productId }

    var selectedProduct by remember { mutableStateOf<Inventory?>(null) }
    var selectedUser by remember { mutableStateOf<User?>(null) }
    var productExpanded by remember { mutableStateOf(false) }
    var userExpanded by remember { mutableStateOf(false) }
    var quantity by remember { mutableStateOf("") }
    var batch by remember { mutableStateOf("") }
    var operation by remember { mutableStateOf(StockOperation.ENTRY) }

    val amount = quantity.toIntOrNull()
    val canSubmit = selectedProduct != null && amount != null && amount > 0 && selectedUser != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Movimiento de stock") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExposedDropdownMenuBox(
                    expanded = productExpanded,
                    onExpandedChange = { productExpanded = !productExpanded },
                ) {
                    OutlinedTextField(
                        value = selectedProduct?.productName ?: "Selecciona producto",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Producto") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = productExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                    )
                    DropdownMenu(
                        expanded = productExpanded,
                        onDismissRequest = { productExpanded = false },
                    ) {
                        uniqueProducts.forEach { productInventory ->
                            DropdownMenuItem(
                                text = { Text(productInventory.productName ?: "Producto sin nombre") },
                                onClick = {
                                    selectedProduct = productInventory
                                    productExpanded = false
                                },
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = userExpanded,
                    onExpandedChange = { userExpanded = !userExpanded },
                ) {
                    OutlinedTextField(
                        value = selectedUser?.name ?: "Selecciona usuario",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Usuario") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = userExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                    )
                    DropdownMenu(
                        expanded = userExpanded,
                        onDismissRequest = { userExpanded = false },
                    ) {
                        users.forEach { user ->
                            DropdownMenuItem(
                                text = { Text(user.name) },
                                onClick = {
                                    selectedUser = user
                                    userExpanded = false
                                },
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Cantidad") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = batch,
                    onValueChange = { batch = it },
                    label = { Text("Lote (opcional)") },
                    singleLine = true,
                )

                FilterChipsRow(
                    options = StockOperation.entries.map { FilterOption(it.name, it.label) },
                    selectedKey = operation.name,
                    onSelected = { selected -> operation = StockOperation.valueOf(selected) },
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val safeProduct = selectedProduct ?: return@TextButton
                    val safeAmount = amount ?: return@TextButton
                    val safeUser = selectedUser ?: return@TextButton
                    onSubmit(
                        MovementStockRequest(
                            productId = safeProduct.productId,
                            quantity = safeAmount,
                            userId = safeUser.id,
                            type = operation.apiType,
                            batch = batch.trim().ifBlank { null },
                        ),
                        operation,
                    )
                },
                enabled = canSubmit,
            ) {
                Text("Registrar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
    )
}

private enum class StockOperation(
    val apiType: String,
    val label: String,
) {
    ENTRY("ENTRADA", "Entrada"),
    EXIT("SALIDA", "Salida"),
    DAMAGE("AVERIA", "Avería"),
}
