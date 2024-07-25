package com.example.testrickmorty

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import com.example.testrickmorty.databinding.FragmentEpisodeFilterBinding
import com.example.testrickmorty.databinding.FragmentFilterBinding

class EpisodeFilterFragment : DialogFragment() {

    private lateinit var binding: FragmentEpisodeFilterBinding
    private var onFilterAppliedListener: ((Map<String, String>) -> Unit)? = null

    fun setOnFilterAppliedListener(listener: (Map<String, String>) -> Unit) {
        onFilterAppliedListener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEpisodeFilterBinding.inflate(inflater, container, false)

        binding.btnApplyFilters.setOnClickListener {
            applyFilters()
        }

        return binding.root
    }

    private fun applyFilters() {
        val filters = mapOf(
            "name" to binding.editTextName.text.toString(),
            "episode" to binding.editTextEpisodeNumber.text.toString()
        )
        onFilterAppliedListener?.invoke(filters)
        dismiss()
    }
}