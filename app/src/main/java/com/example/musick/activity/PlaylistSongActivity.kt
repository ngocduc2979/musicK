package com.example.musick.activity

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.musick.OnClickListerner
import com.example.musick.PlayerService
import com.example.musick.R
import com.example.musick.Song
import com.example.musick.adapter.PlaylistSongAdapter
import database.MusicDatabase
import kotlinx.android.synthetic.main.playlist_song_activity.*
import java.util.*
import kotlin.collections.ArrayList

class PlaylistSongActivity:AppCompatActivity(), OnClickListerner {

    var listSong = ArrayList<Song>()
    var playlistDatabase = MusicDatabase(this, "playlist.db", null, 1)
    lateinit var playlistName: String

    companion object {
        private val KEY_PLAYLIST_NAME = "name"
        fun launch(context: Context, playlistName: String) {
            val intent = Intent(context, PlaylistSongActivity::class.java)
            intent.putExtra(KEY_PLAYLIST_NAME, playlistName)
            context.startActivity(intent)
        }
    }

    private val playlistSongBroadCast: BroadcastReceiver = object : BroadcastReceiver(){
        @SuppressLint("NotifyDataSetChanged")
        override fun onReceive(p0: Context, intent: Intent) {

            val action = intent.action

            if (action.equals(PlayerService.ACTION_UPDATE_STATE_PLAY)){
                setAdapter()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.playlist_song_activity)

        toolbar.setOnClickListener {
            onBackPressed()
        }

        addSong.setOnClickListener {
            addSongToPlaylist()
        }

        playlistName = intent.getStringExtra(KEY_PLAYLIST_NAME).toString()

        getListSong()
    }

    override fun onResume() {
        super.onResume()
        getListSong()
        setAdapter()
    }

    override fun onStart() {
        super.onStart()
        val intentFilter = IntentFilter()
        intentFilter.addAction(PlayerService.ACTION_UPDATE_STATE_PLAY)
        registerReceiver(playlistSongBroadCast, intentFilter)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(playlistSongBroadCast)
    }

    private fun addSongToPlaylist() {
        AddSongActivity.launch(intent.getStringExtra(KEY_PLAYLIST_NAME)!!, this)
    }

    @SuppressLint("SetTextI18n")
    fun getListSong() {
        val cursor = playlistDatabase.getData("SELECT * FROM '$playlistName'")

        listSong.clear()
        while (cursor.moveToNext()) {
            val name = cursor.getString(0)
            val artist = cursor.getString(1)
            val path = cursor.getString(2)
            val album = cursor.getString(3)
            val duration = cursor.getString(4)
            val thumbnail = cursor.getString(5)
            listSong.add(Song(name, artist, path, album, duration, thumbnail))
        }

        if (listSong.size != 0) {
            tvZeroSong.visibility = View.GONE

            Glide.with(this)
                .load(listSong[Random().nextInt(listSong.size)].thumbnail)
                .placeholder(R.drawable.music_default)
                .centerCrop()
                .into(cover)
        } else {
            tvZeroSong.visibility = View.VISIBLE
        }

        playlist_name.text = playlistName
        tracks.text = listSong.size.toString() + " " + resources.getString(R.string.tracks)


    }

    fun setAdapter() {
        listView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        val playlistSongAdapter = PlaylistSongAdapter(listSong, this, playlistName, this)
        listView.adapter = playlistSongAdapter
    }

    override fun setOnclick() {
        val cursor = playlistDatabase.getData("SELECT * FROM '$playlistName'")
        listSong.clear()
        while (cursor.moveToNext()) {
            val name = cursor.getString(0)
            val artist = cursor.getString(1)
            val path = cursor.getString(2)
            val album = cursor.getString(3)
            val duration = cursor.getString(4)
            val thumbnail = cursor.getString(5)
            listSong.add(Song(name, artist, path, album, duration, thumbnail))
        }

        if (listSong.size != 0) {
            tvZeroSong.visibility = View.GONE
        } else {
            tvZeroSong.visibility = View.VISIBLE
        }
        setAdapter()
    }
}