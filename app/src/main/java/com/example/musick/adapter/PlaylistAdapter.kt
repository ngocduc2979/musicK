package com.example.musick.adapter

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musick.OnPlaylistListerner
import com.example.musick.R
import com.example.musick.Song
import com.example.musick.activity.PlayerActivity
import com.example.musick.activity.PlaylistSongActivity
import database.MusicDatabase
import kotlinx.android.synthetic.main.item_playlist.view.*
import saveData.AppConfig
import saveData.DataPlayer

class PlaylistAdapter(val listPlaylist: ArrayList<String>, val context: Context, var onPlaylistListerner: OnPlaylistListerner):
    RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>(){

    val listSong = ArrayList<Song>()
    var playlistDatabase = MusicDatabase(context, "playlist.db", null, 1)

    class PlaylistViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val imvCover        = view.profile_playlist_image
        val tvPlaylistName  = view.tvPlaylistName
        val tvTracks        = view.tvTracksNumber
        val menu            = view.three_dot
        val trackView       = view.trackView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_playlist, parent, false)
        return PlaylistViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {

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

//        checkExists(position)

        holder.tvPlaylistName.text = listPlaylist[position]
        holder.tvTracks.text = listSong.size.toString() + " Bài hát"

        if (listSong.size != 0) {
            Glide.with(context)
                .load(listSong[0].thumbnail)
                .placeholder(R.drawable.music_default)
                .centerCrop()
                .into(holder.imvCover)
        }

        holder.menu.setOnClickListener {
            showPupupMenu(it, position)
        }

        holder.trackView.setOnClickListener {
            PlaylistSongActivity.launch(context, listPlaylist[position])
        }
    }

    override fun getItemCount(): Int {
        return listPlaylist.size
    }

    @SuppressLint("RestrictedApi")
    private fun showPupupMenu(view: View, pos: Int) {

        val menuBuilder = MenuBuilder(context)
        val inflater = MenuInflater(context)
        inflater.inflate(R.menu.playlist_menu, menuBuilder)
        val optionsMenu = MenuPopupHelper(context, menuBuilder, view)
        optionsMenu.setForceShowIcon(true)

        menuBuilder.setCallback(object : MenuBuilder.Callback{
            override fun onMenuItemSelected(menu: MenuBuilder, item: MenuItem): Boolean {
                return when (item.itemId) {
                    R.id.rename -> {
                        renamePlaylist(pos)
                        true
                    }
                    R.id.delete -> {
                        deletePlaylist(pos)
                        true
                    }
                    else -> false
                }
            }
            override fun onMenuModeChange(menu: MenuBuilder) {
            }
        })
        optionsMenu.show();
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun deletePlaylist(pos: Int) {
        val tableName = listPlaylist[pos]
        val deleteTable = "DROP TABLE '$tableName'"
        playlistDatabase.querryData(deleteTable)

        val deleteTableList = "DELETE FROM table_list WHERE name = '$tableName'"
        playlistDatabase.querryData(deleteTableList)
        onPlaylistListerner.onPlaylist()
        notifyDataSetChanged()
    }

    private fun checkExists(pos: Int) {
        val lisAllTracks = AppConfig.getInstance(context).getCacheSong()
        listSong.forEach {
            var checkExist = false
            for (i in lisAllTracks.indices){
                if (it.path == lisAllTracks[i].path) {
                    checkExist = true
                }
            }
            if (!checkExist) {
                val deleteSong = "DELETE FROM '" + listPlaylist[pos] + "' WHERE path = '" +  it.path + "'"
                playlistDatabase.querryData(deleteSong)
                listSong.remove(it)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun renamePlaylist(pos: Int) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.custom_create_playlist_dialog)
        dialog.setCancelable(true)
        val window = dialog.window
        window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT)

        val close = dialog.findViewById<View>(R.id.close)
        val save = dialog.findViewById<Button>(R.id.btSave)
        val edtext = dialog.findViewById<EditText>(R.id.edtPlaylistName)

        val tvTitle = dialog.findViewById<TextView>(R.id.title)

        val tableName = listPlaylist[pos]
        tvTitle.setText("Đổi tên playlist")
        edtext.setText(tableName)
        close.setOnClickListener(View.OnClickListener { dialog.dismiss() })
        save.setOnClickListener(View.OnClickListener {
            val rename =
                "ALTER TABLE '$tableName' RENAME TO '" + edtext.text.toString() + "'"
            playlistDatabase.querryData(rename)

            val renameTableList = "UPDATE table_list SET name = '" + edtext.text.toString() + "' " +
                    "WHERE name = '$tableName'"
            playlistDatabase.querryData(renameTableList)
            dialog.dismiss()

            onPlaylistListerner.onPlaylist()
            notifyDataSetChanged()
        })
        dialog.show()
    }

}