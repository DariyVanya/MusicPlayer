package com.example.musicplayer

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicplayer.databinding.MainBinding
import com.example.musicplayer.databinding.PlaylistBinding
import com.example.musicplayer.databinding.PlaylistItemBinding
import com.example.musicplayer.databinding.TrackItemBinding
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


class PlaylistInfoAdapter(var mainBinding: MainBinding, var playlistBinding:PlaylistBinding, var player: Player) : RecyclerView.Adapter<PlaylistInfoAdapter.PlaylistInfoHolder>() {
    var trackList = ArrayList<Track>()

    class PlaylistInfoHolder(item: View,
                      var playlistBinding:PlaylistBinding,
                      var trackList: ArrayList<Track>, var player: Player, var mainBinding: MainBinding) : RecyclerView.ViewHolder(item){

        val binding = TrackItemBinding.bind(item)

        fun bind(track: Track){

            binding.nameTextView.text = track.name
            binding.artistTextView.text = track.artist
            binding.imageView.setOnClickListener{
                player.play(track)
                player.setNext((trackList.slice(trackList.indexOf(track)+1..trackList.size-1)).toMutableList())
                mainBinding.nowPlayingMenu.isVisible = true;
                playlistBinding.nowPlayingMenu.isVisible = true;
            }
            binding.addToPlaylistButton.setOnClickListener{

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistInfoHolder {
        val view =  LayoutInflater.from(parent.context).inflate(R.layout.track_item, parent, false)
        return PlaylistInfoHolder(view, playlistBinding, trackList, player, mainBinding)
    }

    override fun onBindViewHolder(holder: PlaylistInfoHolder, position: Int) {
        holder.bind(trackList[position])

        val context = holder.itemView.context
        Glide.with(context).load(trackList[position].photo).circleCrop()
            .error(R.drawable.cover)
            .placeholder(R.drawable.cover).into(holder.binding.imageView)

    }

    override fun getItemCount(): Int = trackList.size

    fun setPlaylistInfo(playlist: Playlist){
        trackList.clear()
        trackList.addAll(playlist.getTracks())
        val jsonString = Json.encodeToString(playlist.getTracks())
        Log.d("test", jsonString)

        notifyDataSetChanged()
    }

}
