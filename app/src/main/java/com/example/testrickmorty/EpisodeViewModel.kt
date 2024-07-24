package com.example.testrickmorty

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EpisodeViewModel(private val repository: Repository) : ViewModel() {

    private val _episodes = MutableLiveData<List<Episode>>()
    val episodes: LiveData<List<Episode>> get() = _episodes

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private var currentPage = 1
    private var isLastPage = false
    private var currentSearchQuery: String? = null
    private var searchResults = mutableListOf<Episode>()

    fun fetchEpisodes(page: Int) {
        if (isLoading.value == true || isLastPage) {
            return
        }
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val fetchedEpisodes = if (currentSearchQuery != null) {
                    // Fetch next page of search results
                    val response = repository.searchEpisodes(currentSearchQuery!!)
                    searchResults.addAll(response.results)
                    response.results
                } else {
                    // Fetch regular episodes
                    repository.getEpisodes(page)
                }

                // Combine current episodes with new fetched episodes and remove duplicates
                val currentEpisodes = _episodes.value.orEmpty().toMutableSet()
                currentEpisodes.addAll(fetchedEpisodes)
                _episodes.value = currentEpisodes.toList()

                currentPage = page
                isLastPage = fetchedEpisodes.isEmpty() // Update isLastPage correctly

            } catch (e: Exception) {
                _errorMessage.value = "Error fetching episodes: ${e.message}"
                Log.e("EpisodeViewModel", "Error fetching episodes", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchNextPage() {
        if (!isLastPage) {
            fetchEpisodes(currentPage + 1)
        }
    }

    fun searchEpisodes(query: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                if (query.isBlank()) {
                    // Clear search query and show all episodes
                    currentSearchQuery = null
                    searchResults.clear()
                    currentPage = 1
                    isLastPage = false
                    fetchEpisodes(1) // Fetch first page of all episodes
                } else {
                    currentSearchQuery = query
                    currentPage = 1
                    isLastPage = false
                    searchResults.clear()
                    val response = repository.searchEpisodes(query)
                    _episodes.value = response.results
                    searchResults.addAll(response.results)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error fetching episodes: ${e.message}"
                Log.e("EpisodeViewModel", "Error fetching episodes", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
