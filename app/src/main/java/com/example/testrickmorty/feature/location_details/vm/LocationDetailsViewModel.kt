package com.example.testrickmorty.feature.location_details.vm

import androidx.lifecycle.*
import com.example.testrickmorty.feature.locations.models.Location
import com.example.testrickmorty.data.Repository
import com.example.testrickmorty.feature.characters.models.Character
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationDetailsViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {
    private val _location = MutableLiveData<Location?>()
    val location: LiveData<Location?> get() = _location

    private val _residentCharacters = MutableLiveData<List<Character>>()
    val residentCharacters: LiveData<List<Character>> get() = _residentCharacters

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private var locationId: Int? = null

    fun setLocationId(id: Int) {
        locationId = id
        loadLocationDetails()
    }

    private fun loadLocationDetails() {
        locationId?.let { id ->
            viewModelScope.launch {
                try {
                    fetchLocationFromApi(id)
                } catch (e: Exception) {
                    handleNetworkError(id, e)
                }
            }
        }
    }

    private suspend fun fetchLocationFromApi(id: Int) {
        val location = repository.getLocation(id)
        _location.value = location
        val characterUrls = location.residents
        val characters = repository.getCharactersByUrls(characterUrls)
        _residentCharacters.value = characters
        repository.saveLocationsToDatabase(listOf(location))
        repository.saveCharactersToDatabase(characters)
    }

    private fun handleNetworkError(id: Int, e: Exception) {
        _errorMessage.value = "Error fetching location details: ${e.message}"
        tryLoadFromCache(id)
    }

    private fun tryLoadFromCache(id: Int) {
        viewModelScope.launch {
            try {
                val cachedLocations = repository.getCachedLocations()
                val cachedLocation = cachedLocations.find { it.id == id }
                _location.value = cachedLocation
                cachedLocation?.let { location ->
                    val cachedCharacters = repository.getCachedCharacters()
                    val filteredCharacters = cachedCharacters.filter { character ->
                        location.residents.any { it.endsWith("/${character.id}") }
                    }
                    _residentCharacters.value = filteredCharacters
                }
            } catch (cacheException: Exception) {
                _errorMessage.value = "Error loading cached location details: ${cacheException.message}"
            }
        }
    }
}
