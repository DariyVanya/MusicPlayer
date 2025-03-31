package com.example.musicplayer

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayer.databinding.PlayerFsBinding
import com.example.musicplayer.databinding.MainBinding
import java.util.concurrent.TimeUnit
import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.musicplayer.databinding.CreatePlaylistBinding
import com.example.musicplayer.databinding.PlaylistBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.internal.notify
import java.io.File

class MainActivity : AppCompatActivity(){

    lateinit var runnable: Runnable
    private var handler = Handler()
    lateinit var seekBar: SeekBar
    lateinit var currentText: TextView
    lateinit var maxText: TextView
    lateinit var playBtn: ImageButton
    lateinit var nextBtn: ImageButton
    lateinit var prevBtn: ImageButton
    lateinit var backBtn: ImageButton
    lateinit var btnTest : Button
    lateinit var imageView: ImageView
    lateinit var titleText: TextView

    lateinit var player:Player
    lateinit var linearLayout: LinearLayout

    lateinit var mainBinding: MainBinding
    lateinit var playlistBinding: PlaylistBinding
    lateinit var playerLayout: PlayerFsBinding
    lateinit var createPlaylistBinding: CreatePlaylistBinding

    lateinit private var trackAdapter: TrackAdapter
    lateinit private var playlistAdapter: PlaylistAdapter
    private lateinit var repository: JamendoRepository
    private lateinit var playlistStorage: PlaylistStorage

