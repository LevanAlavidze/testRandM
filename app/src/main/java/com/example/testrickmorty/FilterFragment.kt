package com.example.testrickmorty

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.testrickmorty.databinding.FragmentFilterBinding

class FilterFragment : DialogFragment() {

    private lateinit var binding: FragmentFilterBinding
    private var onFilterAppliedListener: ((Map<String, String>) -> Unit)? = null

    fun setOnFilterAppliedListener(listener: (Map<String, String>) -> Unit) {
        onFilterAppliedListener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFilterBinding.inflate(inflater, container, false)

        binding.btnApplyFilters.setOnClickListener {
            applyFilters()
        }

        return binding.root
    }

    private fun applyFilters() {
        val filters = mapOf(
            "status" to binding.spinnerStatus.selectedItem.toString(),
            "gender" to binding.spinnerGender.selectedItem.toString(),
            "species" to binding.spinnerSpecies.selectedItem.toString(),
            "name" to ((activity as? MainActivity)?.currentSearchQuery ?: "")
        )
        onFilterAppliedListener?.invoke(filters)
        dismiss()
    }
}
