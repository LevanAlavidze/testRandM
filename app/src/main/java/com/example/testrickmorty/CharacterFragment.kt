package com.example.testrickmorty

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
import com.example.testrickmorty.databinding.FragmentCharacterBinding

class CharacterFragment : Fragment(R.layout.fragment_character) {
    private val viewModel: CharacterViewModel by viewModels {
        CharacterViewModelFactory((requireActivity().application as MyApplication).repository)
    }
    private lateinit var binding: FragmentCharacterBinding
    private lateinit var adapter: CharacterAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("CharacterFragment", "Fragment view created")

        binding = FragmentCharacterBinding.bind(view)

        //modified
        adapter = CharacterAdapter(
            onItemClick = { characterId ->
                val bundle = Bundle().apply {
                    putInt("characterId", characterId)
                }
                findNavController().navigate(R.id.characterDetailFragment, bundle)

            }
        )


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
            Log.d("CharacterFragment", "Updating characters list with ${characters.size} items")
            characters.forEach { character ->
                Log.d("CharacterFragment", "Character: ${character.name}, ${character.gender}, ${character.species}")
            }
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

        binding.swipeRefreshLayout.setOnRefreshListener {
            Log.d("CharacterFragment", "Refreshing characters")
            viewModel.fetchCharacters(1)
        }

        // Initial fetch
        viewModel.fetchCharacters(1)

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    viewModel.searchCharacters(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrBlank()) {
                    // Clear the search and show all characters
                    viewModel.searchCharacters("")
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