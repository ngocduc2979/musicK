package com.example.musick.adapter

import android.annotation.SuppressLint
import android.content.Context
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

class SearchAdapter(val listSearch: List<Song>, val context: Context):
    RecyclerView.Adapter<SearchAdapter.SearchViewHolder>(), OnClickListerner {

    lateinit var bottomSheetDialog: BottomSheetDialog
    var playlistDatabase = MusicDatabase(context, "playlist.db", null, 1)

    class SearchViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val imvImageCover   = view.profile_image
        val tvSongName      = view.tvTracks
        val tvArtist        = view.tvArtist
        val tracksView       = view.trackView
        val menu            = view.menu
        val playing         = view.playing
        val pause           = view.pause
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_all_tracks, parent, false)
        return SearchViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        holder.tvSongName.text = listSearch[position].songName
        holder.tvArtist.text = listSearch[position].artist

        holder.pause.visibility = View.GONE
        holder.playing.visibility = View.GONE

        Glide.with(context)
            .load(listSearch[position].thumbnail)
            .placeholder(R.drawable.music_default)
            .error("image error")
            .centerCrop()
            .into(holder.imvImageCover)

        holder.tracksView.setOnClickListener {
            clickTracksView(position)
        }

        holder.menu.setOnClickListener {
            showPopupMenu(position, it, listSearch[position])
        }
    }

    override fun getItemCount(): Int {
        return listSearch.size
    }

    private fun clickTracksView(position: Int) {

        val list = ArrayList<Song>()
        list.clear()
        list.add(listSearch[position])

        AppConfig.getInstance(context).setCurPosition(0)
        AppConfig.getInstance(context).setPlaylist(list)
        AppConfig.getInstance(context).setIsNewPlay(true)

        DataPlayer.getInstance()!!.setPlayPosition(0)
        DataPlayer.getInstance()!!.setPlaylist(list)

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