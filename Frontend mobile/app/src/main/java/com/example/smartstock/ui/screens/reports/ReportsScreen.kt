package com.example.smartstock.ui.screens.reports

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
import com.example.smartstock.domain.model.CreateReportRequest
import com.example.smartstock.domain.model.Report
import com.example.smartstock.domain.model.UpdateReportRequest
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

private val reportStatusFilters =
    listOf(
        FilterOption("all", "Todos"),
        FilterOption("active", "Activos"),
        FilterOption("inactive", "Inactivos")
    )

@Composable
fun ReportsScreen(
    currentRoute: String,
    roleName: String?,
    onNavigate: (String) -> Unit,
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val normalizedRole = roleName?.trim()?.lowercase()
    val canManageReports = normalizedRole in setOf("administrador", "supervisor")
    val canToggleReportStatus = normalizedRole == "administrador"

    var showForm by remember { mutableStateOf(false) }
    var editingReport by remember { mutableStateOf<Report?>(null) }
    var searchText by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("all") }
    var selectedType by remember { mutableStateOf("all") }

    val typeOptions =
        listOf(FilterOption("all", "Todos")) +
            uiState.reports
                .map { it.type.trim() }
                .filter { it.isNotBlank() }
                .distinct()
                .map { FilterOption(it.lowercase(), it) }

    val shownReports =
        uiState.reports.filter { report ->
            val query = searchText.trim().lowercase()
            val matchesSearch =
                query.isBlank() ||
                    report.title.lowercase().contains(query) ||
                    (report.description?.lowercase()?.contains(query) == true)
            val matchesStatus =
                when (selectedStatus) {
                    "active" -> report.status
                    "inactive" -> !report.status
                    else -> true
                }
            val matchesType =
                selectedType == "all" ||
                    report.type.lowercase() == selectedType

            matchesSearch && matchesStatus && matchesType
        }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    if (showForm) {
        ReportFormDialog(
            report = editingReport,
            onDismiss = {
                showForm = false
                editingReport = null
            },
            onCreate = { request ->
                viewModel.createReport(request)
                showForm = false
                editingReport = null
            },
            onUpdate = { request ->
                val current = editingReport ?: return@ReportFormDialog
                viewModel.updateReport(current.id, request)
                showForm = false
                editingReport = null
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
                        title = "Reportes",
                        subtitle = "Consulta y administra reportes de gestión e indicadores."
                    )
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    ModuleToolbar(
                        searchText = searchText,
                        onSearchTextChange = { searchText = it },
                        searchPlaceholder = "Buscar reportes por título o descripción...",
                        actionLabel = if (canManageReports) "Nuevo reporte" else "Recargar",
                        onActionClick = {
                            if (canManageReports) {
                                editingReport = null
                                showForm = true
                            } else {
                                viewModel.loadReports()
                            }
                        },
                        extraControls = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChipsRow(
                                    options = reportStatusFilters,
                                    selectedKey = selectedStatus,
                                    onSelected = { selectedStatus = it }
                                )
                                FilterChipsRow(
                                    options = typeOptions,
                                    selectedKey = selectedType,
                                    onSelected = { selectedType = it }
                                )
                            }
                        }
                    )
                }

                items(shownReports, key = { it.id }) { report ->
                    ReportCard(
                        report = report,
                        canEdit = canManageReports,
                        canToggleStatus = canToggleReportStatus,
                        onEdit = {
                            editingReport = report
                            showForm = true
                        },
                        onToggleStatus = { viewModel.toggleReportStatus(report.id, report.status) }
                    )
                }

                if (shownReports.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        EmptyStateCard(
                            title = "No hay reportes",
                            message = "No se encontraron reportes para los filtros seleccionados."
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
private fun ReportCard(
    report: Report,
    canEdit: Boolean,
    canToggleStatus: Boolean,
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
                        text = report.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Tipo: ${report.type}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500
                    )
                }
                StatusPill(active = report.status)
            }

            Text(
                text = report.description ?: "Sin descripción",
                style = MaterialTheme.typography.bodySmall,
                color = Slate500
            )
            Text(
                text = "Fecha: ${report.createdAt}",
                style = MaterialTheme.typography.bodySmall,
                color = Slate500
            )

            if (canEdit || canToggleStatus) {
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val compact = maxWidth < 340.dp
                    if (compact) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (canEdit) {
                                Button(onClick = onEdit, modifier = Modifier.fillMaxWidth()) { Text("Editar") }
                            }
                            if (canToggleStatus) {
                                Button(onClick = onToggleStatus, modifier = Modifier.fillMaxWidth()) {
                                    Text(if (report.status) "Desactivar" else "Activar")
                                }
                            }
                        }
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (canEdit) {
                                Button(onClick = onEdit) { Text("Editar") }
                            }
                            if (canToggleStatus) {
                                Button(onClick = onToggleStatus) {
                                    Text(if (report.status) "Desactivar" else "Activar")
                                }
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

@Composable
private fun ReportFormDialog(
    report: Report?,
    onDismiss: () -> Unit,
    onCreate: (CreateReportRequest) -> Unit,
    onUpdate: (UpdateReportRequest) -> Unit
) {
    var title by remember(report) { mutableStateOf(report?.title.orEmpty()) }
    var description by remember(report) { mutableStateOf(report?.description.orEmpty()) }
    var type by remember(report) { mutableStateOf(report?.type.orEmpty()) }

    val canSubmit = title.isNotBlank() && type.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (report == null) "Crear reporte" else "Editar reporte") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = type,
                    onValueChange = { type = it },
                    label = { Text("Tipo") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (report == null) {
                        onCreate(
                            CreateReportRequest(
                                title = title.trim(),
                                description = description.trim().ifBlank { null },
                                type = type.trim()
                            )
                        )
                    } else {
                        onUpdate(
                            UpdateReportRequest(
                                title = title.trim(),
                                description = description.trim().ifBlank { null },
                                type = type.trim()
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

