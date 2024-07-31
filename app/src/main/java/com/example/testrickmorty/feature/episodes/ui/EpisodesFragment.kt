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
import com.example.testrickmorty.R
import com.example.testrickmorty.databinding.FragmentEpisodesBinding
import com.example.testrickmorty.feature.episodes.adapter.EpisodeAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EpisodesFragment : Fragment(R.layout.fragment_episodes) {

    private val viewModel: EpisodeViewModel by viewModels()
    private lateinit var binding: FragmentEpisodesBinding
    private lateinit var adapter: EpisodeAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentEpisodesBinding.bind(view)

        setupRecyclerView()
        setupListeners()
        observeViewModel()
        viewModel.fetchEpisodes(1)
    }

    private fun setupRecyclerView() {
        adapter = EpisodeAdapter { episodeId ->
            val bundle = Bundle().apply { putInt("episodeId", episodeId) }
            findNavController().navigate(R.id.episodeDetailFragment, bundle)
        }
        binding.episodeRecyclerView.layoutManager = GridLayoutManager(context, 2)
        binding.episodeRecyclerView.adapter = adapter
        binding.episodeRecyclerView.addOnScrollListener(createScrollListener())
    }

    private fun createScrollListener() = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val layoutManager = recyclerView.layoutManager as GridLayoutManager
            val totalItemCount = layoutManager.itemCount
            val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
            if (lastVisibleItem >= totalItemCount - 1) {
                viewModel.fetchNextPage()
            }
        }
    }

    private fun setupListeners() {
        binding.btnFilter.setOnClickListener { showFilterDialog() }
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.fetchEpisodes(1)
        }
        binding.searchView.setOnQueryTextListener(createSearchListener())
    }

    private fun createSearchListener() = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            query?.let { viewModel.searchEpisodes(it) }
            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            if (newText.isNullOrBlank()) {
                viewModel.fetchEpisodes(1)
            } else {
                viewModel.searchEpisodes(newText)
            }
            return true
        }
    }

    private fun showFilterDialog() {
        val filterFragment = EpisodeFilterFragment()
        filterFragment.setOnFilterAppliedListener { filters ->
            val name = filters["name"] ?: ""
            val episode = filters["episode"] ?: ""
            viewModel.fetchFilteredEpisodes(name, episode)
        }
        filterFragment.show(parentFragmentManager, "FilterDialog")
    }

    private fun observeViewModel() {
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
                Toast.makeText(requireContext(), "No episodes found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        viewModel.episodes.value?.let { adapter.submitList(it.toList()) }
    }
}
