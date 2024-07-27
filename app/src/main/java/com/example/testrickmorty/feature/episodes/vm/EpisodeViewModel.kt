package com.example.testrickmorty.feature.episodes.vm

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testrickmorty.data.Repository
import com.example.testrickmorty.feature.episodes.models.Episode
import kotlinx.coroutines.launch
import retrofit2.HttpException

class EpisodeViewModel(private val repository: Repository) : ViewModel() {

    private val _episodes = MutableLiveData<List<Episode>>()
    val episodes: LiveData<List<Episode>> get() = _episodes

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _noResults = MutableLiveData<Boolean>()
    val noResults: LiveData<Boolean> get() = _noResults

    private var currentPage = 1
    private var isLastPage = false
    private var currentSearchQuery: String? = null
    private val pageLoadingStates = mutableMapOf<Int, Boolean>()
    private var isFiltering = false
    private var filterName: String = ""
    private var filterEpisode: String = ""


    fun fetchEpisodes(page: Int) {
        if (isLoading.value == true || isLastPage || pageLoadingStates[page] == true) {
            return
        }

        pageLoadingStates[page] = true
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val fetchedEpisodes = repository.getEpisodes(page)

                val currentEpisodes = if (page == 1) {
                    fetchedEpisodes
                } else {
                    _episodes.value.orEmpty().toMutableList().apply { addAll(fetchedEpisodes) }
                }
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
        if (isFiltering) {
            fetchFilteredEpisodesNextPage(currentPage + 1)
        } else {
            fetchEpisodes(currentPage + 1)
        }
    }

    fun searchEpisodes(query: String) {
        currentSearchQuery = query
        isFiltering = query.isNotBlank()
        fetchFilteredEpisodes(query, "", 1)
    }


    fun fetchFilteredEpisodes(name: String, episode: String, page: Int = 1) {
        _isLoading.value = true
        isFiltering = true
        currentPage = page
        isLastPage = false
        filterName = name
        filterEpisode = episode
        pageLoadingStates.clear()

        viewModelScope.launch {
            try {
                val filteredEpisodes = repository.getFilteredEpisodes(name, episode, page)
                if (filteredEpisodes.isEmpty()) {
                    _noResults.value = true
                    _episodes.value = emptyList()
                } else {
                    val currentEpisodes = if (page == 1) {
                        filteredEpisodes
                    } else {
                        _episodes.value.orEmpty().toMutableList().apply { addAll(filteredEpisodes) }
                    }
                    _episodes.value = currentEpisodes.distinctBy { it.id }
                    _noResults.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error fetching filtered episodes: ${e.message}"
                Log.e("EpisodeViewModel", "Error fetching filtered episodes", e)
                _episodes.value = emptyList()
                _noResults.value = true
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun fetchFilteredEpisodesNextPage(page: Int) {
        if (isLoading.value == true || isLastPage || pageLoadingStates[page] == true) {
            return
        }

        pageLoadingStates[page] = true
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = repository.getFilteredEpisodes(filterName, filterEpisode, page)
                val fetchedEpisodes = response

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
}