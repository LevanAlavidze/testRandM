package com.example.testrickmorty.feature.episodes.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.testrickmorty.data.Converters

@Entity(tableName = "episode")
@TypeConverters(Converters::class)
data class EpisodeEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val episode: String,
    val airDate: String?,
    val characterUrls: List<String>
)