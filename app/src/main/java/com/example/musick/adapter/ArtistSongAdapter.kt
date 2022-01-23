package com.example.musick.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musick.OnClickListerner
import com.example.musick.R
import com.example.musick.Song
import com.example.musick.activity.PlayerActivity
import database.MusicDatabase
import kotlinx.android.synthetic.main.item_all_tracks.view.*
import saveData.AppConfig
import saveData.DataPlayer
import java.io.File

class ArtistSongAdapter(val listSong: ArrayList<Song>, val context: Context):
    RecyclerView.Adapter<ArtistSongAdapter.ArtistSongViewHolder>() {

    var playlistDatabase = MusicDatabase(context, "playlist.db", null, 1)

    class ArtistSongViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val imvImageCover   = view.profile_image
        val tvSongName      = view.tvTracks
        val tvArtist        = view.tvArtist
        val trackView       = view.trackView
        val menu            = view.menu
        val playing         = view.playing
        val pause           = view.pause
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistSongViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_all_tracks, parent, false)
        return ArtistSongViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArtistSongViewHolder, position: Int) {
        holder.tvSongName.text = listSong[position].songName
        holder.tvArtist.text = listSong[position].artist
        Glide.with(context)
            .load(File(listSong[position].thumbnail))
            .placeholder(R.drawable.music_default)
            .error("image error")
            .centerCrop()
            .into(holder.imvImageCover)

        if (AppConfig.getInstance(context).getPlayList() != null) {
            setStatePlay(holder, listSong[position])
        } else {
            holder.playing.visibility = View.GONE
            holder.pause.visibility = View.GONE
            holder.trackView.setBackgroundResource(R.color.no_color)
        }

        holder.trackView.setOnClickListener {
            clickTracksView(position)
        }

        holder.menu.setOnClickListener {
            showPopupMenu(it, position)
        }
    }

    override fun getItemCount(): Int {
        return listSong.size
    }

    @SuppressLint("RestrictedApi", "DiscouragedPrivateApi")
    fun showPopupMenu(view: View, position: Int) {

        val menuBuilder = MenuBuilder(context)
        val inflater = MenuInflater(context)
        inflater.inflate(R.menu.song_inside_artist, menuBuilder)
        val optionsMenu = MenuPopupHelper(context, menuBuilder, view)
        optionsMenu.setForceShowIcon(true)

        menuBuilder.setCallback(object : MenuBuilder.Callback{
            override fun onMenuItemSelected(menu: MenuBuilder, item: MenuItem): Boolean {
                return when (item.itemId) {
                    R.id.play -> {
                        clickTracksView(position)
                        true
                    }
                    R.id.addFavorite -> {
                        addFavorite(listSong[position])
                        true
                    }
                    else -> false
                }
            }
            override fun onMenuModeChange(menu: MenuBuilder) {
            }
        })
        optionsMenu.show()
    }

    private fun addFavorite(song: Song) {
        val listFavorite = ArrayList<Song>()
        val getListFavorite = "SELECT * FROM  favorite"
        val cursor = playlistDatabase.getData(getListFavorite)
        while (cursor.moveToNext()) {
            val name = cursor.getString(0)
            val artist = cursor.getString(1)
            val path = cursor.getString(2)
            val album = cursor.getString(3)
            val duration = cursor.getString(4)
            val thumbnail = cursor.getString(5)
            listFavorite.add(Song(name, artist, path, album, duration, thumbnail))
        }

        var checkExist = false

        listFavorite.forEach {
            if (it.path == song.path) {
                checkExist = true
            }
        }

        if (!checkExist) {
            val insertSong = "INSERT INTO favorite VALUES (" + "'" + song.songName + "', " +
                    "'" + song.artist + "', " +
                    "'" + song.path + "', " +
                    "'" + song.album + "', " +
                    "'" + song.duration + "', " +
                    "'" + song.thumbnail + "')"
            playlistDatabase.querryData(insertSong)
            Toast.makeText(context, "Đã thêm vào yêu thích", Toast.LENGTH_LONG).show()
            checkExist = true
        } else {
            Toast.makeText(context, "'" + song.songName + "'" + " Đã có trong Yêu thích", Toast.LENGTH_LONG).show()
        }
    }

    @SuppressLint("ResourceAsColor")
    fun setStatePlay(holder: ArtistSongViewHolder, song: Song) {
        if (AppConfig.getInstance(context).getCurrentSong().path == song.path) {
            holder.trackView.setBackgroundResource(R.color.playing_color)
            holder.playing.visibility = View.VISIBLE
            if (AppConfig.getInstance(context).getIsPlaying()) {
                holder.playing.setText(R.string.playing_now)
                holder.pause.visibility = View.GONE
                holder.playing.setTextColor(context.resources.getColor(R.color.red))
            } else {
                holder.playing.setText(R.string.pause_now)
                holder.playing.setTextColor(context.resources.getColor(R.color.black))
                holder.pause.visibility = View.VISIBLE
            }
        } else {
            holder.playing.visibility = View.GONE
            holder.pause.visibility = View.GONE
            holder.trackView.setBackgroundResource(R.color.no_color)
        }
    }

    private fun clickTracksView(position: Int) {
        AppConfig.getInstance(context).setCurPosition(position)
        AppConfig.getInstance(context).setPlaylist(listSong)
        AppConfig.getInstance(context).setIsNewPlay(true)

        DataPlayer.getInstance()!!.setPlayPosition(position)
        DataPlayer.getInstance()!!.setPlaylist(listSong)

        PlayerActivity.launch(context)
    }
}