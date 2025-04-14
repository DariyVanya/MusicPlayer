package com.example.musicplayer

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicplayer.databinding.PlayerFsBinding
import com.example.musicplayer.databinding.MainBinding
import com.example.musicplayer.databinding.TrackItemBinding
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import android.app.AlertDialog
import android.content.Context
import android.widget.PopupMenu


class TrackAdapter(var mainViewBinding:MainBinding,
                   var playerViewBinding: PlayerFsBinding,
                   var player:Player) : RecyclerView.Adapter<TrackAdapter.TrackHolder>() {
    var trackList = ArrayList<Track>()

    class TrackHolder(item: View,
                      var mainViewBinding:MainBinding,
                      var player: Player,
                      var playerViewBinding: PlayerFsBinding,
                      var playlistViewBinding:MainBinding,
                      var trackList: ArrayList<Track>) : RecyclerView.ViewHolder(item){

        val binding = TrackItemBinding.bind(item)

        fun bind(track: Track, position: Int){

            binding.nameTextView.text = track.name
            binding.artistTextView.text = track.artist
            binding.imageView.setOnClickListener{
                player.play(track)
                player.setNext((trackList.slice(trackList.indexOf(track)+1..trackList.size-1)).toMutableList())
                mainViewBinding.nowPlayingMenu.isVisible = true;
                playlistViewBinding.nowPlayingMenu.isVisible = true;
            }

            binding.trackMenu.setOnClickListener {
                showPopupMenu(it, track, position)
            }

        }
        private fun showPopupMenu(view: View, track: Track, position: Int) {
            val popup = PopupMenu(view.context, view)
            popup.inflate(R.menu.track_popup_menu)
            val deleteAction = popup.menu.getItem(1)
            deleteAction.isVisible = false;

            popup.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.action_add_to_playlist -> {
                        val context = itemView.context
                        val playlistsFile = File(context.filesDir, "playlists.json")

                        if (!playlistsFile.exists()) {
                            Log.e("Playlist", "No playlists found.")
                            return@setOnMenuItemClickListener true // ✅ Додаємо return
                        }

                        val json = playlistsFile.readText()
                        val playlists = Json.decodeFromString<MutableList<Playlist>>(json)

                        val playlistNames = playlists.map { it.name }.toTypedArray()

                        AlertDialog.Builder(context)
                            .setTitle("Додати до плейлиста")
                            .setItems(playlistNames) { _, which ->
                                val selectedPlaylist = playlists[which]
                                selectedPlaylist.addTrack(track)

                                val updatedJson = Json.encodeToString(playlists)
                                playlistsFile.writeText(updatedJson)

                                Log.d(
                                    "Playlist",
                                    "Track '${track.name}' додано до '${selectedPlaylist.name}'"
                                )
                            }
                            .setNegativeButton("Скасувати", null)
                            .show()

                        true
                    }

                    else -> false
                }
            }


            popup.show()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackHolder {
        val view =  LayoutInflater.from(parent.context).inflate(R.layout.track_item, parent, false)
        return TrackHolder(view, mainViewBinding, player, playerViewBinding, mainViewBinding, trackList)
    }

    override fun onBindViewHolder(holder: TrackHolder, position: Int) {
        holder.bind(trackList[position], position)

        val context = holder.itemView.context
        Glide.with(context).load(trackList[position].photo).circleCrop()
            .error(R.drawable.cover)
            .placeholder(R.drawable.cover).into(holder.binding.imageView)

    }

    override fun getItemCount(): Int = trackList.size 

    fun addTrack(track: Track){
        trackList.add(track)
        notifyDataSetChanged()
    }

    fun addTrack(tracks: List<Track>){
        trackList.addAll(tracks)
        notifyDataSetChanged()
    }

    fun addTracks(newTracks: List<Track>) {
        val startPos = trackList.size
        trackList.addAll(newTracks)
        notifyItemRangeInserted(startPos, newTracks.size)
    }

    fun setTrackList(tracks: List<Track>){
        trackList.clear()
        trackList.addAll(tracks)
        val jsonString = Json.encodeToString(tracks)
        Log.d("test", jsonString)

        notifyDataSetChanged()
    }

}
