package com.example.testrickmorty

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "episode")
data class EpisodeEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val episode: String,
    val airDate: String?
)