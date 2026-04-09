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
import com.example.smartstock.domain.model.Product

class ProductsAdapter(
    private val onEdit: (Product) -> Unit,
    private val onToggle: (Product) -> Unit,
) : ListAdapter<Product, ProductsAdapter.ProductViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view, onEdit, onToggle)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ProductViewHolder(
        itemView: View,
        private val onEdit: (Product) -> Unit,
        private val onToggle: (Product) -> Unit,
    ) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvProductName)
        private val tvCode: TextView = itemView.findViewById(R.id.tvProductCode)
        private val tvSubcategory: TextView = itemView.findViewById(R.id.tvProductSubcategory)
        private val tvPrice: TextView = itemView.findViewById(R.id.tvProductPrice)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvProductStatus)
        private val btnEdit: Button = itemView.findViewById(R.id.btnEditProduct)
        private val btnToggle: Button = itemView.findViewById(R.id.btnToggleProduct)

        fun bind(item: Product) {
            tvName.text = item.name
            tvCode.text = "Codigo: ${item.code}"
            tvSubcategory.text = "Subcategoria: ${item.subcategoryName ?: "Subcategoria no disponible"}"
            tvPrice.text = "Precio: ${item.unitPrice}"
            tvStatus.text = if (item.status) "ACTIVO" else "INACTIVO"
            btnToggle.text = if (item.status) "Desactivar" else "Activar"

            btnEdit.setOnClickListener { onEdit(item) }
            btnToggle.setOnClickListener { onToggle(item) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean = oldItem == newItem
    }
}
