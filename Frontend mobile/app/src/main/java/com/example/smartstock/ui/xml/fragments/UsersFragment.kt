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
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartstock.R
import com.example.smartstock.domain.model.CreateUserRequest
import com.example.smartstock.domain.model.RoleOption
import com.example.smartstock.domain.model.User
import com.example.smartstock.ui.screens.users.UsersAdapter
import com.example.smartstock.ui.screens.users.UsersViewModel
import com.example.smartstock.ui.xml.afterTextChanged
import com.example.smartstock.ui.xml.dropdown.DropdownOption
import com.example.smartstock.ui.xml.dropdown.bindSearchableDropdown
import com.example.smartstock.ui.xml.toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UsersFragment : Fragment(R.layout.fragment_users) {
    private val viewModel: UsersViewModel by viewModels()

    private lateinit var adapter: UsersAdapter
    private var currentRoles: List<RoleOption> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvUsers: RecyclerView = view.findViewById(R.id.rvUsers)
        val etSearch: EditText = view.findViewById(R.id.etSearch)
        val btnCreate: Button = view.findViewById(R.id.btnCreateUser)
        val btnRefresh: Button = view.findViewById(R.id.btnRefreshUsers)
        val progress: ProgressBar = view.findViewById(R.id.progressUsers)
        val tvError: TextView = view.findViewById(R.id.tvUsersError)

        adapter =
            UsersAdapter(
                onEdit = { user -> showUserFormDialog(user) },
                onToggleStatus = { user -> viewModel.toggleUserStatus(user.id, user.status) },
                onDelete = { user -> confirmDelete(user) },
            )

        rvUsers.layoutManager = LinearLayoutManager(requireContext())
        rvUsers.adapter = adapter

        etSearch.afterTextChanged { query -> viewModel.onSearchChange(query) }

        btnCreate.setOnClickListener { showUserFormDialog(null) }
        btnRefresh.setOnClickListener { viewModel.loadUsers() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    progress.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                    currentRoles = state.roles
                    adapter.submitList(state.filteredUsers)

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

    private fun showUserFormDialog(userToEdit: User?) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_user_form, null)

        val actvRole: AppCompatAutoCompleteTextView = dialogView.findViewById(R.id.spRole)
        val etName: EditText = dialogView.findViewById(R.id.etName)
        val etCedula: EditText = dialogView.findViewById(R.id.etCedula)
        val etEmail: EditText = dialogView.findViewById(R.id.etEmail)
        val etPhone: EditText = dialogView.findViewById(R.id.etPhone)
        val etPassword: EditText = dialogView.findViewById(R.id.etPassword)

        val filteredRoles = currentRoles.filterNot { it.name.trim().equals("administrador", ignoreCase = true) }
        val rolesForUi = if (filteredRoles.isNotEmpty()) filteredRoles else currentRoles

        if (rolesForUi.isEmpty()) {
            requireContext().toast("No hay roles disponibles")
            return
        }

        val roleOptions = rolesForUi.map { role -> DropdownOption(role.id, role.name) }
        val initialRole =
            roleOptions.firstOrNull { option -> option.value == userToEdit?.roleId }
                ?: roleOptions.firstOrNull()
        var selectedRole = initialRole

        actvRole.bindSearchableDropdown(roleOptions, initialRole) { option ->
            selectedRole = option
        }

        if (userToEdit != null) {
            etName.setText(userToEdit.name)
            etCedula.setText(userToEdit.cedula.toString())
            etEmail.setText(userToEdit.email)
            etPhone.setText(userToEdit.phone.orEmpty())
            etPassword.hint = "Contrasena (opcional)"
        }

        AlertDialog
            .Builder(requireContext())
            .setTitle(if (userToEdit == null) "Crear usuario" else "Editar usuario")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val roleId = selectedRole?.value
                if (roleId == null) {
                    requireContext().toast("Selecciona un rol valido")
                    return@setPositiveButton
                }

                val name = etName.text.toString().trim()
                val cedula = etCedula.text.toString().trim().toIntOrNull()
                val email = etEmail.text.toString().trim()
                val phone = etPhone.text.toString().trim().ifBlank { null }
                val passwordRaw = etPassword.text.toString().trim()
                val password = if (passwordRaw.isBlank()) null else passwordRaw

                if (name.isBlank() || cedula == null || email.isBlank()) {
                    requireContext().toast("Completa nombre, cedula y correo")
                    return@setPositiveButton
                }

                if (userToEdit == null && password.isNullOrBlank()) {
                    requireContext().toast("La contrasena es obligatoria al crear")
                    return@setPositiveButton
                }

                val request =
                    CreateUserRequest(
                        roleId = roleId,
                        name = name,
                        cedula = cedula,
                        email = email,
                        password = password,
                        phone = phone,
                        status = userToEdit?.status ?: true,
                    )

                if (userToEdit == null) {
                    viewModel.createUser(request)
                } else {
                    viewModel.updateUser(userToEdit.id, request)
                }
            }.setNegativeButton("Cancelar", null)
            .show()
    }

    private fun confirmDelete(user: User) {
        AlertDialog
            .Builder(requireContext())
            .setTitle("Eliminar usuario")
            .setMessage("Se eliminara a ${user.name}. Deseas continuar?")
            .setPositiveButton("Eliminar") { _, _ -> viewModel.deleteUser(user.id) }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
