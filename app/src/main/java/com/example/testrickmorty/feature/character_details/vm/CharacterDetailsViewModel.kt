package com.example.testrickmorty.feature.character_details.vm

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testrickmorty.feature.episodes.models.Episode
import com.example.testrickmorty.data.Repository
import com.example.testrickmorty.feature.characters.models.Character
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CharacterDetailsViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private val _character = MutableLiveData<Character?>()
    val character: LiveData<Character?> get() = _character

    private val _episodes = MutableLiveData<List<Episode>>()
    val episodes: LiveData<List<Episode>> get() = _episodes

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private var characterId: Int? = null

    fun setCharacterId(id: Int) {
        characterId = id
        loadCharacterDetails()
    }

    private fun loadCharacterDetails() {
        characterId?.let { id ->
            viewModelScope.launch {
                try {
                    val character = repository.getCharacter(id)
                    _character.value = character

                    val episodes = repository.getEpisodesByUrls(character.episode)
                    _episodes.value = episodes

                    repository.saveCharactersToDatabase(listOf(character))
                    repository.saveEpisodesToDatabase(episodes)
                } catch (e: Exception) {
                    handleNetworkError(e, id)
                }
            }
        }
    }

    private suspend fun handleNetworkError(exception: Exception, id: Int) {
        _errorMessage.value = "Error fetching character details: ${exception.message}"
        Log.e("CharacterDetailsViewModel", "Error fetching character details", exception)

        try {
            val cachedCharacters = repository.getCachedCharacters()
            val cachedCharacter = cachedCharacters.find { it.id == id }
            _character.value = cachedCharacter

            cachedCharacter?.let { character ->
                val cachedEpisodes = repository.getCachedEpisodes()
                val filteredEpisodes = cachedEpisodes.filter { episode ->
                    character.episode.contains("https://rickandmortyapi.com/api/episode/${episode.id}")
                }
                _episodes.value = filteredEpisodes
            }
        } catch (cacheException: Exception) {
            _errorMessage.value = "Error loading cached character details: ${cacheException.message}"
            Log.e("CharacterDetailsViewModel", "Error loading cached character details", cacheException)
        }
    }
}
