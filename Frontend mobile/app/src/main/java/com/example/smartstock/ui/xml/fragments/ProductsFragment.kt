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
import com.example.smartstock.domain.model.CreateProductRequest
import com.example.smartstock.domain.model.Product
import com.example.smartstock.domain.model.SubcategoryOption
import com.example.smartstock.domain.model.UpdateProductRequest
import com.example.smartstock.ui.screens.products.ProductsViewModel
import com.example.smartstock.ui.xml.adapters.ProductsAdapter
import com.example.smartstock.ui.xml.dropdown.DropdownOption
import com.example.smartstock.ui.xml.dropdown.bindSearchableDropdown
import com.example.smartstock.ui.xml.toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProductsFragment : Fragment(R.layout.fragment_products) {
    private val viewModel: ProductsViewModel by viewModels()

    private lateinit var adapter: ProductsAdapter
    private var subcategories: List<SubcategoryOption> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv: RecyclerView = view.findViewById(R.id.rvProducts)
        val btnCreate: Button = view.findViewById(R.id.btnCreateProduct)
        val btnRefresh: Button = view.findViewById(R.id.btnRefreshProducts)
        val progress: ProgressBar = view.findViewById(R.id.progressProducts)
        val tvError: TextView = view.findViewById(R.id.tvProductsError)

        adapter =
            ProductsAdapter(
                onEdit = { product -> showProductDialog(product) },
                onToggle = { product -> viewModel.toggleProductStatus(product.id, product.status) },
            )

        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        btnCreate.setOnClickListener { showProductDialog(null) }
        btnRefresh.setOnClickListener { viewModel.loadProducts() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    progress.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                    subcategories = state.subcategories
                    adapter.submitList(state.products)

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

    private fun showProductDialog(productToEdit: Product?) {
        if (subcategories.isEmpty()) {
            requireContext().toast("No hay subcategorias disponibles")
            return
        }

        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_product_form, null)
        val actvSubcategory: AppCompatAutoCompleteTextView = dialogView.findViewById(R.id.spSubcategory)
        val etCode: EditText = dialogView.findViewById(R.id.etCode)
        val etName: EditText = dialogView.findViewById(R.id.etName)
        val etDescription: EditText = dialogView.findViewById(R.id.etDescription)
        val etPrice: EditText = dialogView.findViewById(R.id.etPrice)

        val subcategoryOptions = subcategories.map { subcategory -> DropdownOption(subcategory.id, subcategory.name) }
        val initialSubcategory =
            subcategoryOptions.firstOrNull { option -> option.value == productToEdit?.subcategoryId }
                ?: subcategoryOptions.firstOrNull()
        var selectedSubcategory = initialSubcategory

        actvSubcategory.bindSearchableDropdown(subcategoryOptions, initialSubcategory) { option ->
            selectedSubcategory = option
        }

        if (productToEdit != null) {
            etCode.setText(productToEdit.code)
            etName.setText(productToEdit.name)
            etDescription.setText(productToEdit.description.orEmpty())
            etPrice.setText(productToEdit.unitPrice.toString())
        }

        AlertDialog
            .Builder(requireContext())
            .setTitle(if (productToEdit == null) "Crear producto" else "Editar producto")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val subcategoryId = selectedSubcategory?.value
                val code = etCode.text.toString().trim()
                val name = etName.text.toString().trim()
                val description = etDescription.text.toString().trim().ifBlank { null }
                val price = etPrice.text.toString().trim().toDoubleOrNull()

                if (subcategoryId == null || code.isBlank() || name.isBlank() || price == null) {
                    requireContext().toast("Completa codigo, nombre, precio y subcategoria")
                    return@setPositiveButton
                }

                if (productToEdit == null) {
                    viewModel.createProduct(
                        CreateProductRequest(
                            subcategoryId = subcategoryId,
                            code = code,
                            name = name,
                            description = description,
                            unitPrice = price,
                            status = true,
                        ),
                    )
                } else {
                    viewModel.updateProduct(
                        productToEdit.id,
                        UpdateProductRequest(
                            subcategoryId = subcategoryId,
                            code = code,
                            name = name,
                            description = description,
                            unitPrice = price,
                        ),
                    )
                }
            }.setNegativeButton("Cancelar", null)
            .show()
    }
}
