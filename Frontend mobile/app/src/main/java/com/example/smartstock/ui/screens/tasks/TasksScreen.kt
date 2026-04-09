package com.example.smartstock.ui.screens.tasks

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
import com.example.smartstock.domain.model.CreateTaskRequest
import com.example.smartstock.domain.model.Task
import com.example.smartstock.domain.model.UpdateTaskRequest
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

private val taskStatusFilters =
    listOf(
        FilterOption("all", "Todas"),
        FilterOption("pending", "Pendientes"),
        FilterOption("done", "Completadas"),
    )

@Composable
fun TasksScreen(
    currentRoute: String,
    roleName: String?,
    onNavigate: (String) -> Unit,
    viewModel: TasksViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val normalizedRole = roleName?.trim()?.lowercase()
    val canManageTasks = normalizedRole in setOf("administrador", "supervisor")
    val canDeleteTasks = normalizedRole == "administrador"

    var showForm by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<Task?>(null) }
    var searchText by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("all") }
    var selectedAssignee by remember { mutableStateOf("all") }

    val assigneeOptions =
        buildList {
            add(FilterOption("all", "Todos"))
            val users = uiState.users.associateBy { it.id }
            val fallback =
                uiState.tasks
                    .mapNotNull { task ->
                        val id = task.assignedTo ?: return@mapNotNull null
                        val name = task.assignedToName ?: users[id]?.name ?: "Usuario sin nombre"
                        id to name
                    }
                    .distinctBy { it.first }
                    .sortedBy { it.second }
            fallback.forEach { (id, name) -> add(FilterOption(id.toString(), name)) }
        }

    val shownTasks =
        uiState.tasks.filter { task ->
            val query = searchText.trim().lowercase()
            val matchesSearch =
                query.isBlank() ||
                    task.title.lowercase().contains(query) ||
                    (task.description?.lowercase()?.contains(query) == true) ||
                    (task.assignedToName?.lowercase()?.contains(query) == true)
            val matchesStatus =
                when (selectedStatus) {
                    "pending" -> task.status
                    "done" -> !task.status
                    else -> true
                }
            val matchesAssignee = selectedAssignee == "all" || task.assignedTo?.toString() == selectedAssignee
            matchesSearch && matchesStatus && matchesAssignee
        }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    if (showForm) {
        TaskFormDialog(
            task = editingTask,
            users = uiState.users,
            onDismiss = {
                showForm = false
                editingTask = null
            },
            onCreate = { request ->
                viewModel.createTask(request)
                showForm = false
                editingTask = null
            },
            onUpdate = { request ->
                val current = editingTask ?: return@TaskFormDialog
                viewModel.updateTask(current.id, request)
                showForm = false
                editingTask = null
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
                        title = "Gestión de tareas",
                        subtitle = "Supervisa asignaciones, seguimiento y estado operativo.",
                    )
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    ModuleToolbar(
                        searchText = searchText,
                        onSearchTextChange = { searchText = it },
                        searchPlaceholder = "Buscar por título, descripción o asignado...",
                        actionLabel = if (canManageTasks) "Nueva tarea" else "Recargar",
                        onActionClick = {
                            if (canManageTasks) {
                                editingTask = null
                                showForm = true
                            } else {
                                viewModel.loadTasks()
                            }
                        },
                        extraControls = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChipsRow(
                                    options = taskStatusFilters,
                                    selectedKey = selectedStatus,
                                    onSelected = { selectedStatus = it },
                                )
                                FilterChipsRow(
                                    options = assigneeOptions,
                                    selectedKey = selectedAssignee,
                                    onSelected = { selectedAssignee = it },
                                )
                            }
                        },
                    )
                }

                items(shownTasks, key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        canManage = canManageTasks,
                        canDelete = canDeleteTasks,
                        onEdit = {
                            editingTask = task
                            showForm = true
                        },
                        onToggleStatus = { viewModel.toggleTaskStatus(task.id, task.status) },
                        onDelete = { viewModel.deleteTask(task.id) },
                    )
                }

                if (shownTasks.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        EmptyStateCard(
                            title = "No hay tareas",
                            message = "No se encontraron tareas para los filtros seleccionados.",
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
private fun TaskCard(
    task: Task,
    canManage: Boolean,
    canDelete: Boolean,
    onEdit: () -> Unit,
    onToggleStatus: () -> Unit,
    onDelete: () -> Unit,
) {
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
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "Asignada a: ${task.assignedToName ?: "Sin asignar"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500,
                    )
                }
                StatusPill(
                    active = task.status,
                    activeLabel = "Pendiente",
                    inactiveLabel = "Completada",
                )
            }

            Text(
                text = task.description ?: "Sin descripción",
                style = MaterialTheme.typography.bodySmall,
                color = Slate500,
            )
            Text(
                text = "Creada: ${task.createdAt}",
                style = MaterialTheme.typography.bodySmall,
                color = Slate500,
            )
            Text(
                text = "Finalizada: ${task.finishedAt ?: "Pendiente"}",
                style = MaterialTheme.typography.bodySmall,
                color = Slate500,
            )

            if (canManage) {
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val compact = maxWidth < 360.dp
                    if (compact) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = onEdit, modifier = Modifier.fillMaxWidth()) { Text("Editar") }
                            Button(onClick = onToggleStatus, modifier = Modifier.fillMaxWidth()) {
                                Text(if (task.status) "Completar" else "Reabrir")
                            }
                            if (canDelete) {
                                Button(onClick = onDelete, modifier = Modifier.fillMaxWidth()) { Text("Eliminar") }
                            }
                        }
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = onEdit) { Text("Editar") }
                            Button(onClick = onToggleStatus) {
                                Text(if (task.status) "Completar" else "Reabrir")
                            }
                            if (canDelete) {
                                Button(onClick = onDelete) { Text("Eliminar") }
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
private fun TaskFormDialog(
    task: Task?,
    users: List<User>,
    onDismiss: () -> Unit,
    onCreate: (CreateTaskRequest) -> Unit,
    onUpdate: (UpdateTaskRequest) -> Unit,
) {
    var title by remember(task) { mutableStateOf(task?.title.orEmpty()) }
    var description by remember(task) { mutableStateOf(task?.description.orEmpty()) }
    var assignedToId by remember(task) { mutableStateOf(task?.assignedTo) }
    var assignedExpanded by remember { mutableStateOf(false) }
    var status by remember(task) { mutableStateOf(task?.status ?: true) }

    val selectedUser = users.firstOrNull { it.id == assignedToId }
    val canSubmit = title.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (task == null) "Crear tarea" else "Editar tarea") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    singleLine = true,
                )

                ExposedDropdownMenuBox(
                    expanded = assignedExpanded,
                    onExpandedChange = { assignedExpanded = !assignedExpanded },
                ) {
                    OutlinedTextField(
                        value = selectedUser?.name ?: "Sin asignar",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Asignado a") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = assignedExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                    )
                    DropdownMenu(
                        expanded = assignedExpanded,
                        onDismissRequest = { assignedExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Sin asignar") },
                            onClick = {
                                assignedToId = null
                                assignedExpanded = false
                            },
                        )
                        users.forEach { user ->
                            DropdownMenuItem(
                                text = { Text(user.name) },
                                onClick = {
                                    assignedToId = user.id
                                    assignedExpanded = false
                                },
                            )
                        }
                    }
                }

                if (task != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = status, onCheckedChange = { status = it })
                        Text("Tarea pendiente")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (task == null) {
                        onCreate(
                            CreateTaskRequest(
                                title = title.trim(),
                                description = description.trim().ifBlank { null },
                                assignedTo = assignedToId,
                            ),
                        )
                    } else {
                        onUpdate(
                            UpdateTaskRequest(
                                title = title.trim(),
                                description = description.trim().ifBlank { null },
                                assignedTo = assignedToId,
                                status = status,
                            ),
                        )
                    }
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
