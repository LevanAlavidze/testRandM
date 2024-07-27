package com.example.testrickmorty.data

import android.app.Application
import androidx.room.Room
import com.example.testrickmorty.feature.characters.data.CharacterDao
import com.example.testrickmorty.feature.episodes.data.EpisodeDao
import com.example.testrickmorty.feature.locations.data.LocationDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideApiService(): ApiService {
        return Retrofit.Builder()
            .baseUrl("https://rickandmortyapi.com/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideDatabase(app: Application): AppDatabase {
        return Room.databaseBuilder(app, AppDatabase::class.java, "app_database")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideCharacterDao(db: AppDatabase): CharacterDao {
        return db.characterDao()
    }

    @Provides
    fun provideLocationDao(db: AppDatabase): LocationDao {
        return db.locationDao()
    }

    @Provides
    fun provideEpisodeDao(db: AppDatabase): EpisodeDao {
        return db.episodeDao()
    }

    @Provides
    @Singleton
    fun provideRepository(
        apiService: ApiService,
        characterDao: CharacterDao,
        locationDao: LocationDao,
        episodeDao: EpisodeDao
    ): Repository {
        return Repository(apiService, characterDao, locationDao, episodeDao)
    }
}
