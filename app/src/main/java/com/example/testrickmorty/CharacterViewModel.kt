package com.example.testrickmorty

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.HttpException

class CharacterViewModel(private val repository: Repository) : ViewModel() {

    private val _characters = MutableLiveData<List<Character>>()
    val characters: LiveData<List<Character>> get() = _characters

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private var currentPage = 1
/*    private var isRefreshing = false*/
    private var isLastPage = false
    private var currentSearchQuery: String? = null
    private val searchResults = mutableListOf<Character>()
    private val pageLoadingStates = mutableMapOf<Int, Boolean>()

/*    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                Log.d("CharacterViewModel", "Loading initial data")
                val cachedCharacters = repository.getCachedCharacters()
                Log.d("CharacterViewModel", "Cached characters loaded: ${cachedCharacters.size}")
                _characters.value = cachedCharacters
                fetchCharacters(currentPage)
            } catch (e: Exception) {
                _errorMessage.value = "Error loading initial data: ${e.message}"
                Log.e("CharacterViewModel", "Error loading initial data", e)
            }
        }
    }*/

    fun fetchCharacters(page: Int) {
        Log.d("CharacterViewModel", "Fetching characters for page: $page")
        if (_isLoading.value == true || isLastPage || pageLoadingStates[page] == true) {
            return
        }

        pageLoadingStates[page] = true
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val fetchedCharacters = if (currentSearchQuery != null) {
                    val response = repository.searchCharacters(currentSearchQuery!!)
                    response.results
                } else {
                    repository.getCharacters(page)
                }

                val currentCharacters = _characters.value.orEmpty().toMutableList()
                currentCharacters.addAll(fetchedCharacters)
                _characters.value = currentCharacters.distinctBy { it.id }

                currentPage = page
                isLastPage = fetchedCharacters.isEmpty()
            } catch (e: HttpException) {
                if (e.code() == 404) {
                    isLastPage = true
                } else {
                    _errorMessage.value = "Error fetching characters: ${e.message}"
                    Log.e("CharacterViewModel", "Error fetching characters", e)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error fetching characters: ${e.message}"
                Log.e("CharacterViewModel", "Error fetching characters", e)
            } finally {
                _isLoading.value = false
                pageLoadingStates[page] = false
            }
        }
    }

    fun fetchNextPage() {
        Log.d("CharacterViewModel", "Fetching next page")
        if (!isLastPage && pageLoadingStates[currentPage + 1] != true) {
            fetchCharacters(currentPage + 1)
        }
    }


    fun searchCharacters(query: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                if (query.isBlank()) {
                    // Clear search query and show all episodes
                    currentSearchQuery = null
                    searchResults.clear()
                    currentPage = 1
                    isLastPage = false
                    pageLoadingStates.clear()
                    fetchCharacters(1) // Fetch first page of all episodes
                } else {
                    currentSearchQuery = query
                    currentPage = 1
                    isLastPage = false
                    searchResults.clear()
                    pageLoadingStates.clear()
                    val response = repository.searchCharacters(query)
                    _characters.value = response.results
                    searchResults.addAll(response.results)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error fetching character: ${e.message}"
                Log.e("CharacterViewModel", "Error fetching characters", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
  /*  private fun fetchCharacter(page: Int) {
        if (_isLoading.value == true || isLastPage) return
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val newCharacter = if (currentSearchQuery != null) {
                    val response = repository.searchCharacters(currentSearchQuery!!)
                    searchResults.addAll(response.results)
                    response.results
                } else {
                    repository.getCharacters(page)
                }
                val currentList = _characters.value.orEmpty().toMutableList()
                currentList.addAll(newCharacter)
                _characters.value = currentList.distinctBy { it.id }
                currentPage = page
                isLastPage = newCharacter.isEmpty()
                repository.saveCharactersToDatabase(newCharacter)
            }catch (e: Exception) {
                Log.e("CharacterViewModel", "Error fetching characters", e)
                _errorMessage.value = "Error fetching characters: ${e.message}"
            }finally {
                _isLoading.value = false
            }
        }
    }
*/


/*
    fun refreshCharacters() {
        isRefreshing = true
        currentPage = 1
        isLastPage = false
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val newCharacters = repository.getCharacters(currentPage)
                _characters.value = newCharacters
                currentPage++
            } catch (e: Exception) {
                Log.e("CharacterViewModel", "Error refreshing characters", e)
                _errorMessage.value = "Error refreshing characters: ${e.message}"
            } finally {
                isRefreshing = false
                _isLoading.value = false
            }
        }
    }
*/



    fun fetchFilteredCharacters(
        status: String,
        gender: String,
        species: String,
        type: String,
        name: String
    ) {
        Log.d("CharacterViewModel", "Fetching filtered characters with status: $status, gender: $gender, species: $species, type: $type, name: $name")
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val filteredCharacters = repository.getFilteredCharacters(status, gender, species, type, name)
                Log.d("CharacterViewModel", "Filtered characters fetched: ${filteredCharacters.size}")
                _characters.value = filteredCharacters
            } catch (e: Exception) {
                _errorMessage.value = "Error fetching filtered characters: ${e.message}"
                Log.e("CharacterViewModel", "Error fetching filtered characters", e)
            } finally {
                _isLoading.value = false
                Log.d("CharacterViewModel", "Filter fetch complete")
            }
        }
    }
}
