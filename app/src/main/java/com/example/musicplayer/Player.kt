package com.example.musicplayer

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.widget.ImageButton
import com.bumptech.glide.Glide
import com.example.musicplayer.databinding.PlayerFsBinding
import com.example.musicplayer.databinding.MainBinding
import java.io.File

class Player(
    private var context: Context,
    var mediaPlayer: MediaPlayer =MediaPlayer.create(context, R.raw.music),
    private var prevTracks: MutableList<Track> = mutableListOf<Track>(),
    private var trackPlaying: Track = Track(),
    private var nextTracks: MutableList<Track> = mutableListOf<Track>(),
    var state: String = "Stopped",
    var mainBinding: MainBinding,
    var playerFsBinding: PlayerFsBinding
)
{

    fun stop(){
        mediaPlayer.stop()
        mediaPlayer = MediaPlayer()
        trackPlaying = Track()
        nextTracks = mutableListOf()
        prevTracks = mutableListOf()
        state = "Stopped"
        var playBtn: ImageButton = mainBinding.playBtn
        playBtn.setImageResource(R.drawable.baseline_play_arrow_24)
        mainBinding.nowPlayingName.text = "Not playing..."
        Glide.with(context).load(R.drawable.cover)
            .error(R.drawable.cover)
            .placeholder(R.drawable.cover).into(mainBinding.nowPlayingImage)

        playBtn = playerFsBinding.playBtn
        playBtn.setImageResource(R.drawable.baseline_play_arrow_24)
        playerFsBinding.soundtrackTitle.text = "Not playing..."
        Glide.with(context).load(R.drawable.cover)
            .error(R.drawable.cover)
            .placeholder(R.drawable.cover).into(playerFsBinding.soundtrackCoverImg)

        playerFsBinding.progressMax.text = "00:00"
        playerFsBinding.progressCurrent.text = "00:00"
    }

    fun play(){
        mediaPlayer.start()
        state = "Playing"
    }
    fun pause(){
        mediaPlayer.pause()
        state = "Stopped"
    }
    fun seekTo(progress: Int){ mediaPlayer.seekTo(progress) }

    fun play(track: Track){
        mediaPlayer.stop()
        val file = File(track.uri)
        mediaPlayer = MediaPlayer.create(context, Uri.parse(track.uri))
        trackPlaying = track
        play()

        var playBtn: ImageButton = mainBinding.playBtn
        playBtn.setImageResource(R.drawable.baseline_pause_24)
        play()
        mainBinding.nowPlayingName.text = track.name
        Glide.with(context).load(track.photo)
            .error(R.drawable.cover)
            .placeholder(R.drawable.cover).into(mainBinding.nowPlayingImage)

        playBtn = playerFsBinding.playBtn
        playBtn.setImageResource(R.drawable.baseline_pause_24)
        playerFsBinding.soundtrackTitle.text = track.name
        Glide.with(context).load(track.photo)
            .error(R.drawable.cover)
            .placeholder(R.drawable.cover).into(playerFsBinding.soundtrackCoverImg)


        playerFsBinding.soundtrackSeekBar.progress = getProgress()
        playerFsBinding.soundtrackSeekBar.max = getDuration()

    }
    fun shuffle(){
        TODO()
    }
    fun loop(){ mediaPlayer.isLooping = true }
    fun loopOne(){
        mediaPlayer.isLooping = true
        TODO()
    }
    fun unloop(){ mediaPlayer.isLooping = false }

    fun getTrack():Track{ return trackPlaying }
    fun getProgress():Int{ return mediaPlayer.currentPosition }
    fun getDuration():Int { return mediaPlayer.duration }
    fun isPlaying():Boolean {if (state == "Playing"){return true}else{return false}}

    fun setOnComplete(){
        mediaPlayer.setOnCompletionListener {
            next()
        }
    }

    fun setNext(track: Track){
        nextTracks.add(0, track)
        setOnComplete()
    }
    fun setNext(tracks: MutableList<Track>){
        nextTracks.clear()
        nextTracks.addAll(tracks)
        setOnComplete()
    }

    fun next(){
        if (nextTracks.size == 0){
            trackPlaying = Track()
            stop()
        } else {
            setPrev(trackPlaying)
            Log.d("test", prevTracks.toString())

            play(nextTracks[0])
            nextTracks.removeAt(0)

            setOnComplete()
        }

    }

    fun setPrev(track: Track){
        prevTracks.add(0, track)
    }
    fun prev(){
        if (prevTracks.size == 0){
            mediaPlayer.seekTo(0)
        } else if (mediaPlayer.currentPosition >= 3000){
            mediaPlayer.seekTo(0)
        } else if (mediaPlayer.currentPosition <= 3000 && prevTracks.size != 0){
            setNext(trackPlaying)
            play(prevTracks[0])
            prevTracks.removeAt(0)
        }
    }


}
