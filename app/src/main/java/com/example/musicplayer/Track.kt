package com.example.musicplayer

import android.net.Uri
import androidx.core.net.toUri

import kotlinx.serialization.Serializable

@Serializable
data class Track(
    val id: Long = 0,
    val name: String = "Default",
    val genre: String = "Default",
    val artist: String = "Default",
    val photo: String = "android.resource://",
    val uri: String = "Default"
)
