package com.example.testrickmorty.feature.locations.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.testrickmorty.databinding.FragmentLocationFilterBinding

class LocationFilterFragment : DialogFragment() {

    private lateinit var binding: FragmentLocationFilterBinding
    private var onFilterAppliedListener: ((Map<String, String>) -> Unit)? = null

    fun setOnFilterAppliedListener(listener: (Map<String, String>) -> Unit) {
        onFilterAppliedListener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLocationFilterBinding.inflate(inflater, container, false)

        binding.btnApplyFilter.setOnClickListener {
            applyFilters()
        }
        return binding.root
    }

    private fun applyFilters() {
        val filters = mapOf(
            "name" to binding.etName.text.toString(),
            "type" to binding.etType.text.toString(),
            "dimension" to binding.etDimension.text.toString()
        )
        onFilterAppliedListener?.invoke(filters)
            dismiss()
    }
}
