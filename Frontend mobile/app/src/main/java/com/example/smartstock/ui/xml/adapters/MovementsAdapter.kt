package com.example.smartstock.ui.xml.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.smartstock.R
import com.example.smartstock.domain.model.Movement

class MovementsAdapter : ListAdapter<Movement, MovementsAdapter.MovementViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovementViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_movement, parent, false)
        return MovementViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovementViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MovementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvType: TextView = itemView.findViewById(R.id.tvMovementType)
        private val tvQty: TextView = itemView.findViewById(R.id.tvMovementQty)
        private val tvUser: TextView = itemView.findViewById(R.id.tvMovementUser)
        private val tvProduct: TextView = itemView.findViewById(R.id.tvMovementProduct)
        private val tvDate: TextView = itemView.findViewById(R.id.tvMovementDate)

        fun bind(item: Movement) {
            tvType.text = "${item.type.uppercase()} (${if (item.status) "activo" else "inactivo"})"
            tvQty.text = "Cantidad: ${item.quantity} | Ubicacion: ${item.inventoryLocation ?: "-"}"
            tvUser.text = "Usuario: ${item.userName ?: "Usuario sin nombre disponible"}"
            tvProduct.text = "Producto: ${item.productName ?: "Producto sin nombre disponible"}"
            tvDate.text = "Fecha: ${item.date ?: "-"} | Lote: ${item.batch ?: "-"}"
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<Movement>() {
        override fun areItemsTheSame(oldItem: Movement, newItem: Movement): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Movement, newItem: Movement): Boolean = oldItem == newItem
    }
}
