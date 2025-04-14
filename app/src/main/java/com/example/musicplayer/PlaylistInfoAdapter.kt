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
import com.example.musicplayer.databinding.MainBinding
import com.example.musicplayer.databinding.PlaylistBinding
import com.example.musicplayer.databinding.PlaylistItemBinding
import com.example.musicplayer.databinding.TrackItemBinding
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File


class PlaylistInfoAdapter(var mainBinding: MainBinding, var playlistBinding:PlaylistBinding, var player: Player, var playlist: Playlist) : RecyclerView.Adapter<PlaylistInfoAdapter.PlaylistInfoHolder>() {
    var trackList = ArrayList<Track>()

    class PlaylistInfoHolder(item: View,
                      var playlistBinding:PlaylistBinding,
                      var trackList: ArrayList<Track>, var player: Player, var mainBinding: MainBinding, val adapter: PlaylistInfoAdapter) : RecyclerView.ViewHolder(item) {

        val binding = TrackItemBinding.bind(item)

        fun bind(track: Track, playlist: Playlist, position: Int) {

            binding.nameTextView.text = track.name
            binding.artistTextView.text = track.artist
            binding.imageView.setOnClickListener {
                player.play(track)
                player.setNext((trackList.slice(trackList.indexOf(track) + 1..trackList.size - 1)).toMutableList())
                mainBinding.nowPlayingMenu.isVisible = true;
                playlistBinding.nowPlayingMenu.isVisible = true;
            }

            binding.trackMenu.setOnClickListener {
                showPopupMenu(it, track, playlist, position)
            }
        }

        private fun showPopupMenu(view: View, track: Track, playlist: Playlist, position: Int) {
            val popup = PopupMenu(view.context, view)
            popup.inflate(R.menu.track_popup_menu)
            val deleteAction = popup.menu.getItem(1)
            deleteAction.isVisible = true;

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

                    R.id.action_delete_from_playlist -> {
                        val currentPosition = bindingAdapterPosition.takeIf { it != RecyclerView.NO_POSITION }
                            ?: return@setOnMenuItemClickListener true

                        val context = itemView.context
                        val playlistsFile = File(context.filesDir, "playlists.json")

                        if (!playlistsFile.exists()) {
                            Log.e("Playlist", "No playlists found.")
                            return@setOnMenuItemClickListener true
                        }

                        val json = playlistsFile.readText()
                        val playlists = Json.decodeFromString<MutableList<Playlist>>(json)

                        playlists[playlists.indexOf(playlist)].deleteTrack(currentPosition)

                        val updatedJson = Json.encodeToString(playlists)
                        playlistsFile.writeText(updatedJson)

                        trackList.removeAt(currentPosition)
                        adapter.notifyItemRemoved(currentPosition)
                        playlist.deleteTrack(currentPosition)

                        true
                    }


                    else -> false
                }
            }


            popup.show()
        }
    }



        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistInfoHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.track_item, parent, false)
            return PlaylistInfoHolder(view, playlistBinding, trackList, player, mainBinding, this)
        }

        override fun onBindViewHolder(holder: PlaylistInfoHolder, position: Int) {
            holder.bind(trackList[position], playlist, position)

            val context = holder.itemView.context
            Glide.with(context).load(trackList[position].photo).circleCrop()
                .error(R.drawable.cover)
                .placeholder(R.drawable.cover).into(holder.binding.imageView)

        }

        override fun getItemCount(): Int = trackList.size

        fun setPlaylistInfo(playlist: Playlist) {
            trackList.clear()
            trackList.addAll(playlist.getTracks())
            val jsonString = Json.encodeToString(playlist.getTracks())
            Log.d("test", jsonString)

            notifyDataSetChanged()
        }
    }
