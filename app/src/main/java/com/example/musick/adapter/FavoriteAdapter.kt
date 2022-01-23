package com.example.musick.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.*
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

class FavoriteAdapter(var context: Context, var listFavorite: ArrayList<Song>, val onClickListerner: OnClickListerner):
    RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder>() {

    var playlistDatabase = MusicDatabase(context, "playlist.db", null, 1)

    class FavoriteViewHolder(view: View): RecyclerView.ViewHolder(view) {
        var imvThumbnail = view.profile_image
        var tvSong          = view.tvTracks
        var tvArtist        = view.tvArtist
        var tracksView      = view.trackView
        var memu            = view.menu
        val playing         = view.playing
        val pause           = view.pause
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, ): FavoriteViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_all_tracks, parent, false)
        return FavoriteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.tvSong.text = listFavorite[position].songName
        holder.tvArtist.text = listFavorite[position].artist

        Glide.with(context)
            .load(listFavorite[position].thumbnail)
            .placeholder(R.drawable.music_default)
            .centerCrop()
            .into(holder.imvThumbnail)

        holder.tracksView.setOnClickListener {
            clickTracksView(position)
        }

        holder.memu.setOnClickListener {
            showPopupMenu(position, it, listFavorite[position])
        }

        if (AppConfig.getInstance(context).getPlayList() != null) {
            setStatePlay(holder, listFavorite[position])
        } else {
            holder.playing.visibility = View.GONE
            holder.pause.visibility = View.GONE
            holder.tracksView.setBackgroundResource(R.color.no_color)
        }
    }

    override fun getItemCount(): Int {
        return listFavorite.size
    }

    @SuppressLint("ResourceAsColor")
    private fun setStatePlay(holder: FavoriteViewHolder, song: Song) {
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
        AppConfig.getInstance(context).setPlaylist(listFavorite)
        AppConfig.getInstance(context).setIsNewPlay(true)

        DataPlayer.getInstance()!!.setPlayPosition(position)
        DataPlayer.getInstance()!!.setPlaylist(listFavorite)

        PlayerActivity.launch(context)
    }

    @SuppressLint("RestrictedApi")
    private fun showPopupMenu(position: Int, view: View, song: Song) {

        val menuBuilder = MenuBuilder(context)
        val inflater = MenuInflater(context)
        inflater.inflate(R.menu.song_inside_favorite, menuBuilder)
        val optionsMenu = MenuPopupHelper(context, menuBuilder, view)
        optionsMenu.setForceShowIcon(true)

        menuBuilder.setCallback(object : MenuBuilder.Callback{
            override fun onMenuItemSelected(menu: MenuBuilder, item: MenuItem): Boolean {
                return when (item.itemId) {
                    R.id.play -> {
                        AppConfig.getInstance(context).setCurPosition(position)
                        AppConfig.getInstance(context).setPlaylist(listFavorite)
                        AppConfig.getInstance(context).setIsNewPlay(true)

                        DataPlayer.getInstance()!!.setPlayPosition(position)
                        DataPlayer.getInstance()!!.setPlaylist(listFavorite)

                        PlayerActivity.launch(context)
                        true
                    }
                    R.id.delete -> {
                        delete(song)
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

    fun delete(song: Song) {
        val removeSong = "DELETE FROM favorite WHERE path = '" +  song.path + "'"
        playlistDatabase.querryData(removeSong)
        onClickListerner.setOnclick()
    }
}