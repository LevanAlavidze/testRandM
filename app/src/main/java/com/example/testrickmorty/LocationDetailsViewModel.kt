package com.example.testrickmorty

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class LocationDetailsViewModel(private val repository: Repository, locationId: Int) : ViewModel() {
    private val _location = MutableLiveData<Location?>()
    val location: LiveData<Location?> = _location

    private val _residentCharacters = MutableLiveData<List<Character>>()
    val residentCharacters: LiveData<List<Character>> = _residentCharacters

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        viewModelScope.launch {
            try {
                _location.value = repository.getLocation(locationId)
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun fetchResidentCharacters(residentUrls: List<String>) {
        viewModelScope.launch {
            val residentCharacters = residentUrls.mapNotNull { url ->
                try {
                    val characterId = extractCharacterIdFromUrl(url)
                    repository.getCharacter(characterId)
                } catch (e: Exception) {
                    // Handle errors, e.g., log the error or show a message
                    null // Return null in case of an error
                }
            }
            _residentCharacters.value = residentCharacters
        }
    }

    private fun extractCharacterIdFromUrl(url: String): Int {
        // Extract the character ID from the URL
        // Example using string manipulation:
        return url.substringAfterLast('/').toIntOrNull() ?: 0
    }
}

class LocationDetailsViewModelFactory(private val repository: Repository, private val locationId:Int) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LocationDetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LocationDetailsViewModel(repository, locationId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}