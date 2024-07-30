package com.example.testrickmorty.feature.characters.vm

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testrickmorty.data.NetworkUtils
import com.example.testrickmorty.data.Repository
import com.example.testrickmorty.feature.characters.models.Character
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class CharacterViewModel @Inject constructor(
    private val repository: Repository,
    @ApplicationContext private val context: Context) : ViewModel() {

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
    private var filterName: String? = null

    fun fetchCharacters(page: Int) {
        if (isLoading.value == true || isLastPage || pageLoadingStates[page] == true) {
            Log.d("CharacterViewModel", "Fetch request ignored for page $page: already loading or last page")
            return
        }

        pageLoadingStates[page] = true
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val fetchedCharacters = if (NetworkUtils.hasNetwork(context)) {
                    val charactersFromApi = repository.getCharacters(page)
                    repository.saveCharactersToDatabase(charactersFromApi)
                    charactersFromApi
                } else {
                    Log.d("CharacterViewModel", "Network unavailable, loading cached characters")
                    repository.getCachedCharacters()
                }

                val currentCharacters = if (page == 1) {
                    fetchedCharacters
                } else {
                    _characters.value.orEmpty().toMutableList().apply { addAll(fetchedCharacters) }
                }
                _characters.value = currentCharacters.distinctBy { it.id }

                currentPage = page
                isLastPage = fetchedCharacters.isEmpty()
                Log.d("CharacterViewModel", "Fetched characters: ${fetchedCharacters.size} items")
            } catch (e: HttpException) {
                Log.e("CharacterViewModel", "HTTP error fetching characters: ${e.message()}", e)
                if (e.code() == 404) {
                    isLastPage = true
                } else {
                    _errorMessage.value = "Error fetching characters: ${e.message()}"
                    Log.e("CharacterViewModel", "Error fetching characters", e)
                }
                loadCachedCharacters()
            } catch (e: Exception) {
                _errorMessage.value = "Error fetching characters: ${e.message}"
                Log.e("CharacterViewModel", "Error fetching characters", e)
                loadCachedCharacters()
            } finally {
                _isLoading.value = false
                pageLoadingStates[page] = false
            }
        }
    }


    private fun loadCachedCharacters() {
        viewModelScope.launch {
            try {
                Log.d("CharacterViewModel", "Loading cached characters, filtering: $isFiltering")

                val cachedCharacters = if (isFiltering) {
                    repository.getFilteredCachedCharacters(
                        filterName ?: "",
                        filterStatus ?: "",
                        filterSpecies ?: "",
                        filterGender ?: ""
                    )
                } else {
                    repository.getCachedCharacters()
                }
                _characters.value = cachedCharacters
                Log.d("CharacterViewModel", "Loaded cached characters: ${cachedCharacters.size} items")
            } catch (cacheException: Exception) {
                _errorMessage.value = "Error loading cached characters: ${cacheException.message}"
                Log.e("CharacterViewModel", "Error loading cached characters", cacheException)
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
        _isLoading.value = true // Show loading indicator
        viewModelScope.launch {
            try {
                if (NetworkUtils.hasNetwork(context)) {
                    fetchFilteredCharacters(name = query, status = "", species = "", gender = "",page = 1)
                } else {
                    val filteredCharacters = repository.getFilteredCachedCharacters(name = query, status = "", species = "", gender = "")
                    _characters.value = filteredCharacters
                    _noResults.value = filteredCharacters.isEmpty()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error searching characters: ${e.message}"
            } finally {
                _isLoading.value = false // Hide loading indicator
            }
        }
    }


    fun fetchFilteredCharacters(name: String, status: String, species: String, gender: String, page: Int = 1) {
        Log.d("CharacterViewModel", "Fetching filtered characters: Name=$name, Status=$status, Species=$species, Gender=$gender, Page=$page")
        _isLoading.value = true
        _isLoading.value = true
        isFiltering = true
        currentPage = page
        isLastPage = false
        filterName = name
        filterStatus = status
        filterSpecies = species
        filterGender = gender
        pageLoadingStates.clear()

        viewModelScope.launch {
            try {
                if (NetworkUtils.hasNetwork(context)) {
                    val dataFromApi = repository.getFilteredCharacters(name, status, species, gender, page)
                    repository.saveFilteredCharactersToDatabase(name, status, species, gender, dataFromApi)
                    _characters.value = dataFromApi.distinctBy { it.id }
                } else {
                    val filteredCharacters = repository.getFilteredCachedCharacters(name, status, species, gender)
                    _characters.value = filteredCharacters
                }

                _noResults.value = _characters.value?.isEmpty() ?: true
                Log.d("CharacterViewModel", "Fetched filtered characters: ${_characters.value?.size} items")

            } catch (e: Exception) {
                _errorMessage.value = "Error filtering characters: ${e.message}"
                _characters.value = emptyList()
                _noResults.value = true
                Log.e("CharacterViewModel", "Error filtering characters", e)
            } finally {
                _isLoading.value = false
            }
        }
    }


    private fun fetchFilteredCharactersNextPage(page: Int) {
        if (isLoading.value == true || isLastPage || pageLoadingStates[page] == true) {
            Log.d("CharacterViewModel", "Fetch next page request ignored: already loading or last page")
            return
        }

        pageLoadingStates[page] = true
        _isLoading.value = true
        viewModelScope.launch {
            try {
                Log.d("CharacterViewModel", "Fetching filtered characters for page: $page")
                val fetchedCharacters = repository.getFilteredCharacters(filterName!!, filterStatus!!, filterSpecies!!, filterGender!!, page)

                val currentCharacters = _characters.value.orEmpty().toMutableList().apply { addAll(fetchedCharacters) }
                _characters.value = currentCharacters.distinctBy { it.id }

                currentPage = page
                isLastPage = fetchedCharacters.isEmpty()
                Log.d("CharacterViewModel", "Fetched filtered characters for page $page: ${fetchedCharacters.size} items")
            } catch (e: HttpException) {
                Log.e("CharacterViewModel", "HTTP error fetching filtered characters: ${e.message()}", e)
                if (e.code() == 404) {
                    isLastPage = true
                } else {
                    Log.e("CharacterViewModel", "Error fetching filtered characters: ${e.message}", e)
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
