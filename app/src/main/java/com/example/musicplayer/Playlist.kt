package com.example.musicplayer

import android.net.Uri
import androidx.core.net.toUri

data class Playlist (
    var name:String,
    private var size:Int,
    private var tracks: MutableList<Track>,
    private var photo: Uri
)
{
    fun getSize():Int{
        return size
    }
    fun getPhoto():Uri{
        return photo
    }
    fun setPhoto(uri: Uri){
        photo = uri
    }
    fun setPhoto(string: String){
        photo = string.toUri()
    }
    fun getPhotoString():String{
        return photo.toString()
    }
    fun setTracks(tracks: MutableList<Track>){
        this.tracks = tracks
    }
    fun addTrack(track: Track){
        tracks.add(track)
    }
    fun addAllTracks(tracks: MutableList<Track>){
        this.tracks.addAll(tracks)
    }
    fun getTracks():MutableList<Track>{
        return tracks
    }
}