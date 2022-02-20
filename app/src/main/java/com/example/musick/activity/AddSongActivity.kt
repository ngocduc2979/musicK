package com.example.musick.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musick.OnClickListerner
import com.example.musick.OnSelectSongListener
import com.example.musick.R
import com.example.musick.Song
import com.example.musick.adapter.AddSongAdapter
import database.MusicDatabase
import kotlinx.android.synthetic.main.add_song_activity.*
import kotlinx.android.synthetic.main.playlist_song_activity.*
import saveData.AppConfig
import java.lang.reflect.Type
import java.net.Proxy

class AddSongActivity: AppCompatActivity(), OnSelectSongListener {

    var playlistDatabase = MusicDatabase(this, "playlist.db", null, 1)
    var listSelectSong = ArrayList<Song>()

    companion object {
        private const val KEY_PLAYLIST_NAME = "play_list_name"
        fun launch(playlistName: String, context: Context) {
            val intent = Intent(context, AddSongActivity::class.java)
            intent.putExtra(KEY_PLAYLIST_NAME, playlistName)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_song_activity)

        playlistName.setOnClickListener {
            onBackPressed()
        }

        setAdapter()
    }

    override fun onResume() {
        super.onResume()
        tvDone.setOnClickListener {
            if (listSelectSong.size != 0) {
                listSelectSong.forEach {
                    addSong(it)
                }
                onBackPressed()
            }
        }
    }

    fun addSong(song: Song) {
        val playlistName = intent.getStringExtra(KEY_PLAYLIST_NAME)
        val addSong = "INSERT INTO '" +  playlistName + "' VALUES (" + "'" + song.songName + "', " +
                "'" + song.artist + "', " +
                "'" + song.path + "', " +
                "'" + song.album + "', " +
                "'" + song.duration + "', " +
                "'" + song.thumbnail + "')"
        playlistDatabase.querryData(addSong)
    }

    private fun setAdapter() {
        val playlistName = intent.getStringExtra(KEY_PLAYLIST_NAME)
        val listAllSong = AppConfig.getInstance(this).getCacheSong()
        val listSongInPlaylist = ArrayList<Song>()
        val listSongNotExists = ArrayList<Song>()
        listSongNotExists.clear()
        listSongInPlaylist.clear()
        val cursor = playlistDatabase.getData("SELECT * FROM '$playlistName'")
        listSongInPlaylist.clear()
        while (cursor.moveToNext()) {
            val name = cursor.getString(0)
            val artist = cursor.getString(1)
            val path = cursor.getString(2)
            val album = cursor.getString(3)
            val duration = cursor.getString(4)
            val thumbnail = cursor.getString(5)
            listSongInPlaylist.add(Song(name, artist, path, album, duration, thumbnail))
        }

        listAllSong.forEach {
            var checkExist = false
            for (i in 0 until listSongInPlaylist.size) {
                if (it.path == listSongInPlaylist[i].path) {
                    checkExist = true
                }
            }
            if (!checkExist) {
                listSongNotExists.add(it)
            }
        }

        addSongView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        addSongView.adapter = AddSongAdapter(listSongNotExists, this, this)
    }

    override fun onSelectSong(list: ArrayList<Song>) {

        listSelectSong = list
        if (listSelectSong.size == 0) {
            tvDone.setTextColor(resources.getColor(R.color.gray))
            tvDone.typeface = Typeface.DEFAULT
        } else {
            tvDone.setTextColor(resources.getColor(R.color.black))
            tvDone.typeface = Typeface.DEFAULT_BOLD
        }
    }

}