package com.example.smartstock.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smartstock.domain.model.SessionUser
import com.example.smartstock.ui.components.BottomNavBar
import com.example.smartstock.ui.components.PageHeader
import com.example.smartstock.ui.components.Slate200
import com.example.smartstock.ui.components.Slate500
import com.example.smartstock.ui.components.navItemsForRole
import com.example.smartstock.ui.components.smartStockPageBackground

@Composable
fun ProfileScreen(
    currentRoute: String,
    roleName: String?,
    currentSession: SessionUser?,
    onLogout: () -> Unit,
    onNavigate: (String) -> Unit,
) {
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
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .smartStockPageBackground()
                    .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                PageHeader(
                    title = "Perfil",
                    subtitle = "Consulta tu información de sesión actual.",
                )
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Slate200),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                ) {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = "Datos del usuario",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        ProfileDataRow("Nombre", currentSession?.name ?: "No disponible")
                        ProfileDataRow("Correo", currentSession?.email ?: "No disponible")
                        ProfileDataRow("Rol", currentSession?.roleName ?: "No disponible")
                    }
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Slate200),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                ) {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "Sesión",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "Cerrar sesión limpiará el token JWT y regresará al login.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate500,
                        )
                        Button(
                            onClick = onLogout,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Cerrar sesión")
                        }
                        TextButton(
                            onClick = { onNavigate("home") },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Volver al inicio")
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(74.dp)) }
        }
    }
}

@Composable
private fun ProfileDataRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Slate500,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
        )
    }
}
