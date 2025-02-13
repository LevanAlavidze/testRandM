package com.example.testrickmorty.feature.locations.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.testrickmorty.feature.locations.data.models.LocationEntity

@Dao
interface LocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(locations: List<LocationEntity>)

    @Query("SELECT * FROM location")
    suspend fun getAllLocations(): List<LocationEntity>
    @Query("SELECT * FROM location WHERE name LIKE :name AND type LIKE :type AND dimension LIKE :dimension")
    suspend fun getFilteredLocations(name: String, type: String, dimension: String): List<LocationEntity>
}