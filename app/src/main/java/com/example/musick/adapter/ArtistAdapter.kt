package com.example.musick.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musick.R
import com.example.musick.Song
import com.example.musick.activity.ArtistSongActivity
import kotlinx.android.synthetic.main.item_artist.view.*
import saveData.AppConfig

class ArtistAdapter(val listArtist: ArrayList<Song>, val context: Context): RecyclerView.Adapter<ArtistAdapter.ArtistViewHolder>() {

    val listSongArtist = ArrayList<Song>()

    class ArtistViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val tvArtist    = view.artist
        val cover       = view.cover
        val tracks      = view.tracks
        val artistView  = view.artist_layout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_artist, parent, false)
        return ArtistViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {

        val lisAllTracks = AppConfig.getInstance(context).getCacheSong()
        listSongArtist.clear()
        lisAllTracks.forEach {
            if (listArtist[position].artist == it.artist) {
                listSongArtist.add(it)
            }
        }

        holder.tvArtist.text = listArtist[position].artist
        holder.tracks.text = listSongArtist.size.toString()
        Glide.with(context)
            .load(listSongArtist[0].thumbnail)
            .centerCrop()
            .placeholder(R.drawable.music_default)
            .into(holder.cover)

        holder.artistView.setOnClickListener {
            listSongArtist.clear()
            lisAllTracks.forEach {
                if (listArtist[position].artist == it.artist) {
                    listSongArtist.add(it)
                }
            }
            ArtistSongActivity.launch(context, listSongArtist, listArtist[position].artist)
        }
    }

    override fun getItemCount(): Int {
        return listArtist.size
    }
}