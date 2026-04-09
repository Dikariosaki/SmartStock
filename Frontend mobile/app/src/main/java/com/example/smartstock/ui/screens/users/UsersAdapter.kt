package com.example.smartstock.ui.screens.users

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.smartstock.R
import com.example.smartstock.domain.model.User

class UsersAdapter(
    private val onEdit: (User) -> Unit,
    private val onToggleStatus: (User) -> Unit,
    private val onDelete: (User) -> Unit
) : ListAdapter<User, UsersAdapter.UserViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view, onEdit, onToggleStatus, onDelete)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class UserViewHolder(
        itemView: View,
        private val onEdit: (User) -> Unit,
        private val onToggleStatus: (User) -> Unit,
        private val onDelete: (User) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvCedula: TextView = itemView.findViewById(R.id.tvCedula)
        private val tvEmail: TextView = itemView.findViewById(R.id.tvEmail)
        private val tvPhone: TextView = itemView.findViewById(R.id.tvPhone)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val btnEdit: Button = itemView.findViewById(R.id.btnEditUser)
        private val btnToggle: Button = itemView.findViewById(R.id.btnToggleStatus)
        private val btnDelete: Button = itemView.findViewById(R.id.btnDeleteUser)

        fun bind(user: User) {
            tvName.text = user.name
            tvCedula.text = "Cedula: ${user.cedula}"
            tvEmail.text = user.email
            tvPhone.text = user.phone ?: "Sin telefono"
            tvStatus.text = if (user.status) "ACTIVO" else "INACTIVO"
            btnEdit.setOnClickListener { onEdit(user) }
            btnToggle.text = if (user.status) "Desactivar" else "Activar"
            btnToggle.setOnClickListener { onToggleStatus(user) }
            btnDelete.setOnClickListener { onDelete(user) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean = oldItem == newItem
    }
}
