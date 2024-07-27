package com.example.testrickmorty.feature.locations.ui

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
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
import com.example.testrickmorty.feature.locations.di.LocationsViewModelFactory

class LocationsFragment : Fragment(R.layout.fragment_locations) {
    private val viewModel: LocationsViewModel by viewModels {
        LocationsViewModelFactory((requireActivity().application as MyApplication).repository)
    }
    private lateinit var binding: FragmentLocationsBinding
    private lateinit var adapter: LocationsAdapter


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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("LocationsFragment", "Fragment View Created")
        binding = FragmentLocationsBinding.bind(view)

        adapter = LocationsAdapter {locationId ->
            val bundle = Bundle().apply {
                putInt("locationId", locationId)
                }
                findNavController().navigate(R.id.locationDetailFragment, bundle)
            }
        binding.btnFilter.setOnClickListener {
            showFilterDialog()
        }


        binding.locationRecyclerView.layoutManager = GridLayoutManager(context, 2)
        binding.locationRecyclerView.adapter = adapter

        binding.locationRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as GridLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                if (lastVisibleItem >= totalItemCount - 1) {
                    viewModel.fetchNextPage()
                }
            }
        })

        viewModel.locations.observe(viewLifecycleOwner) { locations ->
            Log.d("LocationsFragment", "Updating Locations List with ${locations.size} items")
            adapter.submitList(locations)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->

            Log.d("LocationsFragment", "Loading State: $isLoading")
            binding.swipeRefreshLayout.isRefreshing = isLoading
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            Log.d("LocationsFragment", "Error Message: $message")
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
        viewModel.noResults.observe(viewLifecycleOwner) { noResults ->
            if (noResults) {
                // Show a message or UI indicating no results
                Toast.makeText(requireContext(), "No Location found", Toast.LENGTH_SHORT).show()
            }
        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            Log.d("LocationsFragment", "Swipe Refresh Triggered")
            viewModel.fetchLocations(1)
        }
        viewModel.fetchLocations(1)

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    viewModel.searchLocations(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrBlank()) {
                    viewModel.searchLocations("")
                    viewModel.fetchLocations(1)
                }
                return true
            }
        })

        Log.d("LocationsFragment", "Initial Fetch Triggered")
        viewModel.fetchLocations(1)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        viewModel.locations.value?.let { adapter.submitList(it.toList()) }
    }
}