package com.example.testrickmorty.feature.locations.vm

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testrickmorty.data.Repository
import com.example.testrickmorty.feature.locations.models.Location
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class LocationsViewModel @Inject constructor(private val repository: Repository) : ViewModel() {

    private val _locations = MutableLiveData<List<Location>>()
    val locations: LiveData<List<Location>> get() = _locations

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
    private var filterType: String = ""
    private var filterDimension: String = ""


    fun fetchLocations(page: Int) {
        if (isLoading.value == true || isLastPage || pageLoadingStates[page] == true) {
            return
        }

        pageLoadingStates[page] = true
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val fetchedLocations = repository.getLocations(page)

                val currentLocations = if (page == 1) {
                    fetchedLocations
                }else{
                    _locations.value.orEmpty().toMutableList().apply {addAll(fetchedLocations)}
                }
                _locations.value = currentLocations.distinctBy { it.id }

                currentPage = page
                isLastPage = fetchedLocations.isEmpty()

                // Save fetched locations to the database
                repository.saveLocationsToDatabase(fetchedLocations)
            } catch (e: HttpException) {
                if (e.code() == 404) {
                    isLastPage = true
                } else {
                    _errorMessage.value = "Error fetching locations: ${e.message()}"
                    Log.e("LocationsViewModel", "Error fetching locations", e)
                }
                // Load cached data as fallback
                loadCachedLocations()
            }catch (e: Exception) {
                _errorMessage.value = "Error fetching locations: ${e.message}"
                Log.e("LocationsViewModel", "Error fetching locations", e)
            } finally {
                _isLoading.value = false
                pageLoadingStates[page] = false
            }
        }
    }

    private fun loadCachedLocations(){
        viewModelScope.launch {
            try {
                val cachedLocations = repository.getCachedLocations()
                _locations.value = cachedLocations
                Log.d(
                    "LocationsViewModel",
                    "Loaded cached locations: ${cachedLocations.size} items"
                )
            }catch (cacheException: Exception) {
                _errorMessage.value = "Error loading cached locations: ${cacheException.message}"
                Log.e(
                    "LocationsViewModel",
                    "Error loading cached locations: ${cacheException.message}")
            }
        }
    }

    fun fetchNextPage() {
        if (isFiltering) {
            fetchFilteredLocationsNextPage(currentPage + 1)
        } else {
            fetchLocations(currentPage + 1)
        }
    }

    fun searchLocations(query: String) {
        currentSearchQuery = query
        isFiltering = query.isNotBlank()
        fetchFilteredLocations(query,"","",1)
    }

    fun fetchFilteredLocations(name: String, type: String, dimension: String, page: Int = 1) {
        _isLoading.value = true
        isFiltering = true
        currentPage = page
        isLastPage = false
        filterName = name
        filterType = type
        filterDimension = dimension
        pageLoadingStates.clear()

        viewModelScope.launch {
            try {
                val filteredLocations = repository.getFilteredLocations(
                    name, type, dimension, page)
                if (filteredLocations.isEmpty()) {
                    _noResults.value = true
                    _locations.value = emptyList()
                } else {
                    val currentLocations = if (page == 1) {
                        filteredLocations
                    } else {
                        _locations.value.orEmpty().toMutableList()
                            .apply { addAll(filteredLocations) }
                    }
                    _locations.value = currentLocations.distinctBy { it.id }
                    _noResults.value = false
                }
            }catch (e: Exception) {
                _errorMessage.value = "Error fetching locations: ${e.message}"
                Log.e("LocationsViewModel", "Error fetching locations", e)
                _locations.value = emptyList()
                _noResults.value = true
            }finally {
                _isLoading.value = false
            }
        }
    }

    private fun fetchFilteredLocationsNextPage(page: Int) {
        if (isLoading.value == true || isLastPage || pageLoadingStates[page] == true) {
            return
        }
        pageLoadingStates[page] = true
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response =
                    repository.getFilteredLocations(filterName, filterType, filterDimension, page)

                val currentLocations = _locations.value.orEmpty().toMutableList()
                currentLocations.addAll(response)
                _locations.value = currentLocations.distinctBy { it.id }

                currentPage = page
                isLastPage = response.isEmpty()
            } catch (e: HttpException) {
                if (e.code() == 404) {
                    isLastPage = true
                } else {
                    _errorMessage.value = "Error fetching locations: ${e.message()}"
                    Log.e("LocationsViewModel", "Error fetching locations", e)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error fetching locations: ${e.message}"
                Log.e("LocationsViewModel", "Error fetching locations", e)
            } finally {
                _isLoading.value = false
                pageLoadingStates[page] = false
            }
        }
    }
}







