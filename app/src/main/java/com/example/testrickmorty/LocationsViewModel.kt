package com.example.testrickmorty

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.HttpException

class LocationsViewModel(private val repository: Repository) : ViewModel() {

    private val _locations = MutableLiveData<List<Location>>()
    val locations: LiveData<List<Location>> get() = _locations

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private var currentPage = 1
  /*  private var isRefreshing = false*/
    private var isLastPage = false
    private var currentSearchQuery: String? = null
    private val searchResults = mutableListOf<Location>()
    private val pageLoadingStates = mutableMapOf<Int, Boolean>()

/*    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                val cachedLocations = repository.getCachedLocations()
                _locations.value = cachedLocations
                Log.d("LocationsViewModel", "Initial cached locations: ${cachedLocations.size} items")

                fetchLocations(currentPage)
            } catch (e: Exception) {
                _errorMessage.value = "Error loading initial data: ${e.message}"
                Log.e("LocationsViewModel", "Error loading initial data: ${e.message}")
            }
        }
    }*/

    fun fetchLocations(page: Int) {
        if (_isLoading.value == true || isLastPage || pageLoadingStates[page] == true) {
            return
        }

        pageLoadingStates[page] = true
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val fetchedLocations = if (currentSearchQuery != null) {
                    val response = repository.searchLocations(currentSearchQuery!!)
                    response.results
                } else {
                    repository.getLocations(page)
                }

                val currentLocations = _locations.value.orEmpty().toMutableList()
                currentLocations.addAll(fetchedLocations)
                _locations.value = currentLocations.distinctBy { it.id }

                currentPage = page
                isLastPage = fetchedLocations.isEmpty()
            } catch (e: HttpException) {
                if (e.code() == 404) {
                    isLastPage = true
                } else {
                    _errorMessage.value = "Error fetching locations: ${e.message()}"
                    Log.e("LocationsViewModel", "Error fetching locations", e)
                }
            }catch (e: Exception) {
                _errorMessage.value = "Error fetching locations: ${e.message}"
                Log.e("LocationsViewModel", "Error fetching locations", e)
            } finally {
                _isLoading.value = false
                pageLoadingStates[page] = false
            }
        }
    }

    fun fetchNextPage() {
        if (!isLastPage && pageLoadingStates[currentPage + 1] != true) {
            fetchLocations(currentPage + 1)
        }
    }

    /*fun refreshLocations() {
        isRefreshing = true
        currentPage = 1
        isLastPage = false
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val newLocations = repository.getLocations(currentPage)
                _locations.value = newLocations
                currentPage++
            } catch (e: Exception) {
                Log.e("LocationsViewModel", "Error refreshing locations", e)
                _errorMessage.value = "Error refreshing locations: ${e.message}"
            } finally {
                isRefreshing = false
                _isLoading.value = false
            }
        }
    }*/

    fun searchLocations(query: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                if (query.isBlank()) {
                    // Reset search and fetch all locations
                    currentSearchQuery = null
                    searchResults.clear()
                    currentPage = 1
                    isLastPage = false
                    pageLoadingStates.clear()
                    fetchLocations(1) // Fetch all locations
                } else {
                    // Perform search and update the list with search results
                    currentSearchQuery = query
                    currentPage = 1
                    isLastPage = false
                    searchResults.clear()
                    pageLoadingStates.clear()
                    val response = repository.searchLocations(query)
                    _locations.value = searchResults // Set the search results
                    searchResults.addAll(response.results)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error searching locations: ${e.message}"
                Log.e("LocationsViewModel", "Error searching locations", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}







