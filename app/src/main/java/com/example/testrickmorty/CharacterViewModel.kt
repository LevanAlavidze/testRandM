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

    private val _noResults = MutableLiveData<Boolean>()
    val noResults: LiveData<Boolean> get() = _noResults

    private var currentPage = 1
    private var isLastPage = false
    private var currentSearchQuery: String? = null
    private val pageLoadingStates = mutableMapOf<Int, Boolean>()
    private var isFiltering = false
    private var filterStatus: String? = null
    private var filterGender: String? = null
    private var filterSpecies: String? = null

    fun fetchCharacters(page: Int) {
        if (isLoading.value == true || isLastPage || pageLoadingStates[page] == true) {
            return
        }

        pageLoadingStates[page] = true
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val fetchedCharacters = repository.getCharacters(page)

                val currentCharacters = if (page == 1) {
                    fetchedCharacters
                } else {
                    _characters.value.orEmpty().toMutableList().apply { addAll(fetchedCharacters) }
                }
                _characters.value = currentCharacters.distinctBy { it.id }

                currentPage = page
                isLastPage = fetchedCharacters.isEmpty()
            } catch (e: HttpException) {
                if (e.code() == 404) {
                    isLastPage = true
                } else {
                    _errorMessage.value = "Error fetching characters: ${e.message()}"
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
        if (isFiltering) {
            fetchFilteredCharactersNextPage(currentPage + 1)
        } else {
            fetchCharacters(currentPage + 1)
        }
    }

    fun searchCharacters(query: String) {
        currentSearchQuery = query
        isFiltering = query.isNotBlank()
        fetchFilteredCharacters(query, "", "", 1)
}
    fun fetchFilteredCharacters(status: String?, gender: String?, species: String?, page: Int = 1) {
        _isLoading.value = true
        isFiltering = true
        currentPage = page
        isLastPage = false
        filterStatus = status
        filterGender = gender
        filterSpecies = species
        pageLoadingStates.clear()

        viewModelScope.launch {
            try {
                val filteredCharacters = repository.getFilteredCharacters(status, gender, species, page)
                if (filteredCharacters.isEmpty()) {
                    _noResults.value = true
                    _characters.value = emptyList()
                } else {
                    val currentCharacters = if (page == 1) {
                        filteredCharacters
                    } else {
                        _characters.value.orEmpty().toMutableList().apply { addAll(filteredCharacters) }
                    }
                    _characters.value = currentCharacters.distinctBy { it.id }
                    _noResults.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error fetching filtered characters: ${e.message}"
                Log.e("CharacterViewModel", "Error fetching filtered characters", e)
                _characters.value = emptyList()
                _noResults.value = true
            } finally {
                _isLoading.value = false
            }
        }
    }
    private fun fetchFilteredCharactersNextPage(page: Int) {
        if (isLoading.value == true || isLastPage || pageLoadingStates[page] == true) {
            return
        }

        pageLoadingStates[page] = true
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val fetchedCharacters = repository.getFilteredCharacters(filterStatus, filterGender, filterSpecies, page)

                val currentCharacters = _characters.value.orEmpty().toMutableList().apply { addAll(fetchedCharacters) }
                _characters.value = currentCharacters.distinctBy { it.id }

                currentPage = page
                isLastPage = fetchedCharacters.isEmpty()
            } catch (e: HttpException) {
                if (e.code() == 404) {
                    isLastPage = true
                } else {
                    _errorMessage.value = "Error fetching filtered characters: ${e.message()}"
                    Log.e("CharacterViewModel", "Error fetching filtered characters", e)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error fetching filtered characters: ${e.message}"
                Log.e("CharacterViewModel", "Error fetching filtered characters", e)
            } finally {
                _isLoading.value = false
                pageLoadingStates[page] = false
            }
        }
    }
}