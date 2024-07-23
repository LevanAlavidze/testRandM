package com.example.testrickmorty

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class LocationDetailsViewModel(
    private val repository: Repository,
    private val locationId: Int
) : ViewModel() {

    private val _location = MutableLiveData<Location>()
    val location: LiveData<Location> get() = _location

    private val _residentCharacters = MutableLiveData<List<Character>>()
    val residentCharacters: LiveData<List<Character>> get() = _residentCharacters

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private var currentPage = 1
    private var isLastPage = false

    init {
        fetchLocation()
    }

    fun fetchLocation() {
        viewModelScope.launch {
            try {
                _location.value = repository.getLocation(locationId)
                // Start fetching residents
                fetchResidentCharacters(_location.value?.residents ?: emptyList())
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun fetchResidentCharacters(residents: List<String>) {
        if (isLastPage) return

        viewModelScope.launch {
            try {
                val characters = repository.getCharactersByUrls(residents)
                if (characters.isEmpty()) {
                    isLastPage = true
                } else {
                    currentPage++
                    val updatedList = _residentCharacters.value?.toMutableList() ?: mutableListOf()
                    updatedList.addAll(characters)
                    _residentCharacters.postValue(updatedList)
                }
            } catch (e: Exception) {
                _errorMessage.postValue("Failed to load residents.")
            }
        }
    }

    fun loadMoreCharacters() {
        if (!isLastPage) {
            fetchResidentCharacters(_location.value?.residents ?: emptyList())
        }
    }
}

class LocationDetailsViewModelFactory(private val repository: Repository, private val locationId: Int) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LocationDetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LocationDetailsViewModel(repository, locationId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}