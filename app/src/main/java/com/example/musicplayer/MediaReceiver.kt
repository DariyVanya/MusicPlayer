package com.example.musicplayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class MediaReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_PLAY = "com.example.musicplayer.PLAY"
        const val ACTION_PAUSE = "com.example.musicplayer.PAUSE"
        const val ACTION_PREV = "com.example.musicplayer.PREV"
        const val ACTION_NEXT = "com.example.musicplayer.NEXT"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val mediaService = (context.applicationContext as? MainActivity)?.mediaService

        when (intent.action) {
            ACTION_PLAY -> mediaService?.player?.play()
            ACTION_PAUSE -> mediaService?.player?.pause()
            ACTION_PREV -> mediaService?.player?.prev()
            ACTION_NEXT -> mediaService?.player?.next()
        }

        mediaService?.updateNotification()
    }
}