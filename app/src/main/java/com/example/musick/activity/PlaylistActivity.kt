package com.example.musick.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musick.OnPlaylistListerner
import com.example.musick.R
import com.example.musick.adapter.PlaylistAdapter
import database.MusicDatabase
import kotlinx.android.synthetic.main.playlist_activity.*


class PlaylistActivity: AppCompatActivity(), OnPlaylistListerner {

    var listPlaylist = ArrayList<String>()
    val playlistDatabase = MusicDatabase(this, "playlist.db", null, 1)
    lateinit var playlistAdapter: PlaylistAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.playlist_activity)

        toolbar.setOnClickListener {
            onBackPressed()
        }

        val sql = "CREATE TABLE IF NOT EXISTS table_list" +
                "(name text PRIMARY KEY)"
        playlistDatabase.querryData(sql)

        setAdapter()

    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()

        listPlaylist.clear()

        val selectTable = "SELECT * FROM table_list"

        val cursor = playlistDatabase.getData(selectTable)
        while (cursor.moveToNext()) {
            listPlaylist.add(cursor.getString(0))
        }

//        listPlaylist.forEach {
//            Log.wtf("PlaylistAdapter", it)
//        }

        clickCreatePlaylist()
        playlistAdapter.notifyDataSetChanged()
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onPostResume() {
        super.onPostResume()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setAdapter() {
        playlistView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        playlistAdapter = PlaylistAdapter(listPlaylist, this, this)
        playlistView.adapter = playlistAdapter
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun clickCreatePlaylist() {
        create_playlist_view.setOnClickListener {
            val dialog = Dialog(this)
            dialog.setContentView(R.layout.custom_create_playlist_dialog)
            dialog.setCancelable(true)
            val window = dialog.window
            window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT)

            dialog.show()

            val close = dialog.findViewById<View>(R.id.close)
            val save = dialog.findViewById<Button>(R.id.btSave)

            close.setOnClickListener {
                dialog.dismiss()
            }

            save.setOnClickListener {
                createNewPlaylist(dialog)
                playlistAdapter.notifyDataSetChanged()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun createNewPlaylist(dialog: Dialog) {
        val edText = dialog.findViewById<EditText>(R.id.edtPlaylistName)
        val tableName = edText.text.toString()
        var checkExist = false

        listPlaylist.forEach {
            if (it == tableName) {
                checkExist = true
            }
        }

        if (!checkExist) {
            val sql = "CREATE TABLE IF NOT EXISTS " + "'" + tableName + "' " +
                    "(songName text PRIMARY KEY, artist text, path text, album text, duration text, " +
                    "thumbnail text)"
            playlistDatabase.querryData(sql)

            val addTable = "INSERT INTO table_list VALUES ('$tableName')"
            playlistDatabase.querryData(addTable)

            val cursor = playlistDatabase.getData("SELECT * FROM table_list")
            listPlaylist.clear()
            while (cursor.moveToNext()) {
                listPlaylist.add(cursor.getString(0))
            }
            playlistAdapter = PlaylistAdapter(listPlaylist, this, this)

            dialog.dismiss()
        } else {
            Toast.makeText(this, "Playlist đã tồn tại", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onPlaylist() {

        val cursor = playlistDatabase.getData("SELECT * FROM table_list")
        listPlaylist.clear()
        while (cursor.moveToNext()) {
            listPlaylist.add(cursor.getString(0))
        }

        playlistAdapter = PlaylistAdapter(listPlaylist, this, this)
        playlistAdapter.notifyDataSetChanged()
    }

}