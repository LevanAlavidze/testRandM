package com.example.testrickmorty


data class EpisodeResponse(
    val info: Info,
    val results: List<Episode>
)
data class Episode(
    val id: Int,
    val name: String,
    val episode: String,
    val airDate: String?
)

data class Info(
    val count: Int,
    val pages: Int,
    val next: String?,
    val prev: String?
)