package com.example.testrickmorty

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.testrickmorty.databinding.FragmentLocationDetailsBinding

class LocationDetailsFragment : Fragment(R.layout.fragment_location_details) {
    private lateinit var binding: FragmentLocationDetailsBinding
    private val viewModel: LocationDetailsViewModel by viewModels {
        LocationDetailsViewModelFactory(
            (requireActivity().application as MyApplication).repository,
            requireArguments().getInt("locationId")
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentLocationDetailsBinding.bind(view)

        val characterAdapter = CharacterAdapter(
            onItemClick = { characterId ->
                val bundle = Bundle().apply {
                    putInt("characterId", characterId)
                }
                findNavController().navigate(R.id.characterDetailFragment, bundle)
            },
            onLoadMore = { /* Handle load more if needed */ }
        )

        binding.characterRecyclerView.layoutManager = GridLayoutManager(context, 2)
        binding.characterRecyclerView.adapter = characterAdapter

        viewModel.location.observe(viewLifecycleOwner) { location ->
            location?.let {
                binding.locationName.text = it.name
                binding.locationType.text = it.type
                binding.locationDimension.text = it.dimension

                viewModel.fetchResidentCharacters(it.residents)


                // Fetch character details for residents
                /*viewModel.fetchResidentCharacters(it.residents)*/
            }
        }

        viewModel.residentCharacters.observe(viewLifecycleOwner) { characters ->
            characterAdapter.submitList(characters)
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}