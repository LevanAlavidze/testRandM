package com.example.testrickmorty.feature.character_details.vm

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
                    val character = repository.getCharacter(id)
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
}