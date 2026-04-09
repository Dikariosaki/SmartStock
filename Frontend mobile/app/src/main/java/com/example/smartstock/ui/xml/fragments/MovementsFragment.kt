package com.example.smartstock.ui.xml.fragments

import android.os.Bundle
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
import com.example.smartstock.domain.model.Movement
import com.example.smartstock.ui.screens.movements.MovementsViewModel
import com.example.smartstock.ui.xml.adapters.MovementsAdapter
import com.example.smartstock.ui.xml.afterTextChanged
import com.example.smartstock.ui.xml.dropdown.DropdownOption
import com.example.smartstock.ui.xml.dropdown.bindSearchableDropdown
import com.example.smartstock.ui.xml.normalizeForSearch
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MovementsFragment : Fragment(R.layout.fragment_movements) {
    private val viewModel: MovementsViewModel by viewModels()

    private lateinit var adapter: MovementsAdapter
    private var allMovements: List<Movement> = emptyList()
    private var currentSearchQuery: String = ""
    private var selectedTypeFilter: DropdownOption<String?>? = MOVEMENT_TYPE_OPTIONS.firstOrNull()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv: RecyclerView = view.findViewById(R.id.rvMovements)
        val etSearch: EditText = view.findViewById(R.id.etSearchMovements)
        val actvType: AppCompatAutoCompleteTextView = view.findViewById(R.id.actvMovementTypeFilter)
        val btnRefresh: Button = view.findViewById(R.id.btnRefreshMovements)
        val progress: ProgressBar = view.findViewById(R.id.progressMovements)
        val tvError: TextView = view.findViewById(R.id.tvMovementsError)

        adapter = MovementsAdapter()
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        configureTypeFilterDropdown(actvType)

        etSearch.afterTextChanged { query ->
            currentSearchQuery = query
            applyFilters()
        }

        btnRefresh.setOnClickListener {
            currentSearchQuery = ""
            etSearch.setText("")
            selectedTypeFilter = MOVEMENT_TYPE_OPTIONS.first()
            configureTypeFilterDropdown(actvType)
            viewModel.loadMovements()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    progress.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                    allMovements = state.movements
                    applyFilters()

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

    private fun configureTypeFilterDropdown(dropdown: AppCompatAutoCompleteTextView) {
        val initialOption =
            MOVEMENT_TYPE_OPTIONS.firstOrNull { option -> option.value == selectedTypeFilter?.value }
                ?: MOVEMENT_TYPE_OPTIONS.first()
        selectedTypeFilter = initialOption

        dropdown.bindSearchableDropdown(MOVEMENT_TYPE_OPTIONS, initialOption) { option ->
            selectedTypeFilter = option
            applyFilters()
        }
    }

    private fun applyFilters() {
        val normalizedQuery = currentSearchQuery.normalizeForSearch()
        val normalizedType = selectedTypeFilter?.value?.normalizeForSearch()

        val filteredMovements =
            allMovements.filter { movement ->
                val matchesType = normalizedType.isNullOrBlank() || movement.type.normalizeForSearch() == normalizedType
                val searchableContent =
                    listOfNotNull(
                        movement.productName,
                        movement.inventoryLocation,
                        movement.userName,
                        movement.type,
                        movement.batch,
                    ).joinToString(" ")
                val matchesQuery =
                    normalizedQuery.isBlank() || searchableContent.normalizeForSearch().contains(normalizedQuery)

                matchesType && matchesQuery
            }

        adapter.submitList(filteredMovements)
    }

    companion object {
        private val MOVEMENT_TYPE_OPTIONS: List<DropdownOption<String?>> =
            listOf(
                DropdownOption<String?>(null, "Todos"),
                DropdownOption("ENTRADA", "Entrada"),
                DropdownOption("SALIDA", "Salida"),
                DropdownOption("AVERIA", "Averia"),
            )
    }
}
