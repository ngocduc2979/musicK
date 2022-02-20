package com.example.musick.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musick.*
import database.MusicDatabase
import kotlinx.android.synthetic.main.item_playlist.view.*

class BottomDialogAdapter(val listPlaylist: ArrayList<String>, val context: Context, var song: Song,
                          var onClickListerner: OnClickListerner):
    RecyclerView.Adapter<BottomDialogAdapter.BottomDialogViewHolder>() {

    val listSong = ArrayList<Song>()
    var playlistDatabase = MusicDatabase(context, "playlist.db", null, 1)

    class BottomDialogViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val imvCover        = view.profile_playlist_image
        val tvPlaylistName  = view.tvPlaylistName
        val tvTracks        = view.tvTracksNumber
        val trackView       = view.trackView
        val menu            = view.three_dot
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BottomDialogViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_playlist, parent, false)
        return BottomDialogViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: BottomDialogViewHolder, position: Int) {
        listSong.clear()
        val cursor = playlistDatabase.getData("SELECT * FROM '" + listPlaylist[position] + "'")
        while (cursor.moveToNext()) {
            val name = cursor.getString(0)
            val artist = cursor.getString(1)
            val path = cursor.getString(2)
            val album = cursor.getString(3)
            val duration = cursor.getString(4)
            val thumbnail = cursor.getString(5)
            listSong.add(Song(name, artist, path, album, duration, thumbnail))
        }

        holder.tvPlaylistName.text = listPlaylist[position]
        holder.tvTracks.text = listSong.size.toString() + " Bài hát"

        if (listSong.size != 0) {
            Glide.with(context)
                .load(listSong[0].thumbnail)
                .placeholder(R.drawable.music_default)
                .centerCrop()
                .into(holder.imvCover)
        }

        holder.trackView.setOnClickListener {
            listSong.clear()
            val cursor = playlistDatabase.getData("SELECT * FROM '" + listPlaylist[position] + "'")
            while (cursor.moveToNext()) {
                val name = cursor.getString(0)
                val artist = cursor.getString(1)
                val path = cursor.getString(2)
                val album = cursor.getString(3)
                val duration = cursor.getString(4)
                val thumbnail = cursor.getString(5)
                listSong.add(Song(name, artist, path, album, duration, thumbnail))
            }

            var checkExist = false

            listSong.forEach {
                if (it.path == song.path) {
                    checkExist = true
                }
            }

            if (!checkExist) {
                val addSong = "INSERT INTO '" +  listPlaylist[position] + "' VALUES (" + "'" + song.songName + "', " +
                        "'" + song.artist + "', " +
                        "'" + song.path + "', " +
                        "'" + song.album + "', " +
                        "'" + song.duration + "', " +
                        "'" + song.thumbnail + "')"
                playlistDatabase.querryData(addSong)
                Toast.makeText(context, "Đã thêm bài hát vào: '" + listPlaylist[position] + "'", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Bài hát đã có trong: '" + listPlaylist[position] + "'", Toast.LENGTH_LONG).show()
            }

            onClickListerner.setOnclick()
        }
    }

    override fun getItemCount(): Int {
        return listPlaylist.size
    }
}