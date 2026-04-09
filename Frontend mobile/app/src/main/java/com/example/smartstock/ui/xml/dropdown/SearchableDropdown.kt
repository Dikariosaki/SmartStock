package com.example.smartstock.ui.xml.dropdown

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import com.example.smartstock.ui.xml.afterTextChanged
import com.example.smartstock.ui.xml.normalizeForSearch

data class DropdownOption<T>(
    val value: T,
    val label: String,
) {
    override fun toString(): String = label
}

class SearchableDropdownAdapter<T>(
    context: Context,
    private val allOptions: List<DropdownOption<T>>,
) : ArrayAdapter<DropdownOption<T>>(context, android.R.layout.simple_dropdown_item_1line, allOptions.toMutableList()), Filterable {
    override fun getFilter(): Filter =
        object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString().orEmpty().normalizeForSearch()
                val filteredOptions =
                    if (query.isBlank()) {
                        allOptions
                    } else {
                        allOptions.filter { option ->
                            option.label.normalizeForSearch().contains(query)
                        }
                    }

                return FilterResults().apply {
                    values = filteredOptions
                    count = filteredOptions.size
                }
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(
                constraint: CharSequence?,
                results: FilterResults?,
            ) {
                clear()
                addAll(results?.values as? List<DropdownOption<T>> ?: allOptions)
                notifyDataSetChanged()
            }

            override fun convertResultToString(resultValue: Any?): CharSequence =
                (resultValue as? DropdownOption<*>)?.label.orEmpty()
        }
}

fun <T> AppCompatAutoCompleteTextView.bindSearchableDropdown(
    options: List<DropdownOption<T>>,
    initialSelection: DropdownOption<T>? = null,
    onSelectionChanged: (DropdownOption<T>?) -> Unit,
) {
    val searchableAdapter = SearchableDropdownAdapter(context, options)
    var selectedOption = initialSelection

    threshold = 0
    setAdapter(searchableAdapter)
    setText(initialSelection?.label.orEmpty(), false)
    onSelectionChanged(initialSelection)

    setOnItemClickListener { _, _, position, _ ->
        selectedOption = searchableAdapter.getItem(position)
        setText(selectedOption?.label.orEmpty(), false)
        setSelection(text?.length ?: 0)
        onSelectionChanged(selectedOption)
    }

    setOnClickListener { post { showDropDown() } }
    setOnFocusChangeListener { _, hasFocus ->
        if (hasFocus) {
            post { showDropDown() }
        }
    }

    afterTextChanged { currentText ->
        val matchesCurrentSelection =
            selectedOption?.label?.normalizeForSearch() == currentText.normalizeForSearch()
        if (!matchesCurrentSelection) {
            selectedOption = null
            onSelectionChanged(null)
        }

        if (hasFocus()) {
            post { showDropDown() }
        }
    }
}
