package com.example.testrickmorty.feature.episode_details.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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
    private val _episode = MutableLiveData<Episode>()
    val episode: LiveData<Episode> get() = _episode

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
                    val episode = repository.getEpisode(id)
                    _episode.value = episode

                    val characterUrls = episode.characters
                    val characters = repository.getCharactersByUrls(characterUrls)
                    _episodeCharacters.value = characters
                } catch (e: Exception) {
                    _errorMessage.value = e.message
                }
            }
        }
    }
}