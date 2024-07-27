package com.example.testrickmorty.feature.character_details.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.testrickmorty.feature.episodes.models.Episode
import com.example.testrickmorty.data.Repository
import com.example.testrickmorty.feature.characters.models.Character
import kotlinx.coroutines.launch

class CharacterDetailsViewModel(private val repository: Repository, private val characterId: Int) : ViewModel() {
    private val _character = MutableLiveData<Character?>()
    val character: LiveData<Character?> get() = _character

    private val _episodes = MutableLiveData<List<Episode>>()
    val episodes: LiveData<List<Episode>> get() = _episodes

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    init {
        viewModelScope.launch {
            try {
                val character = repository.getCharacter(characterId)
                _character.value = character

                val episodeUrls = character.episode
                val episodes = repository.getEpisodesByUrls(episodeUrls)
                _episodes.value = episodes
            } catch (e: Exception) {
                _errorMessage.value = e.message
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