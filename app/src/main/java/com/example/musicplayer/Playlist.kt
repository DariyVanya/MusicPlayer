package com.example.musicplayer

import android.net.Uri
import androidx.core.net.toUri
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable
data class Playlist(
    var name: String,
    private var size: Int = 0,
    private var tracks: MutableList<Track> = mutableListOf(),
    private var photo: String = "android.resource://com.example.musicplayer/drawable/cover"
) {
    fun getSize(): Int = tracks.size
    fun getPhoto(): Uri = photo.toUri()
    fun setPhoto(uri: Uri) { photo = uri.toString() }
    fun setPhoto(string: String) { photo = string }
    fun getPhotoString(): String = photo
    fun setTracks(tracks: MutableList<Track>) { this.tracks = tracks }
    fun addTrack(track: Track) { tracks.add(track) }
    fun addAllTracks(tracks: MutableList<Track>) { this.tracks.addAll(tracks) }
    fun getTracks(): MutableList<Track> = tracks

}
