package com.example.musicplayer

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
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
    fun setPhoto(uri: Uri, context: Context) {
        photo = uri.toString()
        try {
            // Take persistable URI permission with read permission
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (e: Exception) {
            Log.e("Playlist", "Error taking permission: ${e.message}")
        }
    }
    fun setPhoto(string: String) { photo = string }

    fun getPhotoString(): String = photo
    fun setTracks(tracks: MutableList<Track>) { this.tracks = tracks }
    fun addTrack(track: Track) { tracks.add(track) }
    fun deleteTrack (track: Track) { tracks.remove(track) }
    fun deleteTrack (index: Int) { tracks.removeAt(index) }
    fun addAllTracks(tracks: MutableList<Track>) { this.tracks.addAll(tracks) }
    fun getTracks(): MutableList<Track> = tracks
}
