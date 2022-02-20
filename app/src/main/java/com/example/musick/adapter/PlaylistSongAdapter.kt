package com.example.musick.adapter

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.*
import android.widget.Button
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
import androidx.appcompat.view.menu.MenuBuilder

import androidx.appcompat.view.menu.MenuPopupHelper


class PlaylistSongAdapter(
    var listSong: ArrayList<Song>, val context: Context,
    val playlistName: String, val onClickListerner: OnClickListerner,
):
    RecyclerView.Adapter<PlaylistSongAdapter.PlaylistSongViewHolder>() {

    var playlistDatabase = MusicDatabase(context, "playlist.db", null, 1)

    class PlaylistSongViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val imvImageCover   = view.profile_image
        val tvSongName      = view.tvTracks
        val tvArtist        = view.tvArtist
        val tracksView       = view.trackView
        val menu            = view.menu
        val playing         = view.playing
        val pause           = view.pause
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistSongViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_all_tracks, parent, false)
        return PlaylistSongViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaylistSongViewHolder, position: Int) {
        holder.tvSongName.text = listSong[position].songName
        holder.tvArtist.text = listSong[position].artist

        Glide.with(context)
            .load(if (listSong[position].thumbnail.contains("ngocduc2979")) listSong[position].thumbnail else
                File(listSong[position].thumbnail))
            .placeholder(R.drawable.music_default)
            .error("image error")
            .centerCrop()
            .into(holder.imvImageCover)

        if (AppConfig.getInstance(context).getPlayList() != null) {
            setStatePlay(holder, listSong[position])
        } else {
            holder.playing.visibility = View.GONE
            holder.pause.visibility = View.GONE
            holder.tracksView.setBackgroundResource(R.color.no_color)
        }

        holder.tracksView.setOnClickListener {
            val file = File(listSong[position].path)
            if (file.exists()) {
                clickTracksView(position)
            } else {
                showDialog()
                val deleteSong = "DELETE FROM '" + playlistName + "' WHERE path = '" +  listSong[position].path + "'"
                playlistDatabase.querryData(deleteSong)
                onClickListerner.setOnclick()
            }
        }

        holder.menu.setOnClickListener {
            showPopupMenu(it, position)
        }
    }

    override fun getItemCount(): Int {
        return listSong.size
    }

    @SuppressLint("ResourceAsColor")
    private fun setStatePlay(holder: PlaylistSongViewHolder, song: Song) {
        if (AppConfig.getInstance(context).getCurrentSong().path == song.path) {
            holder.tracksView.setBackgroundResource(R.color.playing_color)
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
            holder.tracksView.setBackgroundResource(R.color.no_color)
        }
    }

    private fun clickTracksView(position: Int) {
        AppConfig.getInstance(context).setCurPosition(position)
        AppConfig.getInstance(context).setPlaylist(listSong)
        AppConfig.getInstance(context).setIsNewPlay(true)

        DataPlayer.getInstance()!!.setPlayPosition(position)
        DataPlayer.getInstance()!!.setPlaylist(listSong)

        onClickListerner.setOnclick()
        PlayerActivity.launch(context)
    }

    @SuppressLint("RestrictedApi", "DiscouragedPrivateApi")
    fun showPopupMenu(view: View, position: Int) {

        val menuBuilder = MenuBuilder(context)
        val inflater = MenuInflater(context)
        inflater.inflate(R.menu.song_inside_playlist, menuBuilder)
        val optionsMenu = MenuPopupHelper(context, menuBuilder, view)
        optionsMenu.setForceShowIcon(true)

        menuBuilder.setCallback(object : MenuBuilder.Callback{
            override fun onMenuItemSelected(menu: MenuBuilder, item: MenuItem): Boolean {
                return when (item.itemId) {
                    R.id.play -> {
                        AppConfig.getInstance(context).setCurPosition(position)
                        AppConfig.getInstance(context).setPlaylist(listSong)
                        AppConfig.getInstance(context).setIsNewPlay(true)

                        DataPlayer.getInstance()!!.setPlayPosition(position)
                        DataPlayer.getInstance()!!.setPlaylist(listSong)

                        PlayerActivity.launch(context)
                        true
                    }
                    R.id.delete -> {
                        delete(listSong[position])
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

    private fun showDialog() {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.not_exists_dialog)
        dialog.setCancelable(true)
        val window = dialog.window
        window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT)

        dialog.show()

        val delete = dialog.findViewById<Button>(R.id.tv_delete)

        delete.setOnClickListener {
            dialog.dismiss()
        }
    }

    fun delete(song: Song) {
        val removeSong = "DELETE FROM '" + playlistName + "' WHERE path = " + "'" +  song.path + "'"
        playlistDatabase.querryData(removeSong)
        onClickListerner.setOnclick()
    }
}