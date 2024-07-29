package com.example.testrickmorty

import android.app.Application
import com.example.testrickmorty.data.Repository
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MyApplication : Application() {
    @Inject
    lateinit var repository: Repository
}