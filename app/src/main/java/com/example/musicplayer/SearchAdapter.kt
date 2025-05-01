package com.example.musicplayer

import android.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicplayer.databinding.SearchBinding
import com.example.musicplayer.databinding.PlayerFsBinding
import com.example.musicplayer.databinding.TrackItemBinding
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File

class SearchAdapter(
    private val searchBinding: SearchBinding,
    private val playerFsBinding: PlayerFsBinding,
    private val player: Player
) : RecyclerView.Adapter<SearchAdapter.SearchViewHolder>() {

    var trackList = ArrayList<Track>()

    class SearchViewHolder(
        item: View,
        private val searchBinding: SearchBinding,
        private val player: Player,
        private val playerFsBinding: PlayerFsBinding,
        private val trackList: ArrayList<Track>
    ) : RecyclerView.ViewHolder(item) {

        val binding = TrackItemBinding.bind(item)

        fun bind(track: Track, position: Int) {
            binding.nameTextView.text = track.name
            binding.artistTextView.text = track.artist

            binding.imageView.setOnClickListener {
                // Clear existing tracks first
                player.setPrev(mutableListOf())
                player.setNext(mutableListOf())

                // Set previous tracks (all tracks before current)
                val prevTracks = trackList.slice(0..trackList.indexOf(track)-1).reversed().toMutableList()
                player.setPrev(prevTracks)

                // Play current track
                player.play(track)

                // Set next tracks (all tracks after current)
                val nextTracks = trackList.slice(trackList.indexOf(track)+1..trackList.size-1).toMutableList()
                player.setNext(nextTracks)

                searchBinding.nowPlayingMenu.isVisible = true
                updateNowPlayingUI(track)
            }

            binding.trackMenu.setOnClickListener {
                showPopupMenu(it, track, position)
            }
        }

        private fun showPopupMenu(view: View, track: Track, position: Int) {
            val popup = PopupMenu(view.context, view)
            popup.inflate(R.menu.track_popup_menu)
            val deleteAction = popup.menu.getItem(1)
            deleteAction.isVisible = false

            popup.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.action_add_to_playlist -> {
                        val context = itemView.context
                        val playlistsFile = File(context.filesDir, "playlists.json")

                        if (!playlistsFile.exists()) {
                            Log.e("Playlist", "No playlists found.")
                            return@setOnMenuItemClickListener true
                        }

                        val json = playlistsFile.readText()
                        val playlists = Json.decodeFromString<MutableList<Playlist>>(json)

                        val playlistNames = playlists.map { it.name }.toTypedArray()

                        AlertDialog.Builder(context)
                            .setTitle("Add to Playlist")
                            .setItems(playlistNames) { _, which ->
                                val selectedPlaylist = playlists[which]
                                selectedPlaylist.addTrack(track)

                                val updatedJson = Json.encodeToString(playlists)
                                playlistsFile.writeText(updatedJson)

                                Log.d(
                                    "Playlist",
                                    "Track '${track.name}' added to '${selectedPlaylist.name}'"
                                )
                            }
                            .setNegativeButton("Cancel", null)
                            .show()

                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

        private fun updateNowPlayingUI(track: Track) {
            // Update search view now playing section
            searchBinding.nowPlayingName.text = track.name
            Glide.with(searchBinding.root.context)
                .load(track.photo)
                .error(R.drawable.cover)
                .placeholder(R.drawable.cover)
                .into(searchBinding.nowPlayingImage)

            // Update player view
            playerFsBinding.soundtrackTitle.text = track.name
            Glide.with(playerFsBinding.root.context)
                .load(track.photo)
                .error(R.drawable.cover)
                .placeholder(R.drawable.cover)
                .into(playerFsBinding.soundtrackCoverImg)

            searchBinding.playBtn.setImageResource(R.drawable.baseline_pause_24)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.track_item, parent, false)
        return SearchViewHolder(view, searchBinding, player, playerFsBinding, trackList)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        holder.bind(trackList[position], position)

        val context = holder.itemView.context
        Glide.with(context)
            .load(trackList[position].photo)
            .circleCrop()
            .error(R.drawable.cover)
            .placeholder(R.drawable.cover)
            .into(holder.binding.imageView)
    }

    override fun getItemCount(): Int = trackList.size

    fun addTrack(track: Track) {
        trackList.add(track)
        notifyDataSetChanged()
    }

    fun addTrack(tracks: List<Track>) {
        trackList.addAll(tracks)
        notifyDataSetChanged()
    }

    fun addTracks(newTracks: List<Track>) {
        val startPos = trackList.size
        trackList.addAll(newTracks)
        notifyItemRangeInserted(startPos, newTracks.size)
    }

    fun setTrackList(tracks: List<Track>) {
        trackList.clear()
        trackList.addAll(tracks)
        val jsonString = Json.encodeToString(tracks)
        Log.d("test", jsonString)
        notifyDataSetChanged()
    }
}