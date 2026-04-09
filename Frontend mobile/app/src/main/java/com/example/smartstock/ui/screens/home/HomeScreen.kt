package com.example.smartstock.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smartstock.ui.components.BottomNavBar
import com.example.smartstock.ui.components.PageHeader
import com.example.smartstock.ui.components.Slate200
import com.example.smartstock.ui.components.Slate500
import com.example.smartstock.ui.components.Slate900
import com.example.smartstock.ui.components.navItemsForRole
import com.example.smartstock.ui.components.smartStockPageBackground

@Composable
fun HomeScreen(
    currentRoute: String,
    roleName: String?,
    onNavigate: (String) -> Unit,
) {
    val modules = visibleModulesForRole(roleName)

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            BottomNavBar(
                items = navItemsForRole(roleName),
                currentRoute = currentRoute,
                onItemClick = { onNavigate(it.route) },
            )
        },
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 190.dp),
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
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Slate200),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                ) {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        PageHeader(
                            title = "Dashboard Principal",
                            subtitle = "Administra los módulos habilitados para tu rol.",
                        )
                        Text(
                            text = "Selecciona un módulo para gestionar sus operaciones.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate500,
                        )
                    }
                }
            }

            items(modules, key = { it.route }) { module ->
                HomeModuleCard(module = module, onClick = { onNavigate(module.route) })
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(modifier = Modifier.height(76.dp))
            }
        }
    }
}

@Composable
private fun HomeModuleCard(
    module: HomeModule,
    onClick: () -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Slate200),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Surface(
                    shape = CircleShape,
                    color = module.badgeBg,
                ) {
                    Text(
                        text = module.title.take(1),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        color = module.badgeFg,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = module.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Slate900,
                    )
                    Text(
                        text = module.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500,
                    )
                }
            }
            Text(
                text = "Abrir módulo",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

private fun visibleModulesForRole(roleName: String?): List<HomeModule> {
    val role = roleName?.trim()?.lowercase()
    val allModules =
        listOf(
            HomeModule("Usuarios", "Gestión de accesos", "users", Color(0xFFDBEAFE), Color(0xFF1D4ED8)),
            HomeModule("Productos", "Catálogo y precios", "products", Color(0xFFE0E7FF), Color(0xFF4338CA)),
            HomeModule("Tareas", "Asignaciones operativas", "tasks", Color(0xFFE0F2FE), Color(0xFF0369A1)),
            HomeModule("Inventario", "Control de stock", "inventory", Color(0xFFDCFCE7), Color(0xFF166534)),
            HomeModule("Movimientos", "Entradas y salidas", "movements", Color(0xFFFFEDD5), Color(0xFF9A3412)),
            HomeModule("Reportes", "Análisis y trazabilidad", "reports", Color(0xFFFCE7F3), Color(0xFF9D174D)),
        )

    return when (role) {
        "administrador" -> allModules
        "supervisor" -> allModules.filter { it.route in setOf("products", "tasks", "inventory", "movements", "reports") }
        "auxiliar" -> allModules.filter { it.route in setOf("products", "tasks", "reports") }
        else -> emptyList()
    }
}

data class HomeModule(
    val title: String,
    val subtitle: String,
    val route: String,
    val badgeBg: Color,
    val badgeFg: Color,
)
