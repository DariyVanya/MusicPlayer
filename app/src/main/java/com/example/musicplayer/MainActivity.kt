package com.example.musicplayer


import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity(){

    lateinit var runnable: Runnable
    private var handler = Handler()



    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.player_fs)

        //Creating a mediaPlayer for music.mp3
        val mediaPlayer: MediaPlayer = MediaPlayer.create(this, R.raw.music)

        //seekBar
        val seekBar: SeekBar = findViewById(R.id.soundtrack_seekBar)
        seekBar.progress = 0
        seekBar.max = mediaPlayer.duration

        //Current and max duration textView
        val currentText: TextView = findViewById(R.id.progress_current)
        val maxText: TextView = findViewById(R.id.progress_max)

        // play listener
        val playBtn: ImageButton = findViewById(R.id.play_btn)



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
        }
        handler.postDelayed(runnable, 1000)

        mediaPlayer.setOnCompletionListener {
            playBtn.setImageResource(R.drawable.baseline_play_arrow_24)
            seekBar.progress = 0
        }


    }
}