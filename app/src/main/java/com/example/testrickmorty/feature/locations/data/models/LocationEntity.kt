package com.example.testrickmorty.feature.locations.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.testrickmorty.data.Converters

@Entity(tableName = "location")
@TypeConverters(Converters::class)
data class LocationEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val type: String,
    val dimension: String,
    val residents: List<String> = emptyList()
)