    fun getTimeDuration(time:Int) : String{
        val res: String = String.format("%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(time.toLong()),
            TimeUnit.MILLISECONDS.toSeconds(time.toLong()) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time.toLong())))
        return res
    }

    fun goToPlayer(){

        setContentView(playerLayout.root)

        seekBar = playerLayout.soundtrackSeekBar
        //Current and max duration textView
        currentText = playerLayout.progressCurrent
        maxText = playerLayout.progressMax

        currentText.text = getTimeDuration(player.getProgress())
        maxText.text = getTimeDuration(player.getDuration())

//        //btnTest
//        btnTest = findViewById(R.id.button_test)
//        btnTest.setOnClickListener{
//            mediaPlayer = MediaPlayer.create(this, R.raw.music)
//            mediaPlayer.start()
//        }

        // play listener
        playBtn = playerLayout.playBtn
        backBtn = playerLayout.goBackBtn
        nextBtn = playerLayout.nextBtn
        prevBtn = playerLayout.prevBtn

        if (player.isPlaying()){
            playBtn.setImageResource(R.drawable.baseline_pause_24)
        } else {
            playBtn.setImageResource(R.drawable.baseline_play_arrow_24)
        }

        backBtn.setOnClickListener{
            goToMain()
        }
        nextBtn.setOnClickListener{
            player.next()
        }
        prevBtn.setOnClickListener{
            player.prev()
        }

        playBtn.setOnClickListener{
            Log.d("test", player.isPlaying().toString())
            if (!player.isPlaying()){
                player.play()
                playBtn.setImageResource(R.drawable.baseline_pause_24)
            } else {
                player.pause()
                playBtn.setImageResource(R.drawable.baseline_play_arrow_24)
            }
        }

        seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, changed: Boolean) {
                if (changed){
                    player.seekTo (progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })
        runnable = Runnable {
            seekBar.progress = player.getProgress()
            handler.postDelayed (runnable, 1000)
            currentText.text = getTimeDuration(player.getProgress())
            maxText.text = getTimeDuration(player.getDuration())
        }
        handler.postDelayed(runnable, 1000)


    }
    fun goToCreatePlaylist(){
        setContentView(createPlaylistBinding.root)

        createPlaylistBinding.createBtn.setOnClickListener{
            val newPlaylist = Playlist(
                name = createPlaylistBinding.playlistNameTxt.text.toString(),
                tracks = mutableListOf()
            )
            val playlists = playlistStorage.loadPlaylists().toMutableList()
            playlists.add(newPlaylist)
            playlistStorage.savePlaylists(playlists)
            goToPlaylist()
        }
        createPlaylistBinding.cancelButton.setOnClickListener{
            goToPlaylist()
        }
    }

    fun goToPlaylist(){
        setContentView(playlistBinding.root)
        var tracks:MutableList<Track> = mutableListOf<Track>()

        playlistStorage = PlaylistStorage(this)

        lifecycleScope.launch {
            // Завантажуємо всі плейлисти і логуємо їх
            val loadedPlaylists = playlistStorage.loadPlaylists()
            Log.d("PlaylistStorage", "Loaded playlists: $loadedPlaylists")
            playlistAdapter.setPlaylistList(loadedPlaylists)

        }

        playBtn = playlistBinding.playBtn
        linearLayout = playlistBinding.linearLayout

        if (player.isPlaying()){
            playBtn.setImageResource(R.drawable.baseline_pause_24)
        } else {
            playBtn.setImageResource(R.drawable.baseline_play_arrow_24)
        }
        playBtn.setOnClickListener{
            if (!player.isPlaying()) {
                player.play()
                playBtn.setImageResource(R.drawable.baseline_pause_24)
            } else {
                player.pause()
                playBtn.setImageResource(R.drawable.baseline_play_arrow_24)
            }
        }

        if (player.state == "Stopped"){
            playlistBinding.nowPlayingMenu.isVisible = false;
        }

        playlistBinding.createNewBtn.setOnClickListener{
            goToCreatePlaylist()
        }

        playlistBinding.mainBtn.setOnClickListener{
            goToMain()
        }

        imageView = playlistBinding.nowPlayingImage
        titleText = playlistBinding.nowPlayingName

        imageView.setOnClickListener{
            goToPlayer()
        }

        titleText.setOnClickListener{
            goToPlayer()
        }
    }


    fun goToMain() {
        setContentView(mainBinding.root)
        init()

//        val faker = Faker.instance()
//        val tracks: List<Track> = (1..10).map {
//            Track(
//                id = it.toLong(),
//                name = faker.music().genre(),
//                genre = faker.music().genre(),
//                artist = faker.name().username(),
//                photo = IMAGES[it % IMAGES.size],
//                uri = TRACK
//            )
//        }
        //trackAdapter.setTrackList(getFilesFromDirectory((Environment.getExternalStorageDirectory().path + "/" +Environment.DIRECTORY_DOWNLOADS).toUri()))
        lifecycleScope.launch {
            val popularTracks = repository.getPopularTracks()
            trackAdapter.setTrackList(popularTracks);
            popularTracks.forEach { track ->
                Log.d("Jamendo", "Popular Track: $track")
            }
        }

        playBtn = mainBinding.playBtn
        linearLayout = mainBinding.linearLayout

        if (player.isPlaying()){
            playBtn.setImageResource(R.drawable.baseline_pause_24)
        } else {
            playBtn.setImageResource(R.drawable.baseline_play_arrow_24)
        }
        if (player.state == "Stopped"){
            mainBinding.nowPlayingMenu.isVisible = false;
        }

        playBtn.setOnClickListener{
            if (!player.isPlaying()) {
                player.play()
                playBtn.setImageResource(R.drawable.baseline_pause_24)
            } else {
                player.pause()
                playBtn.setImageResource(R.drawable.baseline_play_arrow_24)
            }
        }
        mainBinding.playlistBtn.setOnClickListener{
            goToPlaylist()
        }


        imageView = mainBinding.nowPlayingImage
        titleText = mainBinding.nowPlayingName

        imageView.setOnClickListener{
            goToPlayer()
        }

        titleText.setOnClickListener{
            goToPlayer()
        }

    }

    fun getFilesFromDirectory(uri: Uri):MutableList<Track>{
        val directory = File(uri.toString())
        var tracks: MutableList<Track> = mutableListOf<Track>()
        var listOfFiles = directory.listFiles()?.filter{ it.isFile && (it.extension=="mp3" || it.extension=="m4p" || it.extension=="mp4")}
        listOfFiles?.forEachIndexed(){
            i, file->
            tracks.add(Track(
                id = (i+1).toLong(),
                name = file.name,
                artist = "Unknown",
                genre = "Unknown",
                uri = file.path,
                photo = IMAGES[i % IMAGES.size]))
        }
        return tracks
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Define a requestPermessionLauncher using the RequestPermission contract
        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
                    mainBinding = MainBinding.inflate(layoutInflater)
                    playerLayout = PlayerFsBinding.inflate(layoutInflater)
                    playlistBinding = PlaylistBinding.inflate(layoutInflater)
                    createPlaylistBinding = CreatePlaylistBinding.inflate(layoutInflater)
                    player = Player(this, playerFsBinding = playerLayout, mainBinding = mainBinding, playlistBinding = playlistBinding)

                    trackAdapter = TrackAdapter(mainBinding,playerLayout,player)
                    playlistAdapter = PlaylistAdapter(mainBinding, player,playlistBinding)
                    repository = JamendoRepository()
                    goToMain()
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        Log.d("test", "aboba")


    }

