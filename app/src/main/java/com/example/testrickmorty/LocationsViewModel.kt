package com.example.testrickmorty

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class LocationsViewModel(private val repository: Repository) : ViewModel() {

    private val _locations = MutableLiveData<List<Location>>()
    val locations: LiveData<List<Location>> get() = _locations

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private var currentPage = 1
    private var isRefreshing = false

    init {
        fetchLocations()
    }

    fun fetchLocations() {
        if (isRefreshing) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val newLocations = repository.getLocations(currentPage)
                val currentList = _locations.value.orEmpty()
                _locations.value = currentList + newLocations
                currentPage++
            } catch (e: Exception) {
                Log.e("LocationsViewModel", "Error fetching locations", e)
                _errorMessage.value = "Error fetching locations: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchNextPage() {
        fetchLocations()
    }

    fun refreshLocations() {
        isRefreshing = true
        currentPage = 1
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
    }
}