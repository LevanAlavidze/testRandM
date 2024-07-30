package com.example.testrickmorty.feature.character_details.vm

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.testrickmorty.feature.episodes.models.Episode
import com.example.testrickmorty.data.Repository
import com.example.testrickmorty.feature.characters.models.Character
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
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
                    // Attempt to fetch character details from the network
                    val character = repository.getCharacter(id)
                    _character.value = character

                    // Fetch episodes using the episode URLs
                    val episodeUrls = character.episode
                    val episodes = repository.getEpisodesByUrls(episodeUrls)
                    _episodes.value = episodes

                    // Save fetched character and episodes to the database
                    repository.saveCharactersToDatabase(listOf(character))
                    repository.saveEpisodesToDatabase(episodes)
                } catch (e: Exception) {
                    // If network request fails, try to load from the database
                    _errorMessage.value = "Error fetching character details: ${e.message}"
                    Log.e("CharacterDetailsViewModel", "Error fetching character details", e)

                    try {
                        // Load character from the cached list
                        val cachedCharacters = repository.getCachedCharacters()
                        val cachedCharacter = cachedCharacters.find { it.id == id }
                        _character.value = cachedCharacter

                        cachedCharacter?.let { character ->
                            // Fetch cached episodes
                            val cachedEpisodes = repository.getCachedEpisodes()
                            // Filter the cached episodes based on the character's episode URLs
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
        }
    }
}