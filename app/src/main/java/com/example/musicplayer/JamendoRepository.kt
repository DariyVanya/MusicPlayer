package com.example.musicplayer

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class JamendoRepository {

    private val api: JamendoApi

    init {
        val interceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.jamendo.com/v3.0/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(JamendoApi::class.java)
    }

    suspend fun getTracks(query: String): List<Track> {
        return try {
            api.getTracks(query = query).results?.map { track ->
                Track(
                    id = track.id,
                    name = track.name,
                    artist = track.artist_name,
                    genre = track.musicinfo?.tags?.genres?.firstOrNull() ?: "Unknown",
                    photo = track.album_image ?: "android.resource://",
                    uri = track.audio
                )
            } ?: emptyList()
        } catch (e: Exception) {
            Log.e("Jamendo", "Error fetching tracks", e)
            emptyList()
        }
    }

    suspend fun getPopularTracks(page: Int = 1): List<Track> {
        return try {
            api.getPopularTracks(page = page).results?.map { track ->
                Track(
                    id = track.id,
                    name = track.name,
                    artist = track.artist_name,
                    genre = track.musicinfo?.tags?.genres?.firstOrNull() ?: "Unknown",
                    photo = track.album_image ?: "android.resource://",
                    uri = track.audio
                )
            } ?: emptyList()
        } catch (e: Exception) {
            Log.e("Jamendo", "Error fetching popular tracks", e)
            emptyList()
        }
    }
}