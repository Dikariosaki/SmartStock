package com.example.smartstock.ui.screens.movements

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import com.example.smartstock.domain.model.Movement
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

@Composable
fun MovementsScreen(
    currentRoute: String,
    roleName: String?,
    onNavigate: (String) -> Unit,
    viewModel: MovementsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var searchText by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("all") }

    val typeOptions =
        listOf(FilterOption("all", "Todos")) +
            uiState.movements
                .map { it.type.trim().uppercase() }
                .filter { it.isNotBlank() }
                .distinct()
                .map { FilterOption(it, it.lowercase().replaceFirstChar(Char::uppercase)) }

    val shownMovements =
        uiState.movements.filter { movement ->
            val query = searchText.trim().lowercase()
            val matchesSearch =
                query.isBlank() ||
                    (movement.userName?.lowercase()?.contains(query) == true) ||
                    (movement.productName?.lowercase()?.contains(query) == true) ||
                    (movement.inventoryLocation?.lowercase()?.contains(query) == true) ||
                    movement.type.lowercase().contains(query) ||
                    (movement.batch?.lowercase()?.contains(query) == true)
            val matchesType = selectedType == "all" || movement.type.uppercase() == selectedType
            matchesSearch && matchesType
        }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { snackbarHostState.showSnackbar(it) }
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
                        title = "Movimientos",
                        subtitle = "Consulta trazabilidad de entradas, salidas y ajustes de inventario.",
                    )
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    ModuleToolbar(
                        searchText = searchText,
                        onSearchTextChange = { searchText = it },
                        searchPlaceholder = "Buscar por usuario, producto, tipo o lote...",
                        actionLabel = "Recargar",
                        onActionClick = { viewModel.loadMovements() },
                        extraControls = {
                            FilterChipsRow(
                                options = typeOptions,
                                selectedKey = selectedType,
                                onSelected = { selectedType = it },
                            )
                        },
                    )
                }

                items(shownMovements, key = { it.id }) { movement ->
                    MovementCard(movement = movement)
                }

                if (shownMovements.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        EmptyStateCard(
                            title = "Sin movimientos",
                            message = "No se encontraron movimientos con los filtros aplicados.",
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
private fun MovementCard(movement: Movement) {
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
                        text = movement.productName ?: "Producto sin nombre",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "Ubicación: ${movement.inventoryLocation ?: "No definida"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500,
                    )
                }
                StatusPill(active = movement.status)
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
                        Text("Tipo", style = MaterialTheme.typography.bodySmall, color = Slate500)
                        Text(
                            text = movement.type,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Cantidad", style = MaterialTheme.typography.bodySmall, color = Slate500)
                        Text(
                            text = movement.quantity.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val compact = maxWidth < 340.dp
                if (compact) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Usuario: ${movement.userName ?: "No definido"}", style = MaterialTheme.typography.bodySmall, color = Slate500)
                        Text("Fecha: ${movement.date ?: "Sin fecha"}", style = MaterialTheme.typography.bodySmall, color = Slate500)
                        Text("Lote: ${movement.batch ?: "N/A"}", style = MaterialTheme.typography.bodySmall, color = Slate500)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Usuario: ${movement.userName ?: "No definido"}", style = MaterialTheme.typography.bodySmall, color = Slate500)
                            Text("Fecha: ${movement.date ?: "Sin fecha"}", style = MaterialTheme.typography.bodySmall, color = Slate500)
                        }
                        Text(
                            text = "Lote: ${movement.batch ?: "N/A"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500,
                        )
                    }
                }
            }
        }
    }
}
