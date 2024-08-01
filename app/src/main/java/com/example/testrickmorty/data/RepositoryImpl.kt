package com.example.testrickmorty.data

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.testrickmorty.feature.characters.data.CharacterDao
import com.example.testrickmorty.feature.characters.data.model.CharacterEntity
import com.example.testrickmorty.feature.characters.models.Character
import com.example.testrickmorty.feature.characters.models.Origin
import com.example.testrickmorty.feature.episodes.data.EpisodeDao
import com.example.testrickmorty.feature.episodes.data.models.EpisodeEntity
import com.example.testrickmorty.feature.episodes.models.Episode
import com.example.testrickmorty.feature.locations.data.LocationDao
import com.example.testrickmorty.feature.locations.data.models.LocationEntity
import com.example.testrickmorty.feature.locations.models.Location
import retrofit2.HttpException
import javax.inject.Inject

class RepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val characterDao: CharacterDao,
    private val locationDao: LocationDao,
    private val episodeDao: EpisodeDao,
    private val context: Application
) : Repository {
    val errorMessage = MutableLiveData<String>()

    override suspend fun getCharacters(page: Int): List<Character> {
        Log.d("Repository", "Fetching characters from API for page: $page")
        try {
            val response = apiService.getCharacters(page)
            Log.d("Repository", "API Response: ${response.results.size} characters fetched")
            response.results.forEach { character ->
                Log.d("Repository", "Character from API: ${character.name}, ${character.gender}, ${character.species}")
            }
            return response.results
        } catch (e: Exception) {
            Log.e("Repository", "Error fetching characters from API", e)
            throw e
        }
    }

    override suspend fun getLocations(page: Int): List<Location> {
        Log.d("Repository", "Fetching locations from API for page: $page")
        try {
            val response = apiService.getLocations(page)
            Log.d("Repository", "API Response: ${response.results.size} locations fetched")
            return response.results
        } catch (e: Exception) {
            Log.e("Repository", "Error fetching locations from API", e)
            throw e
        }
    }

    override suspend fun getEpisodes(page: Int): List<Episode> {
        Log.d("Repository", "Fetching episodes from API for page: $page")
        try {
            val response = apiService.getEpisodes(page)
            Log.d("Repository", "API Response: ${response.results.size} episodes fetched")
            return response.results
        } catch (e: Exception) {
            Log.e("Repository", "Error fetching episodes from API", e)
            throw e
        }
    }

    override suspend fun getCharacter(characterId: Int): Character {
        try {
            val response = apiService.getCharacter(characterId)
            if (response.isSuccessful) {
                return response.body() ?: throw Exception("Character not found")
            } else {
                throw Exception("Failed to fetch character details")
            }
        } catch (e: Exception) {
            Log.e("Repository", "Error fetching character details for ID $characterId", e)
            throw e
        }
    }

    override suspend fun getLocation(locationId: Int): Location {
        Log.d("Repository", "Fetching location from API for ID: $locationId")
        val response = apiService.getLocation(locationId)
        if (response.isSuccessful) {
            val location = response.body() ?: throw Exception("Location not found")
            Log.d("Repository", "Fetched location: $location")
            return location
        } else {
            Log.e("Repository", "Failed to fetch location details: ${response.errorBody()?.string()}")
            throw Exception("Failed to fetch location details")
        }
    }

    override suspend fun getEpisode(episodeId: Int): Episode {
        val response = apiService.getEpisode(episodeId)
        Log.d("Repository", "Response: ${response.body()}")
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Episode not found")
        } else {
            throw Exception("Failed to fetch episode details")
        }
    }

    // Fetching by URLs
    override suspend fun getCharactersByUrls(urls: List<String>): List<Character> {
        val characters = mutableListOf<Character>()
        for (url in urls) {
            try {
                val response = apiService.getCharacterByUrl(url)
                if (response.isSuccessful) {
                    response.body()?.let { characters.add(it) }
                }
            } catch (e: Exception) {
                Log.e("Repository", "Error fetching character from URL $url: ${e.message}")
            }
        }
        Log.d("Repository", "Fetched ${characters.size} characters from URLs")
        return characters
    }

    override suspend fun getEpisodesByUrls(urls: List<String>): List<Episode> {
        return urls.mapNotNull { url ->
            try {
                val episodeId = url.substringAfterLast("/").toIntOrNull()
                episodeId?.let { getEpisode(it) }
            } catch (e: Exception) {
                null
            }
        }
    }

    // Filtering
    override suspend fun getFilteredCharacters(name: String, status: String, species: String, gender: String, page: Int): List<Character> {
        Log.d("Repository", "Fetching filtered characters from API with: Name=$name, Status=$status, Species=$species, Gender=$gender, Page=$page")
        return if (NetworkUtils.hasNetwork(context)) {
            // Fetch from API
            try {
                Log.d("Repository", "Fetching filtered characters from API: Name=$name, Status=$status, Species=$species, Gender=$gender, Page=$page")
                val response = apiService.getFilteredCharacters(name, status, species, gender, page)
                Log.d("Repository", "API Response: ${response.results.size} characters fetched")
                // Optionally cache the fetched data
                saveFilteredCharactersToDatabase(name, status, species, gender, response.results)
                response.results
            } catch (e: HttpException) {
                Log.e("Repository", "HTTP error: ${e.message()}", e)
                throw e
            } catch (e: Exception) {
                Log.e("Repository", "Error fetching filtered characters: ${e.message}", e)
                throw e
            }
        } else {
            // Fetch from cache
            Log.d("Repository", "Network unavailable, fetching filtered cached characters: Name=$name, Status=$status, Species=$species, Gender=$gender")
            getFilteredCachedCharacters(name, status, species, gender)
        }
    }

    override suspend fun getFilteredEpisodes(name: String, episode: String, page: Int): List<Episode> {
        val response = apiService.getFilteredEpisodes(name, episode, page)
        return response.results
    }

    override suspend fun getFilteredLocations(name: String, type: String, dimension: String, page: Int): List<Location> {
        val response = apiService.getFilteredLocations(name, type, dimension, page)
        return response.results
    }

    // Saving to Database
    override suspend fun saveCharactersToDatabase(characters: List<Character>) {
        Log.d("Repository", "Saving ${characters.size} characters to database")
        characters.forEach { character ->
            Log.d("Repository", "Saving character to DB: ${character.name}, ${character.gender}, ${character.species}")
        }
        val characterEntities = characters.map {
            CharacterEntity(
                id = it.id,
                name = it.name,
                status = it.status,
                species = it.species,
                type = it.type,
                gender = it.gender,
                origin = it.origin.name ?: "Unknown",
                location = it.location.name ?: "Unknown",
                image = it.image,
                episode = it.episode,
                url = it.url,
                created = it.created
            )
        }
        characterDao.insertAll(characterEntities)
        Log.d("Repository", "Characters saved to database")
    }

    override suspend fun saveLocationsToDatabase(locations: List<Location>) {
        Log.d("Repository", "Saving ${locations.size} locations to database")
        locations.forEach { location ->
            Log.d("Repository", "Saving location to DB: $location")
        }
        val locationEntities = locations.map {
            LocationEntity(
                id = it.id,
                name = it.name,
                type = it.type,
                dimension = it.dimension,
                residents = it.residents
            )
        }
        locationDao.insertAll(locationEntities)
        Log.d("Repository", "Locations saved to database")
    }

    override suspend fun saveEpisodesToDatabase(episodes: List<Episode>) {
        Log.d("Repository", "Saving ${episodes.size} episodes to database")
        val episodeEntities = episodes.map { episode ->
            EpisodeEntity(
                id = episode.id,
                name = episode.name,
                episode = episode.episode,
                airDate = episode.airDate ?: "Unknown",
                characterUrls = episode.characters // Assuming `episode.characters` contains URLs
            )
        }
        episodeDao.insertAll(episodeEntities)
        Log.d("Repository", "Episodes saved to database")
    }

    // Getting Cached Data
    override suspend fun getCachedCharacters(): List<Character> {
        Log.d("Repository", "Fetching cached characters from database")
        val cachedCharacters = characterDao.getAllCharacters().map { characterEntity ->
            Log.d("Repository", "Cached character: ${characterEntity.name}, ${characterEntity.gender}, ${characterEntity.species}")
            Character(
                id = characterEntity.id,
                name = characterEntity.name,
                status = characterEntity.status,
                species = characterEntity.species,
                type = characterEntity.type,
                gender = characterEntity.gender,
                origin = Origin(
                    name = characterEntity.origin ?: "Unknown",
                    url = ""
                ),
                location = Location(
                    id = 0,
                    name = characterEntity.location ?: "Unknown",
                    type = "",
                    dimension = "",
                    residents = emptyList()
                ),
                image = characterEntity.image,
                episode = characterEntity.episode,
                url = characterEntity.url,
                created = characterEntity.created
            )
        }
        Log.d("Repository", "Cached ${cachedCharacters.size} characters fetched")
        return cachedCharacters
    }

    override suspend fun getCachedLocations(): List<Location> {
        Log.d("Repository", "Fetching cached locations from database")
        val cachedLocations = locationDao.getAllLocations().map { locationEntity ->
            Location(
                id = locationEntity.id,
                name = locationEntity.name,
                type = locationEntity.type,
                dimension = locationEntity.dimension,
                residents = locationEntity.residents // Ensure this matches the actual data
            )
        }
        Log.d("Repository", "Cached ${cachedLocations.size} locations fetched")
        return cachedLocations
    }

    override suspend fun getCachedEpisodes(): List<Episode> {
        Log.d("Repository", "Fetching cached episodes from database")
        val cachedEpisodes = episodeDao.getAllEpisodes().map { episodeEntity ->
            Episode(
                id = episodeEntity.id,
                name = episodeEntity.name,
                episode = episodeEntity.episode,
                airDate = episodeEntity.airDate,
                characters = episodeEntity.characterUrls // Retrieve character URLs
            )
        }
        Log.d("Repository", "Cached ${cachedEpisodes.size} episodes fetched")
        return cachedEpisodes
    }

    override suspend fun saveFilteredCharactersToDatabase(name: String, status: String, species: String, gender: String, characters: List<Character>) {
        Log.d("Repository", "Saving filtered characters to database: Name=$name, Status=$status, Species=$species, Gender=$gender")
        characters.forEach {
            Log.d("Repository", "Character to be saved: ${it.name}, ${it.status}, ${it.species}, ${it.gender}")
        }
        val characterEntities = characters.map {
            CharacterEntity(
                id = it.id,
                name = it.name,
                status = it.status,
                species = it.species,
                type = it.type,
                gender = it.gender,
                origin = it.origin.name ?: "Unknown",
                location = it.location.name ?: "Unknown",
                image = it.image,
                episode = it.episode,
                url = it.url,
                created = it.created
            )
        }
        characterDao.insertAll(characterEntities)
        Log.d("Repository", "Filtered characters saved to database")
    }


    // Fetching filtered data from database
    override suspend fun getFilteredCachedCharacters(name: String, status: String, species: String, gender: String): List<Character> {
        Log.d("Repository", "Fetching filtered cached characters: Name=$name, Status=$status, Species=$species, Gender=$gender")

        val characters = characterDao.getFilteredCharacters(name, status, species, gender)
        Log.d("Repository", "Filtered cached characters count: ${characters.size}")

        return characters.map { characterEntity ->
            Character(
                id = characterEntity.id,
                name = characterEntity.name,
                status = characterEntity.status,
                species = characterEntity.species,
                type = characterEntity.type,
                gender = characterEntity.gender,
                origin = Origin(
                    name = characterEntity.origin ?: "Unknown",
                    url = ""
                ),
                location = Location(
                    id = 0,
                    name = characterEntity.location ?: "Unknown",
                    type = "",
                    dimension = "",
                    residents = emptyList()
                ),
                image = characterEntity.image,
                episode = characterEntity.episode,
                url = characterEntity.url,
                created = characterEntity.created
            )
        }
    }
}