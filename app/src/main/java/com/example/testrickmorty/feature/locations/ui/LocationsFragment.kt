package com.example.testrickmorty.feature.locations.ui

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testrickmorty.feature.locations.vm.LocationsViewModel
import com.example.testrickmorty.MyApplication
import com.example.testrickmorty.R
import com.example.testrickmorty.databinding.FragmentLocationsBinding
import com.example.testrickmorty.feature.locations.adapter.LocationsAdapter
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class LocationsFragment : Fragment(R.layout.fragment_locations) {

    private val viewModel: LocationsViewModel by viewModels()
    private lateinit var binding: FragmentLocationsBinding
    private lateinit var adapter: LocationsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLocationsBinding.bind(view)
        setupUI()
        observeViewModel()
        viewModel.fetchLocations(1)
    }

    private fun setupUI() {
        adapter = LocationsAdapter { locationId ->
            val bundle = Bundle().apply {
                putInt("locationId", locationId)
            }
            findNavController().navigate(R.id.locationDetailFragment, bundle)
        }
        binding.locationRecyclerView.layoutManager = GridLayoutManager(context, 2)
        binding.locationRecyclerView.adapter = adapter

        binding.btnFilter.setOnClickListener {
            showFilterDialog()
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.fetchLocations(1)
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    viewModel.searchLocations(it)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrBlank()) {
                    viewModel.fetchLocations(1)
                } else {
                    viewModel.searchLocations(newText)
                }
                return true
            }
        })
    }

    private fun observeViewModel() {
        viewModel.locations.observe(viewLifecycleOwner) { locations ->
            adapter.submitList(locations)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.swipeRefreshLayout.isRefreshing = isLoading
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }

        viewModel.noResults.observe(viewLifecycleOwner) { noResults ->
            if (noResults) {
                Toast.makeText(requireContext(), "No Location found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showFilterDialog() {
        val filterFragment = LocationFilterFragment()
        filterFragment.setOnFilterAppliedListener { filters ->
            val name = filters["name"] ?: ""
            val type = filters["type"] ?: ""
            val dimension = filters["dimension"] ?: ""
            viewModel.fetchFilteredLocations(name, type, dimension)
        }
        filterFragment.show(parentFragmentManager, "FilterDialog")
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        viewModel.locations.value?.let { adapter.submitList(it.toList()) }
    }
}
