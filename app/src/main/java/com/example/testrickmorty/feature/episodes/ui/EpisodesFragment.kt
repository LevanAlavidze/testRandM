package com.example.testrickmorty.feature.episodes.ui

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testrickmorty.feature.episodes.vm.EpisodeViewModel
import com.example.testrickmorty.MyApplication
import com.example.testrickmorty.R
import com.example.testrickmorty.databinding.FragmentEpisodesBinding
import com.example.testrickmorty.feature.episodes.adapter.EpisodeAdapter
import com.example.testrickmorty.feature.episodes.di.EpisodeViewModelFactory

class EpisodesFragment : Fragment(R.layout.fragment_episodes) {
    private val viewModel: EpisodeViewModel by viewModels {
        EpisodeViewModelFactory((requireActivity().application as MyApplication).repository)
    }
    private lateinit var binding: FragmentEpisodesBinding
    private lateinit var adapter: EpisodeAdapter


    private fun showFilterDialog() {
        val filterFragment = EpisodeFilterFragment()
        filterFragment.setOnFilterAppliedListener { filters ->
            val name = filters["name"] ?: ""
            val episode = filters["episode"] ?: ""
            viewModel.fetchFilteredEpisodes(name, episode)
        }
        filterFragment.show(parentFragmentManager, "FilterDialog")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentEpisodesBinding.bind(view)

        adapter = EpisodeAdapter { episodeId ->
                val bundle = Bundle().apply {
                    putInt("episodeId", episodeId)
                }
                findNavController().navigate(R.id.episodeDetailFragment, bundle)
            }
            binding.btnFilter.setOnClickListener {
                showFilterDialog()
            }
            binding.episodeRecyclerView.layoutManager = GridLayoutManager(context, 2)
            binding.episodeRecyclerView.adapter = adapter

            binding.episodeRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
            viewModel.episodes.observe(viewLifecycleOwner) { episodes ->
                adapter.submitList(episodes)
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
                // Show a message or UI indicating no results
                Toast.makeText(requireContext(), "No episodes found", Toast.LENGTH_SHORT).show()
            }
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.fetchEpisodes(1)
        }
        viewModel.fetchEpisodes(1)

        // Search functionality
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    viewModel.searchEpisodes(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrBlank()) {
                    // Clear the search and show all episodes
                    viewModel.searchEpisodes("")
                    viewModel.fetchEpisodes(1)
                }
                return true
            }
        })
    }
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        viewModel.episodes.value?.let { adapter.submitList(it.toList()) }
    }
}