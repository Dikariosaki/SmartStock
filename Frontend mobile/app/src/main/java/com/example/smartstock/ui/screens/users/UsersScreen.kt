package com.example.smartstock.ui.screens.users

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import com.example.smartstock.domain.model.CreateUserRequest
import com.example.smartstock.domain.model.RoleOption
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

private val userStatusFilters =
    listOf(
        FilterOption("all", "Todos"),
        FilterOption("active", "Activos"),
        FilterOption("inactive", "Inactivos"),
    )

@Composable
fun UsersScreen(
    currentRoute: String,
    roleName: String?,
    onNavigate: (String) -> Unit,
    viewModel: UsersViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showForm by remember { mutableStateOf(false) }
    var editingUser by remember { mutableStateOf<User?>(null) }
    var selectedStatus by remember { mutableStateOf("all") }

    val shownUsers =
        uiState.filteredUsers.filter { user ->
            when (selectedStatus) {
                "active" -> user.status
                "inactive" -> !user.status
                else -> true
            }
        }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    if (showForm) {
        val selectableRoles =
            uiState.roles.filter { role ->
                role.name.trim().lowercase() != "administrador" || editingUser?.roleId == role.id
            }
        UserFormDialog(
            user = editingUser,
            roles = selectableRoles,
            loadingRoles = uiState.isLoadingRoles,
            onDismiss = {
                showForm = false
                editingUser = null
            },
            onSubmit = { request ->
                if (editingUser == null) {
                    viewModel.createUser(request)
                } else {
                    viewModel.updateUser(editingUser!!.id, request)
                }
                showForm = false
                editingUser = null
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
                columns = GridCells.Adaptive(minSize = 310.dp),
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
                        title = "Gestión de usuarios",
                        subtitle = "Administra accesos y datos del personal operativo.",
                    )
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    ModuleToolbar(
                        searchText = uiState.searchText,
                        onSearchTextChange = viewModel::onSearchChange,
                        searchPlaceholder = "Buscar usuarios por nombre, cédula o correo...",
                        actionLabel = "Agregar usuario",
                        onActionClick = {
                            editingUser = null
                            showForm = true
                        },
                        extraControls = {
                            FilterChipsRow(
                                options = userStatusFilters,
                                selectedKey = selectedStatus,
                                onSelected = { selectedStatus = it },
                            )
                        },
                    )
                }

                items(shownUsers, key = { it.id }) { user ->
                    UserCard(
                        user = user,
                        onEdit = {
                            editingUser = user
                            showForm = true
                        },
                        onToggleStatus = { viewModel.toggleUserStatus(user.id, user.status) },
                        onDelete = { viewModel.deleteUser(user.id) },
                    )
                }

                if (shownUsers.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        EmptyStateCard(
                            title = "Sin resultados",
                            message = "No se encontraron usuarios para los filtros seleccionados.",
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
private fun UserCard(
    user: User,
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
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Surface(shape = CircleShape, color = Color(0xFFDBEAFE)) {
                    Text(
                        text = user.name.take(1).uppercase(),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        color = Color(0xFF1D4ED8),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "Rol: ${user.roleName ?: "No definido"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500,
                    )
                    Text(
                        text = "Cédula: ${user.cedula}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500,
                    )
                }
                StatusPill(active = user.status)
            }

            Text(
                text = user.email,
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = user.phone ?: "Sin teléfono",
                style = MaterialTheme.typography.bodySmall,
                color = Slate500,
            )

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val compact = maxWidth < 360.dp
                if (compact) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = onEdit, modifier = Modifier.fillMaxWidth()) { Text("Editar") }
                        Button(onClick = onToggleStatus, modifier = Modifier.fillMaxWidth()) {
                            Text(if (user.status) "Desactivar" else "Activar")
                        }
                        Button(onClick = onDelete, modifier = Modifier.fillMaxWidth()) { Text("Eliminar") }
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = onEdit) { Text("Editar") }
                        Button(onClick = onToggleStatus) {
                            Text(if (user.status) "Desactivar" else "Activar")
                        }
                        Button(onClick = onDelete) { Text("Eliminar") }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserFormDialog(
    user: User?,
    roles: List<RoleOption>,
    loadingRoles: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (CreateUserRequest) -> Unit,
) {
    var roleId by remember(user) { mutableStateOf(user?.roleId) }
    var roleExpanded by remember { mutableStateOf(false) }
    var name by remember(user) { mutableStateOf(user?.name.orEmpty()) }
    var cedula by remember(user) { mutableStateOf(user?.cedula?.toString().orEmpty()) }
    var email by remember(user) { mutableStateOf(user?.email.orEmpty()) }
    var phone by remember(user) { mutableStateOf(user?.phone.orEmpty()) }
    var password by remember(user) { mutableStateOf("") }
    var status by remember(user) { mutableStateOf(user?.status ?: true) }

    val selectedRole = roles.firstOrNull { it.id == roleId }
    val document = cedula.toIntOrNull()
    val canSubmit = roleId != null && document != null && name.isNotBlank() && email.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (user == null) "Crear usuario" else "Editar usuario") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExposedDropdownMenuBox(
                    expanded = roleExpanded,
                    onExpandedChange = { roleExpanded = !roleExpanded },
                ) {
                    OutlinedTextField(
                        value =
                            when {
                                loadingRoles -> "Cargando roles..."
                                selectedRole != null -> selectedRole.name
                                else -> "Selecciona un rol"
                            },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Rol") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                    )

                    DropdownMenu(
                        expanded = roleExpanded,
                        onDismissRequest = { roleExpanded = false },
                    ) {
                        roles.forEach { role ->
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text(role.name) },
                                onClick = {
                                    roleId = role.id
                                    roleExpanded = false
                                },
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = cedula,
                    onValueChange = { cedula = it },
                    label = { Text("Cédula") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Teléfono") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(if (user == null) "Contraseña" else "Contraseña (opcional)") },
                    singleLine = true,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = status, onCheckedChange = { status = it })
                    Text("Usuario activo")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val safeRole = roleId ?: return@TextButton
                    val safeDocument = document ?: return@TextButton
                    onSubmit(
                        CreateUserRequest(
                            roleId = safeRole,
                            name = name.trim(),
                            cedula = safeDocument,
                            email = email.trim(),
                            password = password.trim().ifBlank { null },
                            phone = phone.trim().ifBlank { null },
                            status = status,
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
