package com.example.musicplayer

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.widget.ImageButton
import com.bumptech.glide.Glide
import com.example.musicplayer.databinding.CreatePlaylistBinding
import com.example.musicplayer.databinding.PlayerFsBinding
import com.example.musicplayer.databinding.MainBinding
import com.example.musicplayer.databinding.PlaylistBinding
import com.example.musicplayer.databinding.SearchBinding
import java.io.File

class Player(
    private var context: Context,
    var mediaPlayer: MediaPlayer = MediaPlayer(),
    private var prevTracks: MutableList<Track> = mutableListOf<Track>(),
    private var trackPlaying: Track = Track(),
    private var nextTracks: MutableList<Track> = mutableListOf<Track>(),
    private var copyTracks: MutableList<Track> = mutableListOf<Track>(),
    var state: String = "Stopped",
    var mainBinding: MainBinding,
    var playerFsBinding: PlayerFsBinding,
    var playlistBinding: PlaylistBinding,
    var searchBinding: SearchBinding,
    var isLooping:Int = 0
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

        playBtn = playlistBinding.playBtn
        playBtn.setImageResource(R.drawable.baseline_play_arrow_24)
        playlistBinding.nowPlayingName.text = "Not playing..."
        Glide.with(context).load(R.drawable.cover)
            .error(R.drawable.cover)
            .placeholder(R.drawable.cover).into(playlistBinding.nowPlayingImage)

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
        state = "Paused"
    }
    fun seekTo(progress: Int){ mediaPlayer.seekTo(progress) }

    fun play(track: Track){
        mediaPlayer.stop()
        mediaPlayer = MediaPlayer.create(context, Uri.parse(track.uri))
        trackPlaying = track

        var playBtn: ImageButton = mainBinding.playBtn
        playBtn.setImageResource(R.drawable.baseline_pause_24)

        mainBinding.nowPlayingName.text = track.name
        Glide.with(context).load(track.photo)
            .error(R.drawable.cover)
            .placeholder(R.drawable.cover).into(mainBinding.nowPlayingImage)

        playBtn = playlistBinding.playBtn
        playBtn.setImageResource(R.drawable.baseline_pause_24)

        playlistBinding.nowPlayingName.text = track.name
        Glide.with(context).load(track.photo)
            .error(R.drawable.cover)
            .placeholder(R.drawable.cover).into(playlistBinding.nowPlayingImage)

        playBtn = searchBinding.playBtn
        playBtn.setImageResource(R.drawable.baseline_pause_24)

        searchBinding.nowPlayingName.text = track.name
        Glide.with(context).load(track.photo)
            .error(R.drawable.cover)
            .placeholder(R.drawable.cover).into(searchBinding.nowPlayingImage)

        playBtn = playerFsBinding.playBtn
        playBtn.setImageResource(R.drawable.baseline_pause_24)
        playerFsBinding.soundtrackTitle.text = track.name
        Glide.with(context).load(track.photo)
            .error(R.drawable.cover)
            .placeholder(R.drawable.cover).into(playerFsBinding.soundtrackCoverImg)


        playerFsBinding.soundtrackSeekBar.progress = getProgress()
        playerFsBinding.soundtrackSeekBar.max = getDuration()
        setOnComplete()
        play()

    }

    fun shuffle(){
        copyTracks = nextTracks
        nextTracks.shuffle()
    }

    fun unshuffle(){
        nextTracks = copyTracks
        copyTracks.clear()
    }

    fun loop(){ isLooping = 1 }
    fun loopOne(){ isLooping = 2 }
    fun unloop(){ isLooping = 0 }

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
        if (isLooping == 2){
            mediaPlayer.seekTo(0)
            play()
        } else {
            if (nextTracks.size == 0){
                if (isLooping == 0){
                    trackPlaying = Track()
                    stop()
                } else if (isLooping == 1){
                    setPrev(trackPlaying)
                    nextTracks.addAll(prevTracks.reversed())
                    prevTracks.clear()
                    if (nextTracks.isNotEmpty()) {
                        play(nextTracks[0])
                        nextTracks.removeAt(0)
                    }
                }
            } else {
                setPrev(trackPlaying)
                play(nextTracks[0])
                nextTracks.removeAt(0)
                setOnComplete()
            }
        }
    }

    fun setPrev(track: Track){
        prevTracks.add(0, track)
    }

    fun setPrev(tracks: MutableList<Track>){
        prevTracks.clear()
        prevTracks.addAll(tracks)
    }

    fun prev(){
        if (prevTracks.isEmpty()){
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
