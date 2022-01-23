package com.example.musick.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musick.OnClickListerner
import com.example.musick.R
import com.example.musick.Song
import com.example.musick.activity.PlayerActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import database.MusicDatabase
import kotlinx.android.synthetic.main.item_all_tracks.view.*
import saveData.AppConfig
import saveData.DataPlayer
import java.io.File

class RecentlyPlayAdapter(val context: Context, val listRecently: ArrayList<Song>):
    RecyclerView.Adapter<RecentlyPlayAdapter.RecentlyPlayViewHolder>(), OnClickListerner {

    val playlistDatabase = MusicDatabase(context, "playlist.db", null, 1)
    lateinit var bottomSheetDialog: BottomSheetDialog

    class RecentlyPlayViewHolder(view: View): RecyclerView.ViewHolder(view) {
        var imvThumbnail = view.profile_image
        var tvSong          = view.tvTracks
        var tvArtist        = view.tvArtist
        var tracksView      = view.trackView
        var menu            = view.menu
        val playing         = view.playing
        val pause           = view.pause
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentlyPlayViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_all_tracks, parent, false)
        return RecentlyPlayViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecentlyPlayViewHolder, position: Int) {
        holder.tvSong.text = listRecently[position].songName
        holder.tvArtist.text = listRecently[position].artist

        Glide.with(context)
            .load(listRecently[position].thumbnail)
            .placeholder(R.drawable.music_default)
            .centerCrop()
            .into(holder.imvThumbnail)

        if (AppConfig.getInstance(context).getPlayList() != null) {
            setStatePlay(holder, listRecently[position])
        } else {
            holder.playing.visibility = View.GONE
            holder.pause.visibility = View.GONE
            holder.tracksView.setBackgroundResource(R.color.no_color)
        }

        holder.tracksView.setOnClickListener {
            clickTracksView(position)
        }

        holder.menu.setOnClickListener {
            showPopupMenu(position, it, listRecently[position])
        }
    }

    override fun getItemCount(): Int {
        if (listRecently.size <= 5) {
            return listRecently.size
        } else return 5
    }

    @SuppressLint("ResourceAsColor")
    private fun setStatePlay(holder: RecentlyPlayViewHolder, song: Song) {
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

    @SuppressLint("RestrictedApi")
    private fun showPopupMenu(position: Int, view: View, song: Song) {

        val menuBuilder = MenuBuilder(context)
        val inflater = MenuInflater(context)
        inflater.inflate(R.menu.song_detail_menu, menuBuilder)
        val optionsMenu = MenuPopupHelper(context, menuBuilder, view)
        optionsMenu.setForceShowIcon(true)

        menuBuilder.setCallback(object : MenuBuilder.Callback{
            override fun onMenuItemSelected(menu: MenuBuilder, item: MenuItem): Boolean {
                return when (item.itemId) {
                    R.id.play -> {
                        clickTracksView(position)
                        true
                    }
                    R.id.add_favorite -> {
                        addFavorite(song)
                        true
                    }
                    R.id.add_to -> {
                        addToPlaylist(song)
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

    private fun clickTracksView(position: Int) {
        AppConfig.getInstance(context).setCurPosition(position)
        AppConfig.getInstance(context).setPlaylist(listRecently)
        AppConfig.getInstance(context).setIsNewPlay(true)

        DataPlayer.getInstance()!!.setPlayPosition(position)
        DataPlayer.getInstance()!!.setPlaylist(listRecently)

        PlayerActivity.launch(context)
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

    @SuppressLint("ResourceType")
    private fun addToPlaylist(song: Song) {

        val view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_dialog, null)

        bottomSheetDialog = BottomSheetDialog(context, R.style.MaterialDialogSheet)
        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.setCancelable(true)
        bottomSheetDialog.getWindow()!!.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        bottomSheetDialog.getWindow()!!.setGravity(Gravity.BOTTOM)
        bottomSheetDialog.show()

        val listPlaylist = ArrayList<String>()
        listPlaylist.clear()
        val getAllPlaylist = "SELECT * FROM table_list"
        val cursor = playlistDatabase.getData(getAllPlaylist)
        while (cursor.moveToNext()) {
            listPlaylist.add(cursor.getString(0))
        }

        val toolbar = bottomSheetDialog.findViewById<View>(R.id.toolbar)
        val listView = bottomSheetDialog.findViewById<RecyclerView>(R.id.bottomSheetView)

        toolbar!!.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        listView!!.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        val bottomDialogAdapter = BottomDialogAdapter(listPlaylist, context, song, this)

        listView.adapter = bottomDialogAdapter
    }

    override fun setOnclick() {
        bottomSheetDialog.dismiss()
    }

}