//    override fun onResume() {
//        super.onResume()
//        player = Player(this)
//        playlistLayout = PlaylistBinding.inflate(layoutInflater)
//        playerLayout = PlayerFsBinding.inflate(layoutInflater)
//
//        trackAdapter = TrackAdapter(playlistLayout,playerLayout,player)
//        goToPlaylist()
//    }

    private fun init(){
        mainBinding.apply {
            recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
            recyclerView.adapter = trackAdapter
        }
        playlistBinding.apply {
            recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
            recyclerView.adapter = playlistAdapter
        }
    }

//    private suspend fun save(context: Context){
//        withContext(Dispatchers.IO){
//            context.openFileOutput("tracks.json", Context.MODE_PRIVATE).use {
//                it.write(STRING.toByteArray())
//            }
//        }
//    }

    companion object{
        private val IMAGES = mutableListOf<String>(
            "https://images.unsplash.com/photo-1600267185393-e158a98703de?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&h=600&ixid=MnwxfDB8MXxyYW5kb218fHx8fHx8fHwxNjI0MDE0NjQ0&ixlib=rb-1.2.1&q=80&utm_campaign=api-credit&utm_medium=referral&utm_source=unsplash_source&w=800",
            "https://images.unsplash.com/photo-1488426862026-3ee34a7d66df?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&h=600&ixid=MnwxfDB8MXxyYW5kb218fHx8fHx8fHwxNjI0MDE0ODE0&ixlib=rb-1.2.1&q=80&utm_campaign=api-credit&utm_medium=referral&utm_source=unsplash_source&w=800",
            "https://images.unsplash.com/photo-1620252655460-080dbec533ca?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&h=600&ixid=MnwxfDB8MXxyYW5kb218fHx8fHx8fHwxNjI0MDE0NzQ1&ixlib=rb-1.2.1&q=80&utm_campaign=api-credit&utm_medium=referral&utm_source=unsplash_source&w=800",
            "https://images.unsplash.com/photo-1613679074971-91fc27180061?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&h=600&ixid=MnwxfDB8MXxyYW5kb218fHx8fHx8fHwxNjI0MDE0NzUz&ixlib=rb-1.2.1&q=80&utm_campaign=api-credit&utm_medium=referral&utm_source=unsplash_source&w=800",
            "https://images.unsplash.com/photo-1485795959911-ea5ebf41b6ae?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&h=600&ixid=MnwxfDB8MXxyYW5kb218fHx8fHx8fHwxNjI0MDE0NzU4&ixlib=rb-1.2.1&q=80&utm_campaign=api-credit&utm_medium=referral&utm_source=unsplash_source&w=800",
            "https://images.unsplash.com/photo-1545996124-0501ebae84d0?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&h=600&ixid=MnwxfDB8MXxyYW5kb218fHx8fHx8fHwxNjI0MDE0NzY1&ixlib=rb-1.2.1&q=80&utm_campaign=api-credit&utm_medium=referral&utm_source=unsplash_source&w=800",
            "https://images.unsplash.com/flagged/photo-1568225061049-70fb3006b5be?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&h=600&ixid=MnwxfDB8MXxyYW5kb218fHx8fHx8fHwxNjI0MDE0Nzcy&ixlib=rb-1.2.1&q=80&utm_campaign=api-credit&utm_medium=referral&utm_source=unsplash_source&w=800",
            "https://images.unsplash.com/photo-1567186937675-a5131c8a89ea?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&h=600&ixid=MnwxfDB8MXxyYW5kb218fHx8fHx8fHwxNjI0MDE0ODYx&ixlib=rb-1.2.1&q=80&utm_campaign=api-credit&utm_medium=referral&utm_source=unsplash_source&w=800",
            "https://images.unsplash.com/photo-1546456073-92b9f0a8d413?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&h=600&ixid=MnwxfDB8MXxyYW5kb218fHx8fHx8fHwxNjI0MDE0ODY1&ixlib=rb-1.2.1&q=80&utm_campaign=api-credit&utm_medium=referral&utm_source=unsplash_source&w=800"
        )
        private val TRACK = Environment.getExternalStorageDirectory().path + "/Download/music.mp3"
        private val STRING = """ 
            [{"id":1,"name":"music.mp3","genre":"Unknown","artist":"Unknown","photo":"https://images.unsplash.com/photo-1600267185393-e158a98703de?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&h=600&ixid=MnwxfDB8MXxyYW5kb218fHx8fHx8fHwxNjI0MDE0NjQ0&ixlib=rb-1.2.1&q=80&utm_campaign=api-credit&utm_medium=referral&utm_source=unsplash_source&w=800","uri":"/storage/emulated/0/Download/music.mp3"},{"id":2,"name":"surf-curse-freaks.mp3","genre":"Unknown","artist":"Unknown","photo":"https://images.unsplash.com/photo-1488426862026-3ee34a7d66df?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&h=600&ixid=MnwxfDB8MXxyYW5kb218fHx8fHx8fHwxNjI0MDE0ODE0&ixlib=rb-1.2.1&q=80&utm_campaign=api-credit&utm_medium=referral&utm_source=unsplash_source&w=800","uri":"/storage/emulated/0/Download/surf-curse-freaks.mp3"},{"id":3,"name":"Atsushi Kitajoh - Full Moon Full Life.mp3","genre":"Unknown","artist":"Unknown","photo":"https://images.unsplash.com/photo-1620252655460-080dbec533ca?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&h=600&ixid=MnwxfDB8MXxyYW5kb218fHx8fHx8fHwxNjI0MDE0NzQ1&ixlib=rb-1.2.1&q=80&utm_campaign=api-credit&utm_medium=referral&utm_source=unsplash_source&w=800","uri":"/storage/emulated/0/Download/Atsushi Kitajoh - Full Moon Full Life.mp3"},{"id":4,"name":"Atsushi Kitajoh - It’s Going Down Now.mp3","genre":"Unknown","artist":"Unknown","photo":"https://images.unsplash.com/photo-1613679074971-91fc27180061?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&h=600&ixid=MnwxfDB8MXxyYW5kb218fHx8fHx8fHwxNjI0MDE0NzUz&ixlib=rb-1.2.1&q=80&utm_campaign=api-credit&utm_medium=referral&utm_source=unsplash_source&w=800","uri":"/storage/emulated/0/Download/Atsushi Kitajoh - It’s Going Down Now.mp3"}]
        """.trimIndent()
    }
}




// JSON для запам'ятовування треків та плейлистів
// окрема безкоштовна API для музики
