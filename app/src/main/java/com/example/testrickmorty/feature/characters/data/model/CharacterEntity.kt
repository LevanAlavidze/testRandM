package com.example.testrickmorty.feature.characters.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.testrickmorty.data.Converters

@Entity(tableName = "character")
@TypeConverters(Converters::class)
data class CharacterEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val status: String,
    val species: String,
    val type: String,
    val gender: String,
    val origin: String?,
    val location: String?,
    val image: String,
    val episode: List<String>,
    val url: String,
    val created: String
)