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
                    val episode = fetchEpisodeDetails(id)
                    _episode.value = episode

                    val characters = fetchEpisodeCharacters(episode.characters)
                    _episodeCharacters.value = characters

                    saveToDatabase(episode, characters)
                } catch (e: Exception) {
                    handleError(e, id)
                }
            }
        }
    }

    private suspend fun fetchEpisodeDetails(id: Int): Episode {
        return repository.getEpisode(id)
    }

    private suspend fun fetchEpisodeCharacters(characterUrls: List<String>): List<Character> {
        return repository.getCharactersByUrls(characterUrls)
    }

    private suspend fun saveToDatabase(episode: Episode, characters: List<Character>) {
        repository.saveEpisodesToDatabase(listOf(episode))
        repository.saveCharactersToDatabase(characters)
    }

    private fun handleError(e: Exception, id: Int) {
        _errorMessage.value = "Error fetching episode details: ${e.message}"
        loadFromCache(id)
    }

    private fun loadFromCache(id: Int) {
        viewModelScope.launch {
            try {
                val cachedEpisode = getCachedEpisode(id)
                _episode.value = cachedEpisode

                cachedEpisode?.let { episode ->
                    val cachedCharacters = getCachedCharacters(episode.characters)
                    _episodeCharacters.value = cachedCharacters
                }
            } catch (cacheException: Exception) {
                _errorMessage.value = "Error loading cached episode details: ${cacheException.message}"
            }
        }
    }

    private suspend fun getCachedEpisode(id: Int): Episode? {
        return repository.getCachedEpisodes().find { it.id == id }
    }

    private suspend fun getCachedCharacters(characterUrls: List<String>): List<Character> {
        return repository.getCachedCharacters().filter { character ->
            characterUrls.any { it.endsWith("/${character.id}") }
        }
    }
}
