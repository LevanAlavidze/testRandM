package com.example.testrickmorty.feature.location_details.vm

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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
                    Log.d("LocationDetailsViewModel", "Starting to load location details for ID: $id")

                    // Fetch location from API
                    Log.d("LocationDetailsViewModel", "Fetching location from API")
                    val location = repository.getLocation(id)
                    Log.d("LocationDetailsViewModel", "Fetched location from API: $location")

                    _location.value = location

                    // Fetch characters associated with the location
                    val characterUrls = location.residents
                    Log.d("LocationDetailsViewModel", "Character URLs for location: $characterUrls")
                    val characters = repository.getCharactersByUrls(characterUrls)
                    Log.d("LocationDetailsViewModel", "Fetched characters from URLs: $characters")
                    _residentCharacters.value = characters

                    // Save fetched location and characters to the database
                    Log.d("LocationDetailsViewModel", "Saving location and characters to database")
                    repository.saveLocationsToDatabase(listOf(location))
                    repository.saveCharactersToDatabase(characters)
                    Log.d("LocationDetailsViewModel", "Location and characters saved to database")
                } catch (e: Exception) {
                    _errorMessage.value = "Error fetching location details: ${e.message}"
                    Log.e("LocationDetailsViewModel", "Error fetching location details", e)

                    // Try to load from cache if network fails
                    try {
                        Log.d("LocationDetailsViewModel", "Loading location from cache for ID: $id")
                        val cachedLocations = repository.getCachedLocations()
                        Log.d("LocationDetailsViewModel", "Cached locations: $cachedLocations")
                        val cachedLocation = cachedLocations.find { it.id == id }
                        _location.value = cachedLocation

                        cachedLocation?.let { location ->
                            Log.d("LocationDetailsViewModel", "Cached location: $location")
                            val cachedCharacters = repository.getCachedCharacters()
                            Log.d("LocationDetailsViewModel", "Cached characters: $cachedCharacters")

                            val filteredCharacters = cachedCharacters.filter { character ->
                                location.residents.any { it.endsWith("/${character.id}") }
                            }
                            _residentCharacters.value = filteredCharacters
                            Log.d("LocationDetailsViewModel", "Filtered characters: $filteredCharacters")
                        }
                    } catch (cacheException: Exception) {
                        _errorMessage.value = "Error loading cached location details: ${cacheException.message}"
                        Log.e("LocationDetailsViewModel", "Error loading cached location details", cacheException)
                    }
                }
            }
        }
    }
}