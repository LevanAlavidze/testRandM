package com.example.testrickmorty.feature.episode_details.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.testrickmorty.R
import com.example.testrickmorty.data.Repository
import com.example.testrickmorty.databinding.FragmentEpisodeDetailsBinding
import com.example.testrickmorty.feature.characters.adapter.CharacterAdapter
import com.example.testrickmorty.feature.episode_details.vm.EpisodeDetailsViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class EpisodeDetailsFragment : Fragment(R.layout.fragment_episode_details) {
    private lateinit var binding: FragmentEpisodeDetailsBinding

    @Inject
    lateinit var repository: Repository

    private val viewModel: EpisodeDetailsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentEpisodeDetailsBinding.bind(view)

        val characterAdapter = CharacterAdapter { characterId ->
            val bundle = Bundle().apply {
                putInt("characterId", characterId)
            }
            findNavController().navigate(R.id.characterDetailFragment, bundle)
        }

        binding.characterRecyclerView.layoutManager = GridLayoutManager(context, 2)
        binding.characterRecyclerView.adapter = characterAdapter

        // Retrieve episodeId from arguments
        val episodeId = requireArguments().getInt("episodeId")
        viewModel.setEpisodeId(episodeId)

        viewModel.episode.observe(viewLifecycleOwner) { episode ->
            episode?.let {
                binding.episodeName.text = it.name
                binding.episodeAirDate.text = it.airDate

            }
        }

        viewModel.episodeCharacters.observe(viewLifecycleOwner) { characters ->
            characters?.let {
                characterAdapter.submitList(it)
            }
        }
        binding.episodeName.setOnClickListener {
            val episodeId = viewModel.episode.value?.id ?: return@setOnClickListener
            val bundle = Bundle().apply {
                putInt("episodeId", episodeId)
            }
            findNavController().navigate(R.id.episodeDetailFragment, bundle)
        }
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}