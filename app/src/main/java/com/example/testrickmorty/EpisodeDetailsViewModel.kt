package com.example.testrickmorty

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class EpisodeDetailsViewModel(
    private val repository: Repository,
    private val episodeId: Int
) : ViewModel() {

    private val _episode = MutableLiveData<Episode>()
    val episode: LiveData<Episode> get() = _episode

    private val _episodeCharacters = MutableLiveData<List<Character>>()
    val episodeCharacters: LiveData<List<Character>> get() = _episodeCharacters

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    init {
        fetchEpisode()
    }

    private fun fetchEpisode() {
        viewModelScope.launch {
            try {
                _episode.value = repository.getEpisode(episodeId)
                fetchEpisodeCharacters(_episode.value?.characters ?: emptyList())
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun fetchEpisodeCharacters(characterUrls: List<String>) {
        viewModelScope.launch {
            try {
                val characters = repository.getCharactersByUrls(characterUrls)
                _episodeCharacters.value = characters
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load episode characters."
            }
        }
    }
}

class EpisodeDetailsViewModelFactory(
    private val repository: Repository,
    private val episodeId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EpisodeDetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EpisodeDetailsViewModel(repository, episodeId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}