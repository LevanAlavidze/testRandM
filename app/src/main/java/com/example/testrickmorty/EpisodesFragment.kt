package com.example.testrickmorty

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testrickmorty.databinding.FragmentEpisodesBinding

class EpisodesFragment : Fragment(R.layout.fragment_episodes) {
    private val viewModel: EpisodeViewModel by viewModels {
        EpisodeViewModelFactory((requireActivity().application as MyApplication).repository)
    }
    private lateinit var binding: FragmentEpisodesBinding
    private lateinit var adapter: EpisodeAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentEpisodesBinding.bind(view)

        adapter = EpisodeAdapter(
            onItemClick = { episodeId -> // Handle item click
                val bundle = Bundle().apply {
                    putInt("episodeId", episodeId)
                }
                findNavController().navigate(R.id.episodeDetailFragment, bundle)
            }
        )
        binding.episodeRecyclerView.layoutManager = GridLayoutManager(context, 2)
        binding.episodeRecyclerView.adapter = adapter

        // Add the scroll listener here
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
            Log.d("EpisodesFragment", "Updating episodes list with ${episodes.size} items")
            episodes.forEach { episode ->
                Log.d("EpisodesFragment", "Episode: ${episode.name} (${episode.episode})")
            }
            adapter.submitList(episodes)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.swipeRefreshLayout.isRefreshing = isLoading
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.fetchEpisodes(1)
        }

        viewModel.fetchEpisodes(1)
    }
}