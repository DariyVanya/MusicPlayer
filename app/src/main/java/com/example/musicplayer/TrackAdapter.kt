package com.example.musicplayer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicplayer.databinding.PlayerFsBinding
import com.example.musicplayer.databinding.MainBinding
import com.example.musicplayer.databinding.TrackItemBinding


class TrackAdapter(var mainViewBinding:MainBinding,
                   var playerViewBinding: PlayerFsBinding,
                   var player:Player) : RecyclerView.Adapter<TrackAdapter.TrackHolder>() {
    var trackList = ArrayList<Track>()

    class TrackHolder(item: View, var player: Player,
                      var playerViewBinding: PlayerFsBinding,
                      var playlistViewBinding:MainBinding,
                      var trackList: ArrayList<Track>) : RecyclerView.ViewHolder(item){

        val binding = TrackItemBinding.bind(item)

        fun bind(track: Track){

            binding.nameTextView.text = track.name
            binding.artistTextView.text = track.artist
            binding.imageView.setOnClickListener{
                player.play(track)
                player.setNext((trackList.slice(trackList.indexOf(track)+1..trackList.size-1)).toMutableList())
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackHolder {
        val view =  LayoutInflater.from(parent.context).inflate(R.layout.track_item, parent, false)
        return TrackHolder(view, player, playerViewBinding, mainViewBinding, trackList)
    }

    override fun onBindViewHolder(holder: TrackHolder, position: Int) {
        holder.bind(trackList[position])

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
    fun setTrackList(tracks: List<Track>){
        trackList.clear()
        trackList.addAll(tracks)
        notifyDataSetChanged()
    }

}
