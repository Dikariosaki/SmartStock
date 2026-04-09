package com.example.smartstock.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

val Slate50 = Color(0xFFF8FAFC)
val Slate100 = Color(0xFFF1F5F9)
val Slate200 = Color(0xFFE2E8F0)
val Slate500 = Color(0xFF64748B)
val Slate700 = Color(0xFF334155)
val Slate900 = Color(0xFF0F172A)
val SuccessBg = Color(0xFFDCFCE7)
val SuccessFg = Color(0xFF166534)
val DangerBg = Color(0xFFFEE2E2)
val DangerFg = Color(0xFF991B1B)

val PageBackgroundBrush = Brush.verticalGradient(
    colors = listOf(Slate50, Color(0xFFF1F5F9))
)

fun Modifier.smartStockPageBackground(): Modifier = background(PageBackgroundBrush)

data class FilterOption(
    val key: String,
    val label: String
)

fun navItemsForRole(roleName: String?): List<NavItem> {
    val role = roleName?.trim()?.lowercase()
    if (role == null) return emptyList()

    return when (role) {
        "administrador" ->
            listOf(
                NavItem("home", "Inicio"),
                NavItem("users", "Usuarios"),
                NavItem("products", "Productos"),
                NavItem("tasks", "Tareas"),
                NavItem("inventory", "Inventario"),
                NavItem("movements", "Movimientos"),
                NavItem("reports", "Reportes"),
                NavItem("profile", "Perfil"),
            )
        "supervisor" ->
            listOf(
                NavItem("home", "Inicio"),
                NavItem("products", "Productos"),
                NavItem("tasks", "Tareas"),
                NavItem("inventory", "Inventario"),
                NavItem("movements", "Movimientos"),
                NavItem("reports", "Reportes"),
                NavItem("profile", "Perfil"),
            )
        "auxiliar" ->
            listOf(
                NavItem("home", "Inicio"),
                NavItem("products", "Productos"),
                NavItem("tasks", "Tareas"),
                NavItem("reports", "Reportes"),
                NavItem("profile", "Perfil"),
            )
        else -> emptyList()
    }
}

fun defaultNavItems(): List<NavItem> = navItemsForRole("administrador")

@Composable
fun PageHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Slate900
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = Slate500
        )
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ModuleToolbar(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    searchPlaceholder: String,
    actionLabel: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
    actionEnabled: Boolean = true,
    extraControls: @Composable ColumnScope.(compact: Boolean) -> Unit = {}
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            val compact = maxWidth < 680.dp
            if (compact) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SearchField(
                        value = searchText,
                        onValueChange = onSearchTextChange,
                        placeholder = searchPlaceholder,
                        modifier = Modifier.fillMaxWidth()
                    )
                    extraControls(compact)
                    Button(
                        onClick = onActionClick,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = actionEnabled,
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(actionLabel)
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SearchField(
                        value = searchText,
                        onValueChange = onSearchTextChange,
                        placeholder = searchPlaceholder,
                        modifier = Modifier
                            .weight(1f)
                            .widthIn(max = 420.dp)
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        extraControls(compact)
                    }
                    Button(
                        onClick = onActionClick,
                        enabled = actionEnabled,
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(actionLabel)
                    }
                }
            }
        }
    }
}

@Composable
fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        singleLine = true,
        placeholder = { Text(placeholder) },
        shape = MaterialTheme.shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = Slate100,
            focusedContainerColor = Slate100,
            disabledContainerColor = Slate100,
            unfocusedBorderColor = Slate200,
            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        )
    )
}

@Composable
fun FilterChipsRow(
    options: List<FilterOption>,
    selectedKey: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            FilterChip(
                selected = selectedKey == option.key,
                onClick = { onSelected(option.key) },
                label = { Text(option.label) }
            )
        }
    }
}

@Composable
fun StatusPill(
    active: Boolean,
    modifier: Modifier = Modifier,
    activeLabel: String = "Activo",
    inactiveLabel: String = "Inactivo"
) {
    val bg = if (active) SuccessBg else DangerBg
    val fg = if (active) SuccessFg else DangerFg

    Surface(
        modifier = modifier,
        color = bg,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = if (active) activeLabel else inactiveLabel,
            color = fg,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun EmptyStateCard(
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Slate500
            )
        }
    }
}

@Composable
fun FullScreenLoading(innerPadding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
