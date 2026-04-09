package com.example.smartstock.ui.xml.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartstock.MainActivity
import com.example.smartstock.R
import com.example.smartstock.core.normalizeRoleName
import com.example.smartstock.domain.model.Inventory
import com.example.smartstock.domain.model.MovementStockRequest
import com.example.smartstock.domain.model.UpdateInventoryRequest
import com.example.smartstock.domain.model.User
import com.example.smartstock.ui.screens.inventory.InventoryViewModel
import com.example.smartstock.ui.session.SessionViewModel
import com.example.smartstock.ui.xml.XmlRoutes
import com.example.smartstock.ui.xml.adapters.InventoryAdapter
import com.example.smartstock.ui.xml.canAccessXmlRoute
import com.example.smartstock.ui.xml.dropdown.DropdownOption
import com.example.smartstock.ui.xml.dropdown.bindSearchableDropdown
import com.example.smartstock.ui.xml.toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class InventoryFragment : Fragment(R.layout.fragment_inventory) {
    private val viewModel: InventoryViewModel by viewModels()
    private val sessionViewModel: SessionViewModel by activityViewModels()

    private lateinit var adapter: InventoryAdapter
    private var users: List<User> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val normalizedRole = normalizeRoleName(sessionViewModel.uiState.value.session?.roleName)
        if (!canAccessXmlRoute(normalizedRole, XmlRoutes.INVENTORY)) {
            requireContext().toast("No tienes permiso para acceder a Inventario")
            (activity as? MainActivity)?.openRoute(XmlRoutes.HOME, force = true)
            return
        }

        val rv: RecyclerView = view.findViewById(R.id.rvInventory)
        val btnRefresh: Button = view.findViewById(R.id.btnRefreshInventory)
        val progress: ProgressBar = view.findViewById(R.id.progressInventory)
        val tvError: TextView = view.findViewById(R.id.tvInventoryError)

        adapter =
            InventoryAdapter(
                onEdit = { inventory -> showUpdateDialog(inventory) },
                onToggle = { inventory -> viewModel.toggleInventoryStatus(inventory.id, inventory.status) },
                onEntry = { inventory -> showStockDialog(inventory, "entrada") },
                onExit = { inventory -> showStockDialog(inventory, "salida") },
                onDamage = { inventory -> showStockDialog(inventory, "averia") },
            )

        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        btnRefresh.setOnClickListener { viewModel.loadInventories() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    progress.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                    users = state.users
                    adapter.submitList(state.inventories)

                    if (state.errorMessage.isNullOrBlank()) {
                        tvError.visibility = View.GONE
                    } else {
                        tvError.visibility = View.VISIBLE
                        tvError.text = state.errorMessage
                    }
                }
            }
        }
    }

    private fun showUpdateDialog(item: Inventory) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_inventory_update, null)
        val etLocation: EditText = dialogView.findViewById(R.id.etLocation)
        val etReorderPoint: EditText = dialogView.findViewById(R.id.etReorderPoint)

        etLocation.setText(item.location.orEmpty())
        etReorderPoint.setText(item.reorderPoint.toString())

        AlertDialog
            .Builder(requireContext())
            .setTitle("Actualizar inventario")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val location = etLocation.text.toString().trim().ifBlank { null }
                val reorderPoint = etReorderPoint.text.toString().trim().toIntOrNull()
                if (reorderPoint == null) {
                    requireContext().toast("Punto de reorden invalido")
                    return@setPositiveButton
                }

                viewModel.updateInventory(
                    item.id,
                    UpdateInventoryRequest(
                        location = location,
                        reorderPoint = reorderPoint,
                    ),
                )
            }.setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showStockDialog(item: Inventory, type: String) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_stock_movement, null)

        val etQuantity: EditText = dialogView.findViewById(R.id.etQuantity)
        val actvUser: AppCompatAutoCompleteTextView = dialogView.findViewById(R.id.spUser)
        val etBatch: EditText = dialogView.findViewById(R.id.etBatch)

        val usersForUi =
            if (users.isNotEmpty()) {
                users
            } else {
                sessionViewModel.uiState.value.session?.let {
                    listOf(
                        User(
                            id = it.userId,
                            roleId = it.roleId,
                            roleName = it.roleName,
                            name = it.name,
                            cedula = 0,
                            email = it.email,
                            status = true,
                            phone = null,
                        ),
                    )
                }.orEmpty()
            }

        if (usersForUi.isEmpty()) {
            requireContext().toast("No hay usuario disponible para registrar movimiento")
            return
        }

        val userOptions = usersForUi.map { user -> DropdownOption(user.id, user.name) }
        val preferredUserId = sessionViewModel.uiState.value.session?.userId
        val initialUser =
            userOptions.firstOrNull { option -> option.value == preferredUserId }
                ?: userOptions.firstOrNull()
        var selectedUser = initialUser

        actvUser.bindSearchableDropdown(userOptions, initialUser) { option ->
            selectedUser = option
        }

        AlertDialog
            .Builder(requireContext())
            .setTitle("Registrar $type")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val quantity = etQuantity.text.toString().trim().toIntOrNull()
                val userId = selectedUser?.value
                val batch = etBatch.text.toString().trim().ifBlank { null }

                if (quantity == null || quantity <= 0 || userId == null) {
                    requireContext().toast("Cantidad o usuario invalido")
                    return@setPositiveButton
                }

                val request =
                    MovementStockRequest(
                        productId = item.productId,
                        quantity = quantity,
                        userId = userId,
                        type = type,
                        batch = batch,
                    )

                when (type) {
                    "entrada" -> viewModel.registerEntry(request)
                    "salida" -> viewModel.registerExit(request)
                    else -> viewModel.registerDamage(request)
                }
            }.setNegativeButton("Cancelar", null)
            .show()
    }
}
