package com.example.testrickmorty.feature.characters.ui

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testrickmorty.feature.characters.vm.CharacterViewModel
import com.example.testrickmorty.R
import com.example.testrickmorty.databinding.FragmentCharacterBinding
import com.example.testrickmorty.feature.characters.adapter.CharacterAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CharacterFragment : Fragment(R.layout.fragment_character) {
    private val viewModel: CharacterViewModel by viewModels()
    private lateinit var binding: FragmentCharacterBinding
    private lateinit var adapter: CharacterAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCharacterBinding.bind(view)
        setupAdapter()
        setupRecyclerView()
        setupListeners()
        observeViewModel()
        fetchInitialData()
    }

    private fun setupAdapter() {
        adapter = CharacterAdapter { characterId ->
            navigateToCharacterDetails(characterId)
        }
    }

    private fun setupRecyclerView() {
        with(binding.characterRecyclerView) {
            layoutManager = GridLayoutManager(context, 2)
            adapter = this@CharacterFragment.adapter
            addOnScrollListener(createOnScrollListener())
        }
    }

    private fun createOnScrollListener() = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val layoutManager = recyclerView.layoutManager as GridLayoutManager
            val totalItemCount = layoutManager.itemCount
            val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
            if (lastVisibleItem >= totalItemCount - 1) {
                viewModel.fetchNextPage()
            }
        }
    }

    private fun setupListeners() {
        binding.btnFilter.setOnClickListener { showFilterDialog() }
        binding.swipeRefreshLayout.setOnRefreshListener { viewModel.fetchCharacters(1) }
        binding.searchView.setOnQueryTextListener(createQueryTextListener())
    }

    private fun createQueryTextListener() = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            query?.let { viewModel.searchCharacters(it) }
            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            if (newText.isNullOrEmpty()) {
                viewModel.fetchCharacters(1)
            } else {
                viewModel.searchCharacters(newText)
            }
            return true
        }
    }

    private fun showFilterDialog() {
        val filterFragment = FilterFragment().apply {
            setOnFilterAppliedListener { filters ->
                val name = filters["name"] ?: ""
                val status = filters["status"] ?: ""
                val gender = filters["gender"] ?: ""
                val species = filters["species"] ?: ""
                viewModel.fetchFilteredCharacters(name, status, gender, species)
            }
        }
        filterFragment.show(parentFragmentManager, "FilterDialog")
    }

    private fun navigateToCharacterDetails(characterId: Int) {
        val bundle = Bundle().apply { putInt("characterId", characterId) }
        findNavController().navigate(R.id.characterDetailFragment, bundle)
    }

    private fun observeViewModel() {
        viewModel.characters.observe(viewLifecycleOwner) { characters ->
            adapter.submitList(characters)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.swipeRefreshLayout.isRefreshing = isLoading
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }

        viewModel.noResults.observe(viewLifecycleOwner) { noResults ->
            if (noResults) {
                Toast.makeText(requireContext(), "No characters found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchInitialData() {
        viewModel.fetchCharacters(1)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        viewModel.characters.value?.let { adapter.submitList(it.toList()) }
    }
}
