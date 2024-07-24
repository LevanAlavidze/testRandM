package com.example.testrickmorty

import android.util.Log

class Repository(
    private val apiService: ApiService,
    private val characterDao: CharacterDao,
    private val locationDao: LocationDao,
    private val episodeDao: EpisodeDao
) {

    // Fetching from API
    suspend fun getCharacters(page: Int): List<Character> {
        Log.d("Repository", "Fetching characters from API for page: $page")
        val response = apiService.getCharacters(page)
        Log.d("Repository", "API Response: ${response.results.size} characters fetched")
        response.results.forEach { character ->
            Log.d(
                "Repository",
                "Character from API: ${character.name}, ${character.gender}, ${character.species}"
            )
        }
        return response.results
    }

    suspend fun getLocations(page: Int): List<Location> {
        Log.d("Repository", "Fetching locations from API for page: $page")
        val response = apiService.getLocations(page)
        Log.d("Repository", "API Response: ${response.results.size} locations fetched")
        return response.results
    }

    suspend fun getEpisodes(page: Int): List<Episode> {
        Log.d("Repository", "Fetching episodes from API for page: $page")
        val episodes = apiService.getEpisodes(page)
        Log.d("Repository", "API Response: ${episodes.results.size} episodes fetched")
        return episodes.results
    }

    suspend fun saveCharactersToDatabase(characters: List<Character>) {
        Log.d("Repository", "Saving ${characters.size} characters to database")
        characters.forEach { character ->
            Log.d(
                "Repository",
                "Saving character to DB: ${character.name}, ${character.gender}, ${character.species}"
            )
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

    suspend fun saveLocationsToDatabase(locations: List<Location>) {
        Log.d("Repository", "Saving ${locations.size} locations to database")
        val locationEntities = locations.map {
            LocationEntity(
                id = it.id,
                name = it.name,
                type = it.type,
                dimension = it.dimension
            )
        }
        locationDao.insertAll(locationEntities)
        Log.d("Repository", "Locations saved to database")
    }

    suspend fun saveEpisodesToDatabase(episodes: List<Episode>) {
        Log.d("Repository", "Saving ${episodes.size} episodes to database")
        val episodeEntities = episodes.map { episode ->
            EpisodeEntity(
                id = episode.id,
                name = episode.name,
                episode = episode.episode,
                airDate = episode.airDate ?: "Unknown"
            )
        }
        episodeDao.insertAll(episodeEntities)
        Log.d("Repository", "Episodes saved to database")
    }

    // Getting Cached Data
    suspend fun getCachedCharacters(): List<Character> {
        Log.d("Repository", "Fetching cached characters from database")
        val cachedCharacters = characterDao.getAllCharacters().map { characterEntity ->
            Log.d(
                "Repository",
                "Cached character: ${characterEntity.name}, ${characterEntity.gender}, ${characterEntity.species}"
            )
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
                    residents = emptyList() // Add this line
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

    suspend fun getCachedLocations(): List<Location> {
        Log.d("Repository", "Fetching cached locations from database")
        val cachedLocations = locationDao.getAllLocations().map { locationEntity ->
            Location(
                id = locationEntity.id,
                name = locationEntity.name,
                type = locationEntity.type,
                dimension = locationEntity.dimension,
                residents = emptyList() // Provide an empty list here
            )
        }
        Log.d("Repository", "Cached ${cachedLocations.size} locations fetched")
        return cachedLocations
    }

    suspend fun getCachedEpisodes(): List<Episode> {
        Log.d("Repository", "Fetching cached episodes from database")
        val cachedEpisodes = episodeDao.getAllEpisodes().map { episodeEntity ->
            Episode(
                id = episodeEntity.id,
                name = episodeEntity.name,
                episode = episodeEntity.episode,
                airDate = episodeEntity.airDate,
                characters = emptyList()
            )
        }
        Log.d("Repository", "Cached ${cachedEpisodes.size} episodes fetched")
        return cachedEpisodes
    }

    suspend fun getCharacter(characterId: Int): Character {
        val response = apiService.getCharacter(characterId)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Character not found")
        } else {
            throw Exception("Failed to fetch character details")
        }
    }
    suspend fun getLocation(locationId: Int): Location {
        val response = apiService.getLocation(locationId)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Location not found")
        } else {
            throw Exception("Failed to fetch location details")
        }
    }
    suspend fun getCharactersByUrls(urls: List<String>): List<Character> {
        val characters = mutableListOf<Character>()
        for (url in urls) {
            // Fetch each character by its URL
            try {
                val character = apiService.getCharacterByUrl(url)
                characters.add(character)
            } catch (e: Exception) {
                // Handle exceptions (e.g., log error or continue with the next URL)
            }
        }
        return characters
    }
    suspend fun getEpisode(episodeId: Int): Episode {
        val response = apiService.getEpisode(episodeId)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Episode not found")
        } else {
            throw Exception("Failed to fetch episode details")
        }
    }
    suspend fun getEpisodesByUrls(urls: List<String>): List<Episode> {
        return urls.mapNotNull { url ->
            try {
                val episodeId = url.substringAfterLast("/").toIntOrNull()
                episodeId?.let { getEpisode(it) }
            } catch (e: Exception) {
                null
            }
        }
    }


    suspend fun searchEpisodes(query: String): EpisodeResponse {
        return apiService.searchEpisodes(query)
    }

    suspend fun searchCharacters(query: String): CharacterResponse {
        return apiService.searchCharacters(query)
    }
    suspend fun filterEpisodes(season: String): EpisodeResponse {
        return apiService.filterEpisodes(season)
    }
    suspend fun searchLocations(query: String): LocationResponse {
        return apiService.searchLocations(query)
    }

}