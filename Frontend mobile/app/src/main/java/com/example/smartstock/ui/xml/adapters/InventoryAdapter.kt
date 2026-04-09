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
import com.example.smartstock.domain.model.Inventory

class InventoryAdapter(
    private val onEdit: (Inventory) -> Unit,
    private val onToggle: (Inventory) -> Unit,
    private val onEntry: (Inventory) -> Unit,
    private val onExit: (Inventory) -> Unit,
    private val onDamage: (Inventory) -> Unit,
) : ListAdapter<Inventory, InventoryAdapter.InventoryViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_inventory, parent, false)
        return InventoryViewHolder(view, onEdit, onToggle, onEntry, onExit, onDamage)
    }

    override fun onBindViewHolder(holder: InventoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class InventoryViewHolder(
        itemView: View,
        private val onEdit: (Inventory) -> Unit,
        private val onToggle: (Inventory) -> Unit,
        private val onEntry: (Inventory) -> Unit,
        private val onExit: (Inventory) -> Unit,
        private val onDamage: (Inventory) -> Unit,
    ) : RecyclerView.ViewHolder(itemView) {
        private val tvProduct: TextView = itemView.findViewById(R.id.tvInventoryProduct)
        private val tvLocation: TextView = itemView.findViewById(R.id.tvInventoryLocation)
        private val tvQty: TextView = itemView.findViewById(R.id.tvInventoryQty)
        private val tvReorder: TextView = itemView.findViewById(R.id.tvInventoryReorder)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvInventoryStatus)
        private val btnEdit: Button = itemView.findViewById(R.id.btnEditInventory)
        private val btnToggle: Button = itemView.findViewById(R.id.btnToggleInventory)
        private val btnEntry: Button = itemView.findViewById(R.id.btnEntry)
        private val btnExit: Button = itemView.findViewById(R.id.btnExit)
        private val btnDamage: Button = itemView.findViewById(R.id.btnDamage)

        fun bind(item: Inventory) {
            tvProduct.text = item.productName ?: "Producto sin nombre disponible"
            tvLocation.text = "Ubicacion: ${item.location ?: "-"}"
            tvQty.text = "Cantidad: ${item.quantity}"
            tvReorder.text = "Punto reorden: ${item.reorderPoint}"
            tvStatus.text = if (item.status) "ACTIVO" else "INACTIVO"
            btnToggle.text = if (item.status) "Desactivar" else "Activar"

            btnEdit.setOnClickListener { onEdit(item) }
            btnToggle.setOnClickListener { onToggle(item) }
            btnEntry.setOnClickListener { onEntry(item) }
            btnExit.setOnClickListener { onExit(item) }
            btnDamage.setOnClickListener { onDamage(item) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<Inventory>() {
        override fun areItemsTheSame(oldItem: Inventory, newItem: Inventory): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Inventory, newItem: Inventory): Boolean = oldItem == newItem
    }
}
