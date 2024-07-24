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
    private var currentSearchQuery: String? = null
    private var searchResults = mutableListOf<Character>()

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
        if (isLoading.value == true || isLastPage || page <= currentPage) return

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val fetchedCharacters = if (currentSearchQuery != null) {
                    // Fetch next page of search results
                    val response = repository.searchCharacters(currentSearchQuery!!)
                    searchResults.addAll(response.results)
                    response.results
                } else {
                    // Fetch regular characters
                    repository.getCharacters(page)
                }

                // Combine current episodes with new fetched episodes and remove duplicates
                val currentCharacters = _characters.value.orEmpty().toMutableList()
                currentCharacters.addAll(fetchedCharacters)
                _characters.value = currentCharacters.toList()

                currentPage = page
                isLastPage = fetchedCharacters.isEmpty() // Update isLastPage correctly

            } catch (e: Exception) {
                _errorMessage.value = "Error fetching characters: ${e.message}"
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
    fun searchCharacters(query: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                if (query.isBlank()) {
                    // Clear search query and show all characters
                    currentSearchQuery = null
                    searchResults.clear()
                    currentPage = 1
                    isLastPage = false
                    fetchCharacters(1) // Fetch first page of all characters
                } else {
                    currentSearchQuery = query
                    currentPage = 1
                    isLastPage= false
                    searchResults.clear()
                    val response = repository.searchCharacters(query)
                    _characters.value = response.results
                    searchResults.addAll(response.results)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error fetching characters: ${e.message}"
                Log.e("CharacterViewModel", "Error fetching characters", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}