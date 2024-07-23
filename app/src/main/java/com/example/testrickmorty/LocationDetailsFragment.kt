package com.example.testrickmorty

import android.os.Bundle
import android.util.Log
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
            },
            onLoadMore = {
                // Notify the adapter that more items are being loaded
                characterAdapter.setLoadingState(true)
                viewModel.loadMoreCharacters()
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
            // Notify the adapter that loading is complete
            characterAdapter.setLoadingState(false)
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
