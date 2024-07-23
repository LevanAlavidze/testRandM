package com.example.testrickmorty

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class CharacterViewModel(private val repository: Repository) : ViewModel() {

    private val _characters = MutableLiveData<List<Character>>()
    val characters: LiveData<List<Character>> get() = _characters

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private var currentPage = 1
    private var isLastPage = false

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                Log.d("CharacterViewModel", "Loading initial data")
                // Load cached data initially
                val cachedCharacters = repository.getCachedCharacters()
                _characters.value = cachedCharacters
                Log.d("CharacterViewModel", "Initial cached characters: ${cachedCharacters.size} items")

                // Fetch the first page of characters from the network
                fetchCharacters(currentPage)
            } catch (e: Exception) {
                _errorMessage.value = "Error loading initial data: ${e.message}"
                Log.e("CharacterViewModel", "Error loading initial data: ${e.message}")
            }
        }
    }

    fun fetchCharacters(page: Int) {
        if (isLoading.value == true || isLastPage) return // Prevent multiple requests

        _isLoading.value = true
        viewModelScope.launch {
            try {
                Log.d("CharacterViewModel", "Fetching characters for page: $page")
                val fetchedCharacters = repository.getCharacters(page)
                val currentCharacters = _characters.value.orEmpty()
                val updatedList = currentCharacters.toMutableList().apply {
                    addAll(fetchedCharacters)
                }
                _characters.value = updatedList
                currentPage = page
                isLastPage = fetchedCharacters.isEmpty() // Check if it's the last page
                Log.d("CharacterViewModel", "Fetched characters: ${fetchedCharacters.size} items")

                // Save fetched characters to the database
                repository.saveCharactersToDatabase(fetchedCharacters)
            } catch (e: Exception) {
                _errorMessage.value = "Error fetching characters: ${e.message}"
                Log.e("CharacterViewModel", "Error fetching characters: ${e.message}")
                // Load cached data as fallback
                try {
                    val cachedCharacters = repository.getCachedCharacters()
                    _characters.value = cachedCharacters
                    Log.d("CharacterViewModel", "Loaded cached characters: ${cachedCharacters.size} items")
                } catch (cacheException: Exception) {
                    _errorMessage.value = "Error loading cached characters: ${cacheException.message}"
                    Log.e("CharacterViewModel", "Error loading cached characters: ${cacheException.message}")
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchNextPage() {
        if (!isLastPage) {
            fetchCharacters(currentPage + 1)
        }
    }
}