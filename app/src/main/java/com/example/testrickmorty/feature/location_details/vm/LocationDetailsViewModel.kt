package com.example.testrickmorty.feature.location_details.vm

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
    private val _location = MutableLiveData<Location>()
    val location: LiveData<Location> get() = _location

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
                    val location = repository.getLocation(id)
                    _location.value = location

                    val characterUrls = location.residents
                    val characters = repository.getCharactersByUrls(characterUrls)
                    _residentCharacters.value = characters
                } catch (e: Exception) {
                    _errorMessage.value = e.message
                }
            }
            }
    }
}