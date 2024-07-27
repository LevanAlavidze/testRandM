package com.example.testrickmorty.feature.episodes.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.testrickmorty.feature.episodes.vm.EpisodeViewModel
import com.example.testrickmorty.data.Repository

class EpisodeViewModelFactory(
    private val repository: Repository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EpisodeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EpisodeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}