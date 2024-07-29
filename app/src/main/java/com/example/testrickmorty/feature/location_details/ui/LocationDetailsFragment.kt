package com.example.testrickmorty.feature.location_details.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.testrickmorty.feature.location_details.vm.LocationDetailsViewModel
import com.example.testrickmorty.R
import com.example.testrickmorty.data.Repository
import com.example.testrickmorty.databinding.FragmentLocationDetailsBinding
import com.example.testrickmorty.feature.characters.adapter.CharacterAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LocationDetailsFragment : Fragment(R.layout.fragment_location_details) {
    private lateinit var binding: FragmentLocationDetailsBinding

    @Inject
    lateinit var repository: Repository

    private val viewModel: LocationDetailsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentLocationDetailsBinding.bind(view)

        val characterAdapter = CharacterAdapter { locationId ->
            val bundle = Bundle().apply {
                putInt("locationId", locationId)
            }
            findNavController().navigate(R.id.locationDetailFragment, bundle)
        }

        binding.characterRecyclerView.layoutManager = GridLayoutManager(context, 2)
        binding.characterRecyclerView.adapter = characterAdapter

        // Retrieve locationId from arguments
        val locationId = requireArguments().getInt("locationId")
        viewModel.setLocationId(locationId)

        viewModel.location.observe(viewLifecycleOwner) { location ->
            location?.let {
                binding.locationName.text = it.name
                binding.locationType.text = it.type
                binding.locationDimension.text = it.dimension
            }
        }

        viewModel.residentCharacters.observe(viewLifecycleOwner) { residents ->
            residents?.let {
                characterAdapter.submitList(it)
            }
        }

        binding.locationName.setOnClickListener {
            val locationId = viewModel.location.value?.id ?: return@setOnClickListener
            val bundle = Bundle().apply {
                putInt("locationId", locationId)
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