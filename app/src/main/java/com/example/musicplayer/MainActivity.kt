package com.example.musicplayer


import android.annotation.SuppressLint
import android.media.Image
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.w3c.dom.Text
import java.sql.Time
import java.util.concurrent.TimeUnit
import kotlin.time.toDuration


class MainActivity : AppCompatActivity(){

    lateinit var runnable: Runnable
    private var handler = Handler()

    fun getTimeDuration(time:Int) : String{
        val res: String = String.format("%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(time.toLong()),
            TimeUnit.MILLISECONDS.toSeconds(time.toLong()) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time.toLong())))
        return res
    }

    fun goToPlayer(mediaPlayer: MediaPlayer){
        setContentView(R.layout.player_fs)


        //seekBar
        val seekBar: SeekBar = findViewById(R.id.soundtrack_seekBar)
        seekBar.progress = mediaPlayer.currentPosition
        seekBar.max = mediaPlayer.duration

        //Current and max duration textView
        val currentText: TextView = findViewById(R.id.progress_current)
        val maxText: TextView = findViewById(R.id.progress_max)
        currentText.text = getTimeDuration(mediaPlayer.currentPosition)

        maxText.text = getTimeDuration(mediaPlayer.duration)


        // play listener
        val playBtn: ImageButton = findViewById(R.id.play_btn)
        val backBtn: ImageButton = findViewById(R.id.go_back_btn)


        if (mediaPlayer.isPlaying){
            playBtn.setImageResource(R.drawable.baseline_pause_24)
        } else {
            playBtn.setImageResource(R.drawable.baseline_play_arrow_24)
        }

        backBtn.setOnClickListener{
            goToPlaylist(mediaPlayer)
        }

        playBtn.setOnClickListener{
            if (!mediaPlayer.isPlaying) {
                mediaPlayer.start()
                playBtn.setImageResource(R.drawable.baseline_pause_24)

            } else {
                mediaPlayer.pause()
                playBtn.setImageResource(R.drawable.baseline_play_arrow_24)
            }
        }
        seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, changed: Boolean) {
                if (changed){
                    mediaPlayer.seekTo (progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })
        runnable = Runnable {
            seekBar.progress = mediaPlayer.currentPosition
            handler.postDelayed (runnable, 1000)
            currentText.text = getTimeDuration(mediaPlayer.currentPosition)
        }
        handler.postDelayed(runnable, 1000)

        mediaPlayer.setOnCompletionListener {
            mediaPlayer.seekTo (0)
            playBtn.setImageResource(R.drawable.baseline_play_arrow_24)
            seekBar.progress = 0
            currentText.text = "00:00"
            maxText.text = "00:00"
        }
    }

    fun goToPlaylist(mediaPlayer: MediaPlayer) {
        setContentView(R.layout.playlist)
        val playBtn: ImageButton = findViewById(R.id.play_btn_playlist)
        if (mediaPlayer.isPlaying){
            playBtn.setImageResource(R.drawable.baseline_pause_24)
        } else {
            playBtn.setImageResource(R.drawable.baseline_play_arrow_24)
        }

        playBtn.setOnClickListener{
            if (!mediaPlayer.isPlaying) {
                mediaPlayer.start()
                playBtn.setImageResource(R.drawable.baseline_pause_24)

            } else {
                mediaPlayer.pause()
                playBtn.setImageResource(R.drawable.baseline_play_arrow_24)
            }
        }

        val imageView: ImageView = findViewById(R.id.soundtrack_cover_img_playlist)
        val titleText: TextView = findViewById(R.id.soundtrack_name)

        imageView.setOnClickListener{
            goToPlayer(mediaPlayer)
        }

        titleText.setOnClickListener{
            goToPlayer(mediaPlayer)
        }

    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mediaPlayer: MediaPlayer = MediaPlayer.create(this, R.raw.music)
        goToPlaylist(mediaPlayer)

    }
}