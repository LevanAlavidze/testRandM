package com.example.testrickmorty.feature.episodes.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testrickmorty.data.Repository
import com.example.testrickmorty.feature.episodes.models.Episode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class EpisodeViewModel @Inject constructor(private val repository: Repository) : ViewModel() {

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
        if (shouldSkipFetch(page)) return

        updateLoadingState(page, true)

        viewModelScope.launch {
            try {
                val fetchedEpisodes = repository.getEpisodes(page)
                handleFetchedEpisodes(fetchedEpisodes, page)
            } catch (e: HttpException) {
                handleException(e)
                loadCachedEpisodes()
            } catch (e: Exception) {
                handleException(e)
                loadCachedEpisodes()
            } finally {
                updateLoadingState(page, false)
            }
        }
    }

    private fun shouldSkipFetch(page: Int) =
        isLoading.value == true || isLastPage || pageLoadingStates[page] == true

    private fun updateLoadingState(page: Int, isLoading: Boolean) {
        pageLoadingStates[page] = isLoading
        _isLoading.value = isLoading
    }

    private suspend fun handleFetchedEpisodes(fetchedEpisodes: List<Episode>, page: Int) {
        val currentEpisodes = if (page == 1) {
            fetchedEpisodes
        } else {
            _episodes.value.orEmpty().toMutableList().apply { addAll(fetchedEpisodes) }
        }
        _episodes.value = currentEpisodes.distinctBy { it.id }

        currentPage = page
        isLastPage = fetchedEpisodes.isEmpty()

        repository.saveEpisodesToDatabase(fetchedEpisodes)
    }

    private fun handleException(e: Exception) {
        if (e is HttpException && e.code() == 404) {
            isLastPage = true
        } else {
            _errorMessage.value = "Error fetching episodes: ${e.message}"
        }
    }

    private fun loadCachedEpisodes() {
        viewModelScope.launch {
            try {
                val cachedEpisodes = repository.getCachedEpisodes()
                _episodes.value = cachedEpisodes
            } catch (cacheException: Exception) {
                _errorMessage.value = "Error loading cached episodes: ${cacheException.message}"
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
                handleFilteredEpisodes(filteredEpisodes)
            } catch (e: Exception) {
                _errorMessage.value = "Error fetching filtered episodes: ${e.message}"
                _episodes.value = emptyList()
                _noResults.value = true
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun handleFilteredEpisodes(filteredEpisodes: List<Episode>) {
        if (filteredEpisodes.isEmpty()) {
            _noResults.value = true
            _episodes.value = emptyList()
        } else {
            val currentEpisodes = if (currentPage == 1) {
                filteredEpisodes
            } else {
                _episodes.value.orEmpty().toMutableList().apply { addAll(filteredEpisodes) }
            }
            _episodes.value = currentEpisodes.distinctBy { it.id }
            _noResults.value = false
        }
    }

    private fun fetchFilteredEpisodesNextPage(page: Int) {
        if (shouldSkipFetch(page)) return

        updateLoadingState(page, true)

        viewModelScope.launch {
            try {
                val response = repository.getFilteredEpisodes(filterName, filterEpisode, page)
                handleFilteredEpisodes(response)
                currentPage = page
                isLastPage = response.isEmpty()
            } catch (e: HttpException) {
                handleException(e)
            } catch (e: Exception) {
                _errorMessage.value = "Error fetching episodes: ${e.message}"
            } finally {
                updateLoadingState(page, false)
            }
        }
    }
}
