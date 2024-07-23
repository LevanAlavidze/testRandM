package com.example.testrickmorty

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class CharacterDetailsViewModel(private val repository: Repository, private val characterId: Int) : ViewModel() {
    private val _character = MutableLiveData<Character?>()
    val character: LiveData<Character?> get() = _character

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    init {
        fetchCharacterDetails()
    }

    private fun fetchCharacterDetails() {
        viewModelScope.launch {
            try {
                val character = repository.getCharacter(characterId)
                _character.value = character
            } catch (e: Exception) {
                _errorMessage.value = "Error fetching character details: ${e.message}"
            }
        }
    }
}

class CharacterDetailsViewModelFactory(
    private val repository: Repository,
    private val characterId: Int
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CharacterDetailsViewModel::class.java)) {
            return CharacterDetailsViewModel(repository, characterId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}