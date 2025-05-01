package com.example.musicplayer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import com.bumptech.glide.Glide

@UnstableApi class MediaService : Service() {
    private val binder = LocalBinder()
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var notificationManager: NotificationManagerCompat
    private var player: Player? = null

    companion object {
        private const val CHANNEL_ID = "media_playback_channel"
        private const val NOTIFICATION_ID = 1
        private const val REQUEST_CODE = 100
    }

    inner class LocalBinder : Binder() {
        fun getService(): MediaService = this@MediaService
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        mediaSession = MediaSessionCompat(this, "MusicPlayer").apply {
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() {
                    player?.play()
                    updateNotification()
                }

                override fun onPause() {
                    player?.pause()
                    updateNotification()
                }

                override fun onSkipToNext() {
                    player?.next()
                    updateNotification()
                }

                override fun onSkipToPrevious() {
                    player?.prev()
                    updateNotification()
                }
            })
            isActive = true
        }

        notificationManager = NotificationManagerCompat.from(this)
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    fun setPlayer(player: Player) {
        this.player = player
        updateNotification()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Media Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Media playback controls"
                setShowBadge(false)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun updateNotification() {
        player?.let { player ->
            val currentTrack = player.getTrack()

            val mainIntent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            val mainPendingIntent = PendingIntent.getActivity(
                this, REQUEST_CODE, mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val playPauseIcon = if (player.isPlaying()) {
                R.drawable.baseline_pause_24
            } else {
                R.drawable.baseline_play_arrow_24
            }

            val playPauseIntent = Intent(this, MediaReceiver::class.java).apply {
                action = if (player.isPlaying()) MediaReceiver.ACTION_PAUSE else MediaReceiver.ACTION_PLAY
            }
            val playPausePendingIntent = PendingIntent.getBroadcast(
                this, 0, playPauseIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val prevIntent = Intent(this, MediaReceiver::class.java).apply {
                action = MediaReceiver.ACTION_PREV
            }
            val prevPendingIntent = PendingIntent.getBroadcast(
                this, 1, prevIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val nextIntent = Intent(this, MediaReceiver::class.java).apply {
                action = MediaReceiver.ACTION_NEXT
            }
            val nextPendingIntent = PendingIntent.getBroadcast(
                this, 2, nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                //.setSmallIcon(R.drawable.ic_music_note)
                .setContentTitle(currentTrack.name)
                .setContentText(currentTrack.artist)
                .setContentIntent(mainPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOnlyAlertOnce(true)
                .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2))
                .addAction(R.drawable.baseline_skip_previous_24, "Previous", prevPendingIntent)
                .addAction(playPauseIcon, "Play/Pause", playPausePendingIntent)
                .addAction(R.drawable.baseline_skip_next_24, "Next", nextPendingIntent)
                .setOngoing(player.isPlaying())

            try {
                val bitmap = Glide.with(this)
                    .asBitmap()
                    .load(currentTrack.photo)
                    .submit()
                    .get()
                builder.setLargeIcon(bitmap)
            } catch (e: Exception) {
                // Handle error loading image
            }

            mediaSession.setMetadata(MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, currentTrack.name)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, currentTrack.artist)
                .build())

            mediaSession.setPlaybackState(PlaybackStateCompat.Builder()
                .setState(
                    if (player.isPlaying()) PlaybackStateCompat.STATE_PLAYING
                    else PlaybackStateCompat.STATE_PAUSED,
                    PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                    1f
                )
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY or
                            PlaybackStateCompat.ACTION_PAUSE or
                            PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                )
                .build())

            startForeground(NOTIFICATION_ID, builder.build())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaSession.release()
    }
}