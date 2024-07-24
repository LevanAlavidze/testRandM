package com.example.testrickmorty

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.testrickmorty.databinding.FragmentEpisodeDetailsBinding

class EpisodeDetailsFragment : Fragment(R.layout.fragment_episode_details) {

    private var _binding: FragmentEpisodeDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EpisodeDetailsViewModel by viewModels {
        EpisodeDetailsViewModelFactory(
            (requireActivity().application as MyApplication).repository,
            requireArguments().getInt("episodeId")
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEpisodeDetailsBinding.bind(view)

        val characterAdapter = CharacterAdapter(
            onItemClick = { characterId ->
                val bundle = Bundle().apply {
                    putInt("characterId", characterId)
                }
                findNavController().navigate(R.id.characterDetailFragment, bundle)
            }
        )

        binding.characterRecyclerView.layoutManager = GridLayoutManager(context, 2)
        binding.characterRecyclerView.adapter = characterAdapter

        viewModel.episode.observe(viewLifecycleOwner) { episode ->
            episode?.let {
                binding.episode = it // Bind episode to the layout
                Log.d("EpisodeDetailsFragment", "Episode: $it")
                viewModel.fetchEpisodeCharacters(it.characters) // Fetch characters in the episode
            }
        }

        viewModel.episodeCharacters.observe(viewLifecycleOwner) { characters ->
            Log.d("EpisodeDetailsFragment", "Characters fetched: ${characters.size}")
            characterAdapter.submitList(characters)
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}