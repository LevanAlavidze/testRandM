package com.example.testrickmorty

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class EpisodeViewModel(private val repository: Repository) : ViewModel() {

    private val _episodes = MutableLiveData<List<Episode>>()
    val episodes: LiveData<List<Episode>> get() = _episodes

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private var currentPage = 1
    private var isLastPage = false

    fun fetchEpisodes(page: Int) {
        if (isLoading.value == true || isLastPage) {
            Log.d("EpisodeViewModel", "Fetch skipped: isLoading=${isLoading.value}, isLastPage=$isLastPage")
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val fetchedEpisodes = repository.getEpisodes(page)
                Log.d("EpisodeViewModel", "Fetched Episodes: $fetchedEpisodes")

                val currentEpisodes = _episodes.value.orEmpty()
                Log.d("EpisodeViewModel", "Current Episodes: $currentEpisodes")

                val updatedList = currentEpisodes.toMutableList().apply { addAll(fetchedEpisodes) }
                Log.d("EpisodeViewModel", "Updated Episodes List: $updatedList")

                _episodes.value = updatedList
                currentPage = page
                isLastPage = fetchedEpisodes.isEmpty()

                repository.saveEpisodesToDatabase(fetchedEpisodes)
                Log.d("EpisodeViewModel", "Episodes saved to database")
            } catch (e: Exception) {
                _errorMessage.value = "Error fetching episodes: ${e.message}"
                Log.e("EpisodeViewModel", "Error fetching episodes", e)

                try {
                    val cachedEpisodes = repository.getCachedEpisodes()
                    Log.d("EpisodeViewModel", "Cached Episodes: $cachedEpisodes")

                    _episodes.value = cachedEpisodes
                } catch (cacheException: Exception) {
                    _errorMessage.value += "\nError loading cached episodes: ${cacheException.message}"
                    Log.e("EpisodeViewModel", "Error loading cached episodes", cacheException)
                }
            } finally {
                _isLoading.value = false
                Log.d("EpisodeViewModel", "Loading state set to false")
            }
        }
    }



    fun fetchNextPage() {
        if (!isLastPage) {
            fetchEpisodes(currentPage + 1)
        }
    }
}
