package com.example.testrickmorty.feature.episode_details.vm

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
class EpisodeDetailsViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {
    private val _episode = MutableLiveData<Episode?>()
    val episode: LiveData<Episode?> get() = _episode

    private val _episodeCharacters = MutableLiveData<List<Character>>()
    val episodeCharacters: LiveData<List<Character>> get() = _episodeCharacters

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private var episodeId: Int? = null

    fun setEpisodeId(id: Int) {
        episodeId = id
        loadEpisodeDetails()
    }

    private fun loadEpisodeDetails() {
        episodeId?.let { id ->
            viewModelScope.launch {
                try {
                    // Fetch episode from API
                    Log.d("EpisodeDetailsViewModel", "Fetching episode details from API for ID: $id")
                    val episode = repository.getEpisode(id)
                    _episode.value = episode
                    Log.d("EpisodeDetailsViewModel", "Fetched episode: ${episode.name}, Characters count: ${episode.characters.size}")

                    // Fetch characters associated with the episode
                    val characterUrls = episode.characters
                    Log.d("EpisodeDetailsViewModel", "Character URLs: $characterUrls")
                    val characters = repository.getCharactersByUrls(characterUrls)
                    Log.d("EpisodeDetailsViewModel", "Fetched ${characters.size} characters from URLs")
                    _episodeCharacters.value = characters

                    // Save fetched data to the database
                    repository.saveEpisodesToDatabase(listOf(episode))
                    repository.saveCharactersToDatabase(characters)
                } catch (e: Exception) {
                    _errorMessage.value = "Error fetching episode details: ${e.message}"
                    Log.e("EpisodeDetailsViewModel", "Error fetching episode details", e)

                    // Try to load from cache if network fails
                    try {
                        Log.d("EpisodeDetailsViewModel", "Loading episode from cache for ID: $id")
                        val cachedEpisodes = repository.getCachedEpisodes()
                        Log.d("EpisodeDetailsViewModel", "Cached episodes count: ${cachedEpisodes.size}")
                        val cachedEpisode = cachedEpisodes.find { it.id == id }
                        _episode.value = cachedEpisode

                        cachedEpisode?.let { episode ->
                            Log.d("EpisodeDetailsViewModel", "Cached episode: ${episode.name}, Characters count: ${episode.characters.size}")
                            val cachedCharacters = repository.getCachedCharacters()
                            Log.d("EpisodeDetailsViewModel", "Cached characters count: ${cachedCharacters.size}")

                            val filteredCharacters = cachedCharacters.filter { character ->
                                episode.characters.any { it.endsWith("/${character.id}") }
                            }

                            _episodeCharacters.value = filteredCharacters
                            Log.d("EpisodeDetailsViewModel", "Filtered characters count: ${filteredCharacters.size}")
                        }
                    } catch (cacheException: Exception) {
                        _errorMessage.value = "Error loading cached episode details: ${cacheException.message}"
                        Log.e("EpisodeDetailsViewModel", "Error loading cached episode details", cacheException)
                    }
                }
            }
        }
    }
}