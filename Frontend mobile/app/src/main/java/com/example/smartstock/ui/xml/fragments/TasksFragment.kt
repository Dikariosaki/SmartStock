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
import com.example.smartstock.R
import com.example.smartstock.core.normalizeRoleName
import com.example.smartstock.domain.model.CreateTaskRequest
import com.example.smartstock.domain.model.Task
import com.example.smartstock.domain.model.UpdateTaskRequest
import com.example.smartstock.domain.model.User
import com.example.smartstock.ui.screens.tasks.TasksViewModel
import com.example.smartstock.ui.session.SessionViewModel
import com.example.smartstock.ui.xml.adapters.TasksAdapter
import com.example.smartstock.ui.xml.dropdown.DropdownOption
import com.example.smartstock.ui.xml.dropdown.bindSearchableDropdown
import com.example.smartstock.ui.xml.toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TasksFragment : Fragment(R.layout.fragment_tasks) {
    private val viewModel: TasksViewModel by viewModels()
    private val sessionViewModel: SessionViewModel by activityViewModels()

    private lateinit var adapter: TasksAdapter
    private var users: List<User> = emptyList()
    private var canManageTasks: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv: RecyclerView = view.findViewById(R.id.rvTasks)
        val btnCreate: Button = view.findViewById(R.id.btnCreateTask)
        val btnRefresh: Button = view.findViewById(R.id.btnRefreshTasks)
        val progress: ProgressBar = view.findViewById(R.id.progressTasks)
        val tvError: TextView = view.findViewById(R.id.tvTasksError)
        val role = normalizeRoleName(sessionViewModel.uiState.value.session?.roleName)
        canManageTasks = role in setOf("administrador", "supervisor")

        adapter =
            TasksAdapter(
                canManageTasks = canManageTasks,
                onEdit = { task -> showTaskDialog(task) },
                onToggle = { task -> viewModel.toggleTaskStatus(task.id, task.status) },
                onDelete = { task -> confirmDelete(task) },
            )

        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        btnCreate.visibility = if (canManageTasks) View.VISIBLE else View.GONE
        if (canManageTasks) {
            viewModel.loadUsers()
        }

        btnCreate.setOnClickListener {
            if (!canManageTasks) {
                requireContext().toast("No tienes permiso para crear tareas")
                return@setOnClickListener
            }
            showTaskDialog(null)
        }
        btnRefresh.setOnClickListener { viewModel.loadTasks() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    progress.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                    users = state.users
                    adapter.submitList(state.tasks)

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

    private fun showTaskDialog(taskToEdit: Task?) {
        if (!canManageTasks) {
            requireContext().toast("No tienes permiso para modificar tareas")
            return
        }

        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_task_form, null)
        val etTitle: EditText = dialogView.findViewById(R.id.etTitle)
        val etDescription: EditText = dialogView.findViewById(R.id.etDescription)
        val actvUser: AppCompatAutoCompleteTextView = dialogView.findViewById(R.id.spAssignedUser)

        val userOptions =
            buildList {
                add(DropdownOption<Int?>(null, "Sin asignar"))
                addAll(users.map { user -> DropdownOption<Int?>(user.id, user.name) })
            }
        val initialUser =
            userOptions.firstOrNull { option -> option.value == taskToEdit?.assignedTo }
                ?: userOptions.firstOrNull()
        var selectedUser = initialUser

        actvUser.bindSearchableDropdown(userOptions, initialUser) { option ->
            selectedUser = option
        }

        if (taskToEdit != null) {
            etTitle.setText(taskToEdit.title)
            etDescription.setText(taskToEdit.description.orEmpty())
        }

        AlertDialog
            .Builder(requireContext())
            .setTitle(if (taskToEdit == null) "Crear tarea" else "Editar tarea")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val title = etTitle.text.toString().trim()
                val description = etDescription.text.toString().trim().ifBlank { null }
                val assignedUserId = selectedUser?.value

                if (title.isBlank()) {
                    requireContext().toast("El titulo es obligatorio")
                    return@setPositiveButton
                }

                if (selectedUser == null) {
                    requireContext().toast("Selecciona un responsable valido o deja Sin asignar")
                    return@setPositiveButton
                }

                if (taskToEdit == null) {
                    viewModel.createTask(
                        CreateTaskRequest(
                            title = title,
                            description = description,
                            assignedTo = assignedUserId,
                        ),
                    )
                } else {
                    viewModel.updateTask(
                        taskToEdit.id,
                        UpdateTaskRequest(
                            title = title,
                            description = description,
                            assignedTo = assignedUserId,
                            status = taskToEdit.status,
                        ),
                    )
                }
            }.setNegativeButton("Cancelar", null)
            .show()
    }

    private fun confirmDelete(task: Task) {
        if (!canManageTasks) {
            requireContext().toast("No tienes permiso para eliminar tareas")
            return
        }

        AlertDialog
            .Builder(requireContext())
            .setTitle("Eliminar tarea")
            .setMessage("Se eliminara la tarea ${task.title}. Continuar?")
            .setPositiveButton("Eliminar") { _, _ -> viewModel.deleteTask(task.id) }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
