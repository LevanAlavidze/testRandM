package com.example.testrickmorty

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testrickmorty.databinding.FragmentLocationsBinding

class LocationsFragment : Fragment(R.layout.fragment_locations) {
    private val viewModel: LocationsViewModel by viewModels {
        LocationsViewModelFactory((requireActivity().application as MyApplication).repository)
    }
    private lateinit var binding: FragmentLocationsBinding
    private lateinit var adapter: LocationsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLocationsBinding.bind(view)

        adapter = LocationsAdapter { locationId ->
            val bundle = Bundle().apply {
                putInt("locationId", locationId)
            }
            findNavController().navigate(R.id.locationDetailFragment, bundle)
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
            Log.d("LocationsFragment", "Observed Locations: $locations")
            adapter.submitList(locations)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            Log.d("LocationsFragment", "Loading State: $isLoading")
            binding.swipeRefreshLayout.isRefreshing = isLoading
            // Only disable swipeRefreshLayout when loading is finished
            if (!isLoading) {
                binding.swipeRefreshLayout.isEnabled = true
            }
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            Log.d("LocationsFragment", "Swipe Refresh Triggered")
            viewModel.refreshLocations()
        }

        Log.d("LocationsFragment", "Initial Fetch Triggered")
        viewModel.fetchLocations()
    }
}