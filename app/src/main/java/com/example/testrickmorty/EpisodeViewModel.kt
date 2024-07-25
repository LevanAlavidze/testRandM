package com.example.testrickmorty

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

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
    private val pageLoadingStates = mutableMapOf<Int, Boolean>()

    fun fetchEpisodes(page: Int) {
        if (isLoading.value == true || isLastPage || pageLoadingStates[page] == true) {
            return
        }

        pageLoadingStates[page] = true
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val fetchedEpisodes = if (currentSearchQuery != null) {
                    val response = repository.searchEpisodes(currentSearchQuery!!)
                    response.results
                } else {
                    repository.getEpisodes(page)
                }

                // Combine current episodes with new fetched episodes and remove duplicates
                val currentEpisodes = _episodes.value.orEmpty().toMutableList()
                currentEpisodes.addAll(fetchedEpisodes)
                _episodes.value = currentEpisodes.distinctBy { it.id }

                currentPage = page
                isLastPage = fetchedEpisodes.isEmpty()
            } catch (e: HttpException) {
                if (e.code() == 404) {
                    isLastPage = true
                } else {
                    _errorMessage.value = "Error fetching episodes: ${e.message()}"
                    Log.e("EpisodeViewModel", "Error fetching episodes", e)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error fetching episodes: ${e.message}"
                Log.e("EpisodeViewModel", "Error fetching episodes", e)
            } finally {
                _isLoading.value = false
                pageLoadingStates[page] = false
            }
        }
    }

    fun fetchNextPage() {
        if (!isLastPage && pageLoadingStates[currentPage + 1] != true) {
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
                    pageLoadingStates.clear()
                    fetchEpisodes(1) // Fetch first page of all episodes
                } else {
                    currentSearchQuery = query
                    currentPage = 1
                    isLastPage = false
                    searchResults.clear()
                    pageLoadingStates.clear()
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