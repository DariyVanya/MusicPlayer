package com.example.musicplayer

import android.content.Context
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File

class PlaylistStorage(private val context: Context) {
    private val fileName = "playlists.json"

    private fun getFile(): File = File(context.filesDir, fileName)

    fun savePlaylists(playlists: List<Playlist>) {
        val json = Json.encodeToString(playlists)
        getFile().writeText(json)
    }

    fun clearFile(){
        getFile().delete()
    }

    fun loadPlaylists(): List<Playlist> {
        val file = getFile()
        if (!file.exists()) return emptyList()

        return try {
            Json.decodeFromString(file.readText())
        } catch (e: Exception) {
            emptyList()
        }
    }
}
