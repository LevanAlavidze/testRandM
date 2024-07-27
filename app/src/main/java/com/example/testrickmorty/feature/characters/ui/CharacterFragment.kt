package com.example.testrickmorty.feature.characters.ui

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testrickmorty.feature.characters.vm.CharacterViewModel
import com.example.testrickmorty.feature.characters.di.CharacterViewModelFactory
import com.example.testrickmorty.MyApplication
import com.example.testrickmorty.R
import com.example.testrickmorty.databinding.FragmentCharacterBinding
import com.example.testrickmorty.feature.characters.adapter.CharacterAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CharacterFragment : Fragment(R.layout.fragment_character) {
    private val viewModel: CharacterViewModel by viewModels {
        CharacterViewModelFactory((requireActivity().application as MyApplication).repository)
    }
    private lateinit var binding: FragmentCharacterBinding
    private lateinit var adapter: CharacterAdapter

    private fun showFilterDialog() {
        val filterFragment = FilterFragment()
        filterFragment.setOnFilterAppliedListener { filters ->
            val name = filters["name"] ?: ""
            val status = filters["status"] ?: ""
            val gender = filters["gender"] ?: ""
            val species = filters["species"] ?: ""


            viewModel.fetchFilteredCharacters(name, status, gender, species)
        }
        filterFragment.show(parentFragmentManager, "FilterDialog")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("CharacterFragment", "Fragment view created")
        binding = FragmentCharacterBinding.bind(view)

        adapter = CharacterAdapter{ characterId ->
            val bundle = Bundle().apply {
                putInt("characterId", characterId)
            }
            findNavController().navigate(R.id.characterDetailFragment, bundle)
        }

        binding.btnFilter.setOnClickListener {
            showFilterDialog()
        }

        binding.characterRecyclerView.layoutManager = GridLayoutManager(context, 2)
        binding.characterRecyclerView.adapter = adapter

        binding.characterRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
        viewModel.characters.observe(viewLifecycleOwner) { characters ->
            adapter.submitList(characters)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            Log.d("CharacterFragment", "Loading state: $isLoading")
            binding.swipeRefreshLayout.isRefreshing = isLoading
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE

        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            Log.d("CharacterFragment", "Error message: $message")
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
        viewModel.noResults.observe(viewLifecycleOwner) { noResults ->
            if (noResults) {
                // Show a message or UI indicating no results
                Toast.makeText(requireContext(), "No characters found", Toast.LENGTH_SHORT).show()
            }
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            Log.d("CharacterFragment", "Refreshing characters")
            viewModel.fetchCharacters(1)
        }
        viewModel.fetchCharacters(1)

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    viewModel.searchCharacters(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    if (newText != null) {
                        viewModel.searchCharacters(newText)
                    }
                    viewModel.fetchCharacters(1)

                }
                return true
            }
        })
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        viewModel.characters.value?.let { adapter.submitList(it.toList()) }
    }
}