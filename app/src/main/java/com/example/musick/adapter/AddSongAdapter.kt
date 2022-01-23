package com.example.musick.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musick.OnClickListerner
import com.example.musick.OnSelectSongListener
import com.example.musick.R
import com.example.musick.Song
import database.MusicDatabase
import kotlinx.android.synthetic.main.item_add_song.view.*
import java.io.File


class AddSongAdapter(val listSong: ArrayList<Song>, val context: Context,
                     val onSelectSongListener: OnSelectSongListener):
    RecyclerView.Adapter<AddSongAdapter.AddSongViewHolder>()   {

    var playlistDatabase = MusicDatabase(context, "playlist.db", null, 1)
    val listSongSelect = ArrayList<Song>()

    class AddSongViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val imvImageCover   = view.profile_image
        val tvSongName      = view.tvTracks
        val tvArtist        = view.tvArtist
        val tracksView       = view.trackView
        val checkBook            = view.checkbox
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddSongViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_add_song, parent, false)
        return AddSongViewHolder(view)
    }

    override fun onBindViewHolder(holder: AddSongViewHolder, position: Int) {
        holder.tvSongName.text = listSong[position].songName
        holder.tvArtist.text = listSong[position].artist

        Glide.with(context)
            .load(File(listSong[position].thumbnail))
            .placeholder(R.drawable.music_default)
            .error("image error")
            .centerCrop()
            .into(holder.imvImageCover)

        holder.tracksView.setOnClickListener {
            if (!holder.checkBook.isChecked) {
                holder.checkBook.isChecked = true
                listSongSelect.add(listSong[position])
            } else {
                holder.checkBook.isChecked = false
                listSongSelect.remove(listSong[position])
            }
            onSelectSongListener.onSelectSong(listSongSelect)
        }

        holder.checkBook.setOnClickListener {
            if (holder.checkBook.isChecked) {
                listSongSelect.add(listSong[position])
            } else {
                listSongSelect.remove(listSong[position])
            }
            onSelectSongListener.onSelectSong(listSongSelect)
        }

        onSelectSongListener.onSelectSong(listSongSelect)
    }

    override fun getItemCount(): Int {
        return listSong.size
    }
}