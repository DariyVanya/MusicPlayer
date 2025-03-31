package com.example.musicplayer

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import kotlinx.serialization.Serializable

class JamendoRepository {
    private val api: JamendoApi

    init {
        val interceptor = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
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

    suspend fun getPopularTracks(): List<Track> {
        return try {
            api.getPopularTracks().results?.map { track ->
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