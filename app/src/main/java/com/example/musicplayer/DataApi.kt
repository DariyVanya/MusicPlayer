package com.example.musicplayer

data class JamendoTrackResponse(val results: List<JamendoTrack>?)
data class JamendoTrack(
    val id: Long,
    val name: String,
    val artist_name: String,
    val album_image: String?,
    val audio: String,
    val musicinfo: MusicInfo?
)
data class MusicInfo(val tags: Tags?)
data class Tags(val genres: List<String>?)
