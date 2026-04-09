package com.example.smartstock.ui.xml.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.smartstock.R
import com.example.smartstock.domain.model.Task

class TasksAdapter(
    private val canManageTasks: Boolean,
    private val onEdit: (Task) -> Unit,
    private val onToggle: (Task) -> Unit,
    private val onDelete: (Task) -> Unit,
) : ListAdapter<Task, TasksAdapter.TaskViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view, canManageTasks, onEdit, onToggle, onDelete)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TaskViewHolder(
        itemView: View,
        private val canManageTasks: Boolean,
        private val onEdit: (Task) -> Unit,
        private val onToggle: (Task) -> Unit,
        private val onDelete: (Task) -> Unit,
    ) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTaskTitle)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvTaskDescription)
        private val tvAssigned: TextView = itemView.findViewById(R.id.tvTaskAssigned)
        private val tvDates: TextView = itemView.findViewById(R.id.tvTaskDates)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvTaskStatus)
        private val actionsLayout: View = itemView.findViewById(R.id.layoutTaskActions)
        private val btnEdit: Button = itemView.findViewById(R.id.btnEditTask)
        private val btnToggle: Button = itemView.findViewById(R.id.btnToggleTask)
        private val btnDelete: Button = itemView.findViewById(R.id.btnDeleteTask)

        fun bind(item: Task) {
            val assignedLabel =
                item.assignedToName
                    ?: if (item.assignedTo == null) "Sin asignar" else "Usuario sin nombre disponible"

            tvTitle.text = item.title
            tvDescription.text = item.description ?: "Sin descripcion"
            tvAssigned.text = "Asignado a: $assignedLabel"
            tvDates.text = "Creada: ${item.createdAt} | Fin: ${item.finishedAt ?: "-"}"
            tvStatus.text = if (item.status) "ACTIVA" else "INACTIVA"
            btnToggle.text = if (item.status) "Desactivar" else "Activar"

            actionsLayout.visibility = if (canManageTasks) View.VISIBLE else View.GONE
            if (canManageTasks) {
                btnEdit.setOnClickListener { onEdit(item) }
                btnToggle.setOnClickListener { onToggle(item) }
                btnDelete.setOnClickListener { onDelete(item) }
            } else {
                btnEdit.setOnClickListener(null)
                btnToggle.setOnClickListener(null)
                btnDelete.setOnClickListener(null)
            }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean = oldItem == newItem
    }
}
