package com.example.testrickmorty.data


import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.testrickmorty.feature.characters.data.CharacterDao
import com.example.testrickmorty.feature.characters.data.model.CharacterEntity
import com.example.testrickmorty.feature.episodes.data.EpisodeDao
import com.example.testrickmorty.feature.episodes.data.models.EpisodeEntity
import com.example.testrickmorty.feature.locations.data.LocationDao
import com.example.testrickmorty.feature.locations.data.models.LocationEntity

@Database(entities = [CharacterEntity::class, LocationEntity::class, EpisodeEntity::class], version = 2)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun characterDao(): CharacterDao
    abstract fun locationDao(): LocationDao
    abstract fun episodeDao(): EpisodeDao

}