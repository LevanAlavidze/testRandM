package com.example.testrickmorty.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.testrickmorty.feature.characters.models.Character
import com.example.testrickmorty.feature.characters.models.CharacterResponse
import com.example.testrickmorty.feature.episodes.models.Episode
import com.example.testrickmorty.feature.episodes.models.EpisodeResponse
import com.example.testrickmorty.feature.locations.models.Location
import com.example.testrickmorty.feature.locations.models.LocationResponse
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url
import java.io.File

interface ApiService {
    @GET("character")
    suspend fun getCharacters(@Query("page") page: Int): CharacterResponse

    @GET("location")
    suspend fun getLocations(@Query("page") page: Int): LocationResponse

    @GET("episode")
    suspend fun getEpisodes(@Query("page") page: Int): EpisodeResponse

    @GET("character/{id}")

    suspend fun getCharacter(@Path("id") id: Int): Response<Character>

    @GET("location/{id}")
    suspend fun getLocation(@Path("id") locationId: Int): Response<Location>

    @GET
    suspend fun getCharacterByUrl(@Url url: String): Response<Character>

    @GET("character")
    suspend fun getCharactersByPage(@Query("page") page: Int): List<Character>

    @GET("episode/{id}")
    suspend fun getEpisode(@Path("id") episodeId: Int): Response<Episode>

    @GET("episode")
    suspend fun searchEpisodes(@Query("name") query: String): EpisodeResponse

    @GET("episode")
    suspend fun filterEpisodes(@Query("episode") season: String): EpisodeResponse

    @GET("character")
    suspend fun searchCharacters(@Query("name") query: String): CharacterResponse

    @GET("location")
    suspend fun searchLocations(@Query("name") query: String): LocationResponse

    @GET("character")
    suspend fun getFilteredCharacters(
        @Query("name") name: String,
        @Query("status") status: String,
        @Query("gender") gender: String,
        @Query("species") species: String,
        @Query("page") page: Int
    ): CharacterResponse

    @GET("location")
    suspend fun getFilteredLocations(
        @Query("name") name: String,
        @Query("type") type: String,
        @Query("dimension") dimension: String,
        @Query("page") page: Int
    ): LocationResponse

    @GET("episode")
    suspend fun getFilteredEpisodes(
        @Query("name") name: String,
        @Query("episode") episode: String,
        @Query("page") page: Int
    ): EpisodeResponse


    companion object {
        private const val BASE_URL = "https://rickandmortyapi.com/api/"

        fun create(context: Context): ApiService {
            val cacheDir = File(context.cacheDir, "http_cache")
            val cacheSize = 10 * 1024 * 1024 // 10 MB
            val cache = Cache(cacheDir, cacheSize.toLong())

            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val okHttpClient = OkHttpClient.Builder()
                .cache(cache)
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder().apply {
                        if (hasNetwork(context)) {
                            header("Cache-Control", "public, max-age=" + 5)
                        } else {
                            header(
                                "Cache-Control",
                                "public, only-if-cached, max-stale=" + 60 * 60 * 24 * 7
                            )
                        }
                    }.build()
                    chain.proceed(request)
                }
                .addInterceptor(loggingInterceptor) // Add the logging interceptor here
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }

        private fun hasNetwork(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            return capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(
                NetworkCapabilities.TRANSPORT_CELLULAR
            ))
        }
    }
}