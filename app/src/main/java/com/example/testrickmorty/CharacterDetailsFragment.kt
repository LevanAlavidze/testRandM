package com.example.testrickmorty

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.testrickmorty.databinding.FragmentCharacterDetailsBinding

class CharacterDetailsFragment : Fragment(R.layout.fragment_character_details) {
    private lateinit var binding: FragmentCharacterDetailsBinding
    private val viewModel: CharacterDetailsViewModel by viewModels {
        CharacterDetailsViewModelFactory(
            (requireActivity().application as MyApplication).repository,
            requireArguments().getInt("characterId")
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentCharacterDetailsBinding.bind(view)

        val episodeAdapter = EpisodeAdapter { episodeId ->
            val bundle = Bundle().apply {
                putInt("episodeId", episodeId)
            }
            findNavController().navigate(R.id.episodeDetailFragment, bundle)
        }

        binding.episodeRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.episodeRecyclerView.adapter = episodeAdapter

        viewModel.character.observe(viewLifecycleOwner) { character ->
            character?.let {
                binding.characterName.text = it.name
                binding.characterSpecies.text = it.species
                binding.characterStatus.text = it.status
                binding.characterGender.text = it.gender
                binding.characterLocation.text = it.location.name
                binding.characterOrigin.text = it.origin.name

                Glide.with(binding.characterImage.context)
                    .load(it.image)
                    .into(binding.characterImage)
            }
        }

        viewModel.episodes.observe(viewLifecycleOwner) { episodes ->
            episodes?.let {
                episodeAdapter.submitList(it)
            }
        }

        binding.characterLocation.setOnClickListener {
            val locationId = viewModel.character.value?.getOriginId() ?: return@setOnClickListener
            val bundle = Bundle().apply {
                putInt("locationId", locationId)
            }
            findNavController().navigate(R.id.locationDetailFragment, bundle)
        }

        binding.characterOrigin.setOnClickListener {
            val originId = viewModel.character.value?.getOriginId() ?: return@setOnClickListener
            val bundle = Bundle().apply {
                putInt("locationId", originId)
            }
            findNavController().navigate(R.id.locationDetailFragment, bundle)
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}