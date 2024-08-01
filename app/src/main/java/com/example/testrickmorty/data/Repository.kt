package com.example.testrickmorty.data

import com.example.testrickmorty.feature.characters.models.Character
import com.example.testrickmorty.feature.episodes.models.Episode
import com.example.testrickmorty.feature.locations.models.Location

interface Repository {
    suspend fun getCharacters(page: Int): List<Character>
    suspend fun getLocations(page: Int): List<Location>
    suspend fun getEpisodes(page: Int): List<Episode>
    suspend fun getCharacter(characterId: Int): Character
    suspend fun getLocation(locationId: Int): Location
    suspend fun getEpisode(episodeId: Int): Episode
    suspend fun getCharactersByUrls(urls: List<String>): List<Character>
    suspend fun getEpisodesByUrls(urls: List<String>): List<Episode>
    suspend fun getFilteredCharacters(name: String, status: String, species: String, gender: String, page: Int): List<Character>
    suspend fun getFilteredEpisodes(name: String, episode: String, page: Int): List<Episode>
    suspend fun getFilteredLocations(name: String, type: String, dimension: String, page: Int): List<Location>
    suspend fun saveCharactersToDatabase(characters: List<Character>)
    suspend fun saveLocationsToDatabase(locations: List<Location>)
    suspend fun saveEpisodesToDatabase(episodes: List<Episode>)
    suspend fun getCachedCharacters(): List<Character>
    suspend fun getCachedLocations(): List<Location>
    suspend fun getCachedEpisodes(): List<Episode>
    suspend fun saveFilteredCharactersToDatabase(name: String, status: String, species: String, gender: String, characters: List<Character>)
    suspend fun getFilteredCachedCharacters(name: String, status: String, species: String, gender: String): List<Character>
}
