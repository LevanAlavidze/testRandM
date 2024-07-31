package com.example.testrickmorty.feature.character_details.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.testrickmorty.feature.character_details.vm.CharacterDetailsViewModel
import com.example.testrickmorty.feature.episodes.adapter.EpisodeAdapter
import com.example.testrickmorty.R
import com.example.testrickmorty.data.Repository
import com.example.testrickmorty.databinding.FragmentCharacterDetailsBinding
import com.example.testrickmorty.feature.characters.models.Character
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CharacterDetailsFragment : Fragment(R.layout.fragment_character_details) {

    private lateinit var binding: FragmentCharacterDetailsBinding

    @Inject
    lateinit var repository: Repository

    private val viewModel: CharacterDetailsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCharacterDetailsBinding.bind(view)

        setupRecyclerView()
        setupClickListeners()
        observeViewModel()

        // Retrieve characterId from arguments
        val characterId = requireArguments().getInt("characterId")
        viewModel.setCharacterId(characterId)
    }

    private fun setupRecyclerView() {
        val episodeAdapter = EpisodeAdapter { episodeId ->
            val bundle = Bundle().apply { putInt("episodeId", episodeId) }
            findNavController().navigate(R.id.episodeDetailFragment, bundle)
        }

        binding.episodeRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = episodeAdapter
        }
    }

    private fun setupClickListeners() {
        binding.characterLocation.setOnClickListener {
            navigateToLocationDetail(viewModel.character.value?.getOriginId())
        }

        binding.characterOrigin.setOnClickListener {
            navigateToLocationDetail(viewModel.character.value?.getOriginId())
        }
    }

    private fun navigateToLocationDetail(locationId: Int?) {
        locationId?.let {
            val bundle = Bundle().apply { putInt("locationId", it) }
            findNavController().navigate(R.id.locationDetailFragment, bundle)
        }
    }

    private fun observeViewModel() {
        viewModel.character.observe(viewLifecycleOwner) { character ->
            character?.let { updateCharacterDetails(it) }
        }

        viewModel.episodes.observe(viewLifecycleOwner) { episodes ->
            (binding.episodeRecyclerView.adapter as? EpisodeAdapter)?.submitList(episodes)
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let { showToast(it) }
        }
    }

    private fun updateCharacterDetails(character: Character) {
        with(binding) {
            characterName.text = character.name
            characterSpecies.text = character.species
            characterStatus.text = character.status
            characterGender.text = character.gender
            characterLocation.text = character.location.name
            characterOrigin.text = character.origin.name

            Glide.with(characterImage.context)
                .load(character.image)
                .into(characterImage)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
