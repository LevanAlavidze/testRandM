package com.example.testrickmorty.feature.characters.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.testrickmorty.feature.characters.data.model.CharacterEntity

@Dao
interface CharacterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(characters: List<CharacterEntity>)

    @Query("SELECT * FROM character")
    suspend fun getAllCharacters(): List<CharacterEntity>

    @Query("""
    SELECT * FROM character
    WHERE name LIKE '%' || :name || '%'
    AND status LIKE '%' || :status || '%'
    AND species LIKE '%' || :species || '%'
    AND gender LIKE '%' || :gender || '%'
""")
    suspend fun getFilteredCharacters(name: String, status: String, species: String, gender: String): List<CharacterEntity>

}