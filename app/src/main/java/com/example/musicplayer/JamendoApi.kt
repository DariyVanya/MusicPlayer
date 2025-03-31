package com.example.musicplayer

import retrofit2.http.GET
import retrofit2.http.Query

// Retrofit API interface
interface JamendoApi {
    @GET("tracks")
    suspend fun getTracks(
        @Query("client_id") clientId: String = "ecb07a90",
        @Query("format") format: String = "json",
        @Query("search") query: String,
        @Query("limit") limit: Int = 20
    ): JamendoTrackResponse

    @GET("tracks")
    suspend fun getPopularTracks(
        @Query("client_id") clientId: String = "ecb07a90",
        @Query("format") format: String = "json",
        @Query("order") order: String = "popularity_total",
        @Query("limit") limit: Int = 20
    ): JamendoTrackResponse
}
