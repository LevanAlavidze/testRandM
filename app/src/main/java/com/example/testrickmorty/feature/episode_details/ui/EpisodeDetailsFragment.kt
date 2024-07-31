package com.example.testrickmorty.feature.episode_details.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.testrickmorty.R
import com.example.testrickmorty.data.Repository
import com.example.testrickmorty.databinding.FragmentEpisodeDetailsBinding
import com.example.testrickmorty.feature.characters.adapter.CharacterAdapter
import com.example.testrickmorty.feature.characters.models.Character
import com.example.testrickmorty.feature.episode_details.vm.EpisodeDetailsViewModel
import com.example.testrickmorty.feature.episodes.models.Episode
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
        setupRecyclerView()
        observeViewModel()

        val episodeId = requireArguments().getInt("episodeId")
        viewModel.setEpisodeId(episodeId)
    }

    private fun setupRecyclerView() {
        val characterAdapter = CharacterAdapter { characterId ->
            navigateToCharacterDetails(characterId)
        }
        binding.characterRecyclerView.layoutManager = GridLayoutManager(context, 2)
        binding.characterRecyclerView.adapter = characterAdapter
    }

    private fun navigateToCharacterDetails(characterId: Int) {
        val bundle = Bundle().apply {
            putInt("characterId", characterId)
        }
        findNavController().navigate(R.id.characterDetailFragment, bundle)
    }

    private fun observeViewModel() {
        viewModel.episode.observe(viewLifecycleOwner) { episode ->
            episode?.let { updateEpisodeDetails(it) }
        }

        viewModel.episodeCharacters.observe(viewLifecycleOwner) { characters ->
            characters?.let { updateCharacterList(it) }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let { showError(message) }
        }
    }

    private fun updateEpisodeDetails(episode: Episode) {
        binding.episodeName.text = episode.name
        binding.episodeAirDate.text = episode.airDate
    }

    private fun updateCharacterList(characters: List<Character>) {
        (binding.characterRecyclerView.adapter as CharacterAdapter).submitList(characters)
    }

    private fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
