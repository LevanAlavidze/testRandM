package com.example.testrickmorty

data class CharacterResponse(
    val info: Info,
    val results: List<Character>
)
data class Character(
    val id: Int,
    val name: String,
    val status: String,
    val species: String,
    val type: String,
    val gender: String,
    val origin: Origin,
    val location: Location,
    val image: String,
    val episode: List<String>,
    val url: String,
    val created: String
) {
      // Extract ID from URL for origin
    fun getOriginId(): Int? {
        return origin.url.substringAfterLast("/").toIntOrNull()
    }
}

data class Origin(
    val name: String,
    val url: String
)