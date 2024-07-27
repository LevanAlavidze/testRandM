package com.example.testrickmorty

import android.app.Application
import androidx.room.Room
import com.example.testrickmorty.data.ApiService
import com.example.testrickmorty.data.AppDatabase
import com.example.testrickmorty.data.Repository
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {
    lateinit var repository: Repository
    private lateinit var database: AppDatabase

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "my-database1").build()
        val apiService = ApiService.create(applicationContext)
        repository = Repository(apiService, database.characterDao(), database.locationDao(), database.episodeDao())
    }
}