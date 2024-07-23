package com.example.testrickmorty

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
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

        adapter = CharacterAdapter {
            Log.d("CharacterFragment", "Load more characters")
            viewModel.fetchNextPage()
        }
        binding.characterRecyclerView.layoutManager = GridLayoutManager(context, 2)
        binding.characterRecyclerView.adapter = adapter

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
    }
}