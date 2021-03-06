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

class AllTracksAdapter(val listAllTracks: List<Song>, val context: Context):
    RecyclerView.Adapter<AllTracksAdapter.AllTracksViewHolder>(), OnClickListerner {

    lateinit var bottomSheetDialog: BottomSheetDialog

    var playlistDatabase = MusicDatabase(context, "playlist.db", null, 1)

    class AllTracksViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val imvImageCover   = view.profile_image
        val tvSongName      = view.tvTracks
        val tvArtist        = view.tvArtist
        val tracksView       = view.trackView
        val menu            = view.menu
        val playing         = view.playing
        val pause           = view.pause
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllTracksAdapter.AllTracksViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_all_tracks, parent, false)
        return AllTracksViewHolder(view)
    }

    override fun onBindViewHolder(holder: AllTracksViewHolder, position: Int) {

        holder.tvSongName.text = listAllTracks[position].songName
        holder.tvArtist.text = listAllTracks[position].artist

        Glide.with(context)
            .load(listAllTracks[position].thumbnail)
            .placeholder(R.drawable.music_default)
            .error("image error")
            .centerCrop()
            .into(holder.imvImageCover)

        if (AppConfig.getInstance(context).getPlayList() != null) {
            setStatePlay(holder, listAllTracks[position])
        } else {
            holder.playing.visibility = View.GONE
            holder.pause.visibility = View.GONE
            holder.tracksView.setBackgroundResource(R.color.no_color)
        }

        holder.tracksView.setOnClickListener {
            clickTracksView(position)
        }

        holder.menu.setOnClickListener {
            showPopupMenu(position, it, listAllTracks[position])
        }

    }

    override fun getItemCount(): Int {
        return listAllTracks.size
    }

    @SuppressLint("ResourceAsColor")
    private fun setStatePlay(holder: AllTracksViewHolder, song: Song) {
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
        AppConfig.getInstance(context).setPlaylist(listAllTracks)
        AppConfig.getInstance(context).setIsNewPlay(true)

        DataPlayer.getInstance()!!.setPlayPosition(position)
        DataPlayer.getInstance()!!.setPlaylist(listAllTracks)

        PlayerActivity.launch(context)
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
                        val listPlaylist = ArrayList<String>()
                        listPlaylist.clear()
                        val selectTable = "SELECT * FROM table_list"

                        val cursor = playlistDatabase.getData(selectTable)
                        while (cursor.moveToNext()) {
                            listPlaylist.add(cursor.getString(0))
                        }
                        if (listPlaylist.size != 0) {
                            addToPlaylist(song)
                        } else {
                            Toast.makeText(context, context.getString(R.string.playlistIsZero), Toast.LENGTH_LONG).show()
                        }
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
            Toast.makeText(context, "???? th??m v??o y??u th??ch", Toast.LENGTH_LONG).show()
            checkExist = true
        } else {
            Toast.makeText(context, "'" + song.songName + "'" + " ???? c?? trong Y??u th??ch", Toast.LENGTH_LONG).show()
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