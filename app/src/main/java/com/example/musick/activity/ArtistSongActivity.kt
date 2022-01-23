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
import com.example.musick.PlayerService
import com.example.musick.R
import com.example.musick.Song
import com.example.musick.adapter.ArtistSongAdapter
import kotlinx.android.synthetic.main.playlist_song_activity.*
import java.util.*
import kotlin.collections.ArrayList

class ArtistSongActivity: AppCompatActivity() {

    var listSong = ArrayList<Song>()

    companion object {
        private val KEY_LIST_SONG = "list_song"
        private val KEY_ARTIST_NAME = "artist"
        fun launch(context: Context, listSong: ArrayList<Song>, artistName: String) {
            val intent = Intent(context, ArtistSongActivity::class.java)
            intent.putParcelableArrayListExtra(KEY_LIST_SONG, listSong)
            intent.putExtra(KEY_ARTIST_NAME, artistName)
            context.startActivity(intent)
        }
    }

    private val artistSongBroadCast: BroadcastReceiver = object : BroadcastReceiver(){
        @SuppressLint("NotifyDataSetChanged")
        override fun onReceive(p0: Context, intent: Intent) {

            val action = intent.action

            if (action.equals(PlayerService.ACTION_UPDATE_STATE_PLAY)){
                setAdapter()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.playlist_song_activity)

        addSong.visibility = View.GONE

        toolbar.setOnClickListener {
            onBackPressed()
        }



        listSong = intent.getParcelableArrayListExtra(KEY_LIST_SONG)!!
        val artistName = intent.getStringExtra(KEY_ARTIST_NAME).toString()

        setView(artistName)
    }

    override fun onResume() {
        super.onResume()
        setAdapter()
    }

    override fun onStart() {
        super.onStart()
        val intentFilter = IntentFilter()
        intentFilter.addAction(PlayerService.ACTION_UPDATE_STATE_PLAY)
        registerReceiver(artistSongBroadCast, intentFilter)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(artistSongBroadCast)
    }

    private fun setAdapter() {
        listView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        listView.adapter = ArtistSongAdapter(listSong, this)
    }

    @SuppressLint("SetTextI18n")
    private fun setView(artistName: String) {
        if (listSong.size != 0) {
            tvZeroSong.visibility = View.GONE
        } else {
            tvZeroSong.visibility = View.VISIBLE
        }

        Glide.with(this)
            .load(listSong[Random().nextInt(listSong.size)].thumbnail)
            .placeholder(R.drawable.music_default)
            .centerCrop()
            .into(cover)
        playlist_name.text = artistName
        tracks.text = listSong.size.toString() + " " + resources.getString(R.string.tracks)
    }

}