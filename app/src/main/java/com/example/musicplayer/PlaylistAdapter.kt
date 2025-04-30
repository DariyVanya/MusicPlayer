package com.example.musicplayer

import android.net.Uri
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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


class PlaylistAdapter(var mainBinding: MainBinding, var player: Player,
                      var playlistBinding:PlaylistBinding) : RecyclerView.Adapter<PlaylistAdapter.PlaylistHolder>() {
    var playlistList = ArrayList<Playlist>()

    class PlaylistHolder(item: View, var mainBinding: MainBinding, var player: Player,
                         var playlistBinding:PlaylistBinding,
                         var playlistList: ArrayList<Playlist>) : RecyclerView.ViewHolder(item){

        val binding = PlaylistItemBinding.bind(item)

        fun bind(playlist: Playlist, position: Int){

            binding.nameTextView.text = playlist.name
            binding.imageView.setOnClickListener{
                var playlistInfoAdapter = PlaylistInfoAdapter(mainBinding, playlistBinding, player, playlist)
                playlistBinding.recyclerView.adapter = playlistInfoAdapter
                playlistBinding.createNewBtn.isVisible = false
                playlistBinding.goBackBtn.isVisible = true
                playlistBinding.playlistNameLbl.isVisible = true
                playlistBinding.editPlaylist.isVisible = true
                playlistBinding.playlistNameLbl.text = playlist.name

                playlistBinding.editNameTxt.setText(playlist.name)
                Glide.with(binding.imageView.context)
                    .load(playlist.getPhoto())
                    .circleCrop()
                    .error(R.drawable.cover)
                    .into(playlistBinding.editCoverImg)
                playlistBinding.editPlaylistPos.setText(position.toString())
                playlistInfoAdapter.setPlaylistInfo(playlist)


            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistHolder {
        val view =  LayoutInflater.from(parent.context).inflate(R.layout.playlist_item, parent, false)
        return PlaylistHolder(view, mainBinding, player, playlistBinding, playlistList)
    }

    override fun onBindViewHolder(holder: PlaylistHolder, position: Int) {
        holder.bind(playlistList[position], position)

        val context = holder.itemView.context
        try {
            val uri = Uri.parse(playlistList[position].getPhotoString())
            Glide.with(context)
                .load(uri)
                .circleCrop()
                .error(R.drawable.cover)
                .placeholder(R.drawable.cover)
                .into(holder.binding.imageView)
        } catch (e: Exception) {
            Log.e("PlaylistAdapter", "Error loading image: ${e.message}")
            Glide.with(context)
                .load(R.drawable.cover)
                .circleCrop()
                .into(holder.binding.imageView)
        }

    }

    override fun getItemCount(): Int = playlistList.size

    fun addPlaylist(playlist: Playlist){
        playlistList.add(playlist)
        notifyDataSetChanged()
    }

    fun addPlaylist(playlist: List<Playlist>){
        playlistList.addAll(playlist)
        notifyDataSetChanged()
    }
    fun setPlaylistList(playlists: List<Playlist>){
        playlistList.clear()
        playlistList.addAll(playlists)
        val jsonString = Json.encodeToString(playlists)
        Log.d("test", jsonString)

        notifyDataSetChanged()
    }

}
