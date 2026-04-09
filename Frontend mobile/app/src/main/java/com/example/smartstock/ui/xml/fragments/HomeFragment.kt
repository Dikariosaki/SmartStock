package com.example.smartstock.ui.xml.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.smartstock.MainActivity
import com.example.smartstock.R
import com.example.smartstock.core.normalizeRoleName
import com.example.smartstock.ui.session.SessionViewModel
import com.example.smartstock.ui.xml.XmlRoutes
import com.example.smartstock.ui.xml.canAccessXmlRoute
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {
    private val sessionViewModel: SessionViewModel by activityViewModels()

    private lateinit var moduleButtons: Map<String, Button>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvWelcome: TextView = view.findViewById(R.id.tvWelcome)
        val tvHomeSubtitle: TextView = view.findViewById(R.id.tvHomeSubtitle)
        val tvModulesHint: TextView = view.findViewById(R.id.tvModulesHint)

        moduleButtons =
            mapOf(
                XmlRoutes.USERS to view.findViewById(R.id.btnModuleUsers),
                XmlRoutes.PRODUCTS to view.findViewById(R.id.btnModuleProducts),
                XmlRoutes.TASKS to view.findViewById(R.id.btnModuleTasks),
                XmlRoutes.INVENTORY to view.findViewById(R.id.btnModuleInventory),
                XmlRoutes.MOVEMENTS to view.findViewById(R.id.btnModuleMovements),
                XmlRoutes.REPORTS to view.findViewById(R.id.btnModuleReports),
            )

        moduleButtons.forEach { (route, button) ->
            button.setOnClickListener {
                (activity as? MainActivity)?.openRoute(route)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sessionViewModel.uiState.collect { state ->
                    val userName = state.session?.name?.trim().orEmpty()
                    val normalizedRole = normalizeRoleName(state.session?.roleName)
                    tvWelcome.text = if (userName.isBlank()) "Bienvenido" else "Bienvenido, $userName"
                    tvHomeSubtitle.text = subtitleForRole(normalizedRole)

                    val visibleModules =
                        moduleButtons.count { (route, button) ->
                            val canAccess = canAccessXmlRoute(normalizedRole, route)
                            button.visibility = if (canAccess) View.VISIBLE else View.GONE
                            canAccess
                        }

                    tvModulesHint.text =
                        if (visibleModules > 0) {
                            "Selecciona un modulo para continuar."
                        } else {
                            "No hay modulos habilitados para tu rol."
                        }
                }
            }
        }
    }

    private fun subtitleForRole(role: String?): String {
        return when (role) {
            "administrador" -> "Gestion total del sistema: usuarios, inventario, tareas y reportes."
            "supervisor" -> "Control operativo de inventario, tareas y reportes."
            "auxiliar" -> "Gestion operativa de productos y tareas."
            else -> "Administra tus modulos habilitados segun tu rol."
        }
    }
}
