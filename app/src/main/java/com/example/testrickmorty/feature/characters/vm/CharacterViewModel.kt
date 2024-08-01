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
    @ApplicationContext private val context: Context
) : ViewModel() {

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
            return
        }

        pageLoadingStates[page] = true
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val fetchedCharacters = fetchCharactersFromSource(page)
                updateCharactersList(fetchedCharacters, page)
            } catch (e: Exception) {
                handleFetchError(e)
                loadCachedCharacters()
            } finally {
                _isLoading.value = false
                pageLoadingStates[page] = false
            }
        }
    }


    private suspend fun fetchCharactersFromSource(page: Int): List<Character> {
        return if (NetworkUtils.hasNetwork(context)) {
            val charactersFromApi = repository.getCharacters(page)
            repository.saveCharactersToDatabase(charactersFromApi)
            charactersFromApi
        } else {
            repository.getCachedCharacters()
        }
    }

    private fun updateCharactersList(fetchedCharacters: List<Character>, page: Int) {
        val currentCharacters = if (page == 1) {
            fetchedCharacters
        } else {
            _characters.value.orEmpty().toMutableList().apply { addAll(fetchedCharacters) }
        }
        _characters.value = currentCharacters.distinctBy { it.id }
        currentPage = page
        isLastPage = fetchedCharacters.isEmpty()
    }

    private fun handleFetchError(e: Exception) {
        when (e) {
            is HttpException -> {
                if (e.code() == 404) {
                    isLastPage = true
                } else {
                    _errorMessage.value = "Error fetching characters: ${e.message()}"
                }
            }
            else -> {
                _errorMessage.value = "Error fetching characters: ${e.message}"
            }
        }
    }

    private fun loadCachedCharacters() {
        viewModelScope.launch {
            try {
                _characters.value = if (isFiltering) {
                    repository.getFilteredCachedCharacters(
                        filterName.orEmpty(),
                        filterStatus.orEmpty(),
                        filterSpecies.orEmpty(),
                        filterGender.orEmpty()
                    )
                } else {
                    repository.getCachedCharacters()
                }
            } catch (cacheException: Exception) {
                _errorMessage.value = "Error loading cached characters: ${cacheException.message}"
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
        _isLoading.value = true
        viewModelScope.launch {
            try {
                if (NetworkUtils.hasNetwork(context)) {
                    fetchFilteredCharacters(name = query, status = "", species = "", gender = "", page = 1)
                } else {
                    val filteredCharacters = repository.getFilteredCachedCharacters(name = query, status = "", species = "", gender = "")
                    _characters.value = filteredCharacters
                    _noResults.value = filteredCharacters.isEmpty()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error searching characters: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchFilteredCharacters(name: String, status: String, species: String, gender: String, page: Int = 1) {
        isFiltering = true
        currentPage = page
        isLastPage = false
        filterName = name
        filterStatus = status
        filterSpecies = species
        filterGender = gender
        pageLoadingStates.clear()
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val dataFromApi = fetchFilteredCharactersFromSource(name, status, species, gender, page)
                _characters.value = dataFromApi.distinctBy { it.id }
                _noResults.value = dataFromApi.isEmpty()
            } catch (e: Exception) {
                _errorMessage.value = "Error filtering characters: ${e.message}"
                _characters.value = emptyList()
                _noResults.value = true
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun fetchFilteredCharactersFromSource(name: String, status: String, species: String, gender: String, page: Int): List<Character> {
        return if (NetworkUtils.hasNetwork(context)) {
            val dataFromApi = repository.getFilteredCharacters(name, status, species, gender, page)
            repository.saveFilteredCharactersToDatabase(name, status, species, gender, dataFromApi)
            dataFromApi
        } else {
            repository.getFilteredCachedCharacters(name, status, species, gender)
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
                val fetchedCharacters = repository.getFilteredCharacters(filterName!!, filterStatus!!, filterSpecies!!, filterGender!!, page)
                updateCharactersList(fetchedCharacters, page)
            } catch (e: Exception) {
                handleFetchError(e)
            } finally {
                _isLoading.value = false
                pageLoadingStates[page] = false
            }
        }
    }
}
