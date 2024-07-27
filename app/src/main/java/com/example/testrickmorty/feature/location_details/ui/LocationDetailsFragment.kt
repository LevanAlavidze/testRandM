package com.example.testrickmorty.feature.location_details.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.testrickmorty.feature.location_details.vm.LocationDetailsViewModel
import com.example.testrickmorty.feature.location_details.vm.LocationDetailsViewModelFactory
import com.example.testrickmorty.MyApplication
import com.example.testrickmorty.R
import com.example.testrickmorty.databinding.FragmentLocationDetailsBinding
import com.example.testrickmorty.feature.characters.adapter.CharacterAdapter

class LocationDetailsFragment : Fragment(R.layout.fragment_location_details) {
    private var _binding: FragmentLocationDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LocationDetailsViewModel by viewModels {
        LocationDetailsViewModelFactory(
            (requireActivity().application as MyApplication).repository,
            requireArguments().getInt("locationId")
        )
    }

    private lateinit var characterAdapter: CharacterAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLocationDetailsBinding.bind(view)

        characterAdapter = CharacterAdapter(
            onItemClick = { characterId ->
                val bundle = Bundle().apply {
                    putInt("characterId", characterId)
                }
                findNavController().navigate(R.id.characterDetailFragment, bundle)

            }
        )

        binding.characterRecyclerView.layoutManager = GridLayoutManager(context, 2)
        binding.characterRecyclerView.adapter = characterAdapter

        viewModel.location.observe(viewLifecycleOwner) { location ->
            location?.let {
                binding.location = it // Bind location to the layout
                Log.d("LocationDetailsFragment", "Location: $it")
                viewModel.fetchResidentCharacters(it.residents) // Fetch resident characters
            }
        }

        viewModel.residentCharacters.observe(viewLifecycleOwner) { residents ->
            Log.d("LocationDetailsFragment", "Residents fetched: ${residents.size}")
            characterAdapter.submitList(residents)

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
