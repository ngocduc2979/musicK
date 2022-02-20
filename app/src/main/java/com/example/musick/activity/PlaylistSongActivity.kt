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
import com.example.musick.fragment.FavoriteFragment
import database.MusicDatabase
import kotlinx.android.synthetic.main.playlist_song_activity.*
import saveData.AppConfig
import saveData.DataPlayer
import java.util.*
import kotlin.collections.ArrayList

class PlaylistSongActivity:AppCompatActivity(), OnClickListerner {

    private var isPlaying = false
    var listSong = ArrayList<Song>()
    var playlistDatabase = MusicDatabase(this, "playlist.db", null, 1)
    lateinit var playlistName: String
    lateinit var playlistSongAdapter: PlaylistSongAdapter

    companion object {
        private const val KEY_PLAYLIST_NAME = "name"
        fun launch(context: Context, playlistName: String) {
            val intent = Intent(context, PlaylistSongActivity::class.java)
            intent.putExtra(KEY_PLAYLIST_NAME, playlistName)
            context.startActivity(intent)
        }
    }

    private val playlistSongBroadCast: BroadcastReceiver = object : BroadcastReceiver(){
        @SuppressLint("NotifyDataSetChanged")
        override fun onReceive(context: Context, intent: Intent) {

            val action = intent.action

            if (action.equals(PlayerService.ACTION_UPDATE_STATE_PLAY)){
                setAdapter()
                isPlaying = intent.getBooleanExtra(PlayerService.EXTRA_STATE_PLAY, false)
                if (isPlaying) {
                    play_mini_player.setBackgroundResource(R.drawable.ic_baseline_pause_24_white)
                } else {
                    play_mini_player.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24_white)
                }
                AppConfig.getInstance(context).setStatePlay(isPlaying)
            } else if (action == PlayerService.ACTION_NEW_PLAY) {
                layout_mini_player.visibility = View.VISIBLE
            } else if (action == PlayerService.ACTION_UPDATE_SONG_INFO) {
                setInfoSong(DataPlayer.getInstance()!!.getCurrentSong())
            } else if (action == PlayerService.ACTION_CLOSE_PLAYER) {
                layout_mini_player.visibility = View.GONE
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.playlist_song_activity)

        playlistName = intent.getStringExtra(KEY_PLAYLIST_NAME).toString()
        playlistSongAdapter = PlaylistSongAdapter(listSong, this, playlistName, this)

        toolbar.setOnClickListener {
            onBackPressed()
        }

        addSong.setOnClickListener {
            addSongToPlaylist()
        }

        getListSong()
        setAdapter()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        getListSong()
        playlistSongAdapter.notifyDataSetChanged()

        updateStateAndSong()
        setDefaultPlaySong()
        updateStatePlay()
        upDateCurrentSong()

        layout_mini_player.setOnClickListener(View.OnClickListener { v ->
            AppConfig.getInstance(v.context).setIsNewPlay(false)
            PlayerActivity.launch(v.context)
        })

        play_mini_player.setOnClickListener(View.OnClickListener { v ->
            val intent = Intent(v.context, PlayerService::class.java)
            intent.action = PlayerService.ACTION_PLAY_PAUSE_MUSIC
            AppConfig.getInstance(v.context).setIsNewPlay(false)
            startService(intent)
            sendBroadcast(intent)
        })
    }

    override fun onStart() {
        super.onStart()
        val intentFilter = IntentFilter()
        intentFilter.addAction(PlayerService.ACTION_NEW_PLAY)
        intentFilter.addAction(PlayerService.ACTION_UPDATE_STATE_PLAY)
        intentFilter.addAction(PlayerService.ACTION_UPDATE_SONG_INFO)
        intentFilter.addAction(PlayerService.ACTION_CLOSE_PLAYER)
        intentFilter.addAction(FavoriteFragment.ACTION_CLICK_FAVORITE)
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

//        checkExists(listSong, playlistName)

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

    private fun checkExists(list: ArrayList<Song>, table_name: String) {
        val lisAllTracks = AppConfig.getInstance(this).getCacheSong()

        list.forEach {
            var checkExist = false
            for (i in lisAllTracks.indices){
                if (it.path == lisAllTracks[i].path) {
                    checkExist = true
                }
            }
            if (!checkExist) {
                val deleteSong = "DELETE FROM '" + table_name + "' WHERE path = '" +  it.path + "'"
                playlistDatabase.querryData(deleteSong)
                list.remove(it)
            }
        }
    }

    fun setAdapter() {
        listView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        listView.adapter = playlistSongAdapter
    }

    private fun updateStateAndSong() {
        var checkExist = false
        val listAllSong = ArrayList<Song>()
        listAllSong.clear()
        listAllSong.addAll(AppConfig.getInstance(this).getCacheSong())
        listAllSong.addAll(AppConfig.getInstance(this).getCacheListSongAPI())

        if (AppConfig.getInstance(this).getPlayList() != null) {
            val song = AppConfig.getInstance(this).getCurrentSong()

            for (i in listAllSong.indices){
                if (song.path == listAllSong[i].path) {
                    checkExist = true
                }
            }

            if (AppConfig.getInstance(applicationContext).getSongName() == null) {
                layout_mini_player.visibility = View.GONE
            } else {
                if (!checkExist) {
                    layout_mini_player.visibility = View.GONE
                } else {
                    layout_mini_player.visibility = View.VISIBLE
                }
            }
        } else {
            layout_mini_player.visibility = View.GONE
        }
    }

    private fun setDefaultPlaySong() {
        if (AppConfig.getInstance(applicationContext).getPlayList() != null) {
            DataPlayer.getInstance()!!.setPlayPosition(AppConfig.getInstance(applicationContext).getCurposition())
            DataPlayer.getInstance()!!.setPlaylist(AppConfig.getInstance(applicationContext).getPlayList() as List<Song>)
        }
    }

    private fun setInfoSong(song: Song) {
        songName_mini.text = song.songName
        artist_mini.text = song.artist

        Glide.with(this)
            .load(song.thumbnail)
            .placeholder(R.drawable.music_default)
            .error("image error")
            .centerCrop()
            .into(profile_image_main)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateStatePlay() {
        isPlaying = AppConfig.getInstance(this).getStatePlay()
        if (isPlaying) {
            play_mini_player.setBackgroundResource(R.drawable.ic_baseline_pause_24_white)
        } else {
            play_mini_player.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24_white)
        }

        val listFavorite = ArrayList<Song>()
        listFavorite.clear()

        val cursor = playlistDatabase.getData("SELECT * FROM favorite")
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
            if (it.path == DataPlayer.getInstance()!!.getCurrentSong().path) {
                checkExist = true
            }
        }

        if (checkExist) {
            favorite_mini_player.setBackgroundResource(R.drawable.ic_baseline_favorite_24_orange)
        } else {
            favorite_mini_player.setBackgroundResource(R.drawable.ic_baseline_favorite_24_white)
        }

        favorite_mini_player.setOnClickListener {
            val song = DataPlayer.getInstance()!!.getCurrentSong()

            if (checkExist) {
                val removeSong = "DELETE FROM favorite WHERE path = " +  "'" +  song.path + "'"
                playlistDatabase.querryData(removeSong)
                checkExist = false
                favorite_mini_player.setBackgroundResource(R.drawable.ic_baseline_favorite_24_white)
            } else {
                val insertSong = "INSERT INTO favorite VALUES (" + "'" + song.songName + "', " +
                        "'" + song.artist + "', " +
                        "'" + song.path + "', " +
                        "'" + song.album + "', " +
                        "'" + song.duration + "', " +
                        "'" + song.thumbnail + "')"
                playlistDatabase.querryData(insertSong)
                checkExist = true
                favorite_mini_player.setBackgroundResource(R.drawable.ic_baseline_favorite_24_orange)
            }

            val intent = Intent(FavoriteFragment.ACTION_CLICK_FAVORITE)
            sendBroadcast(intent)
        }
    }

    private fun upDateCurrentSong() {
        if (AppConfig.getInstance(this).getPlayList() != null) {
            val song = AppConfig.getInstance(this).getCurrentSong()
            songName_mini.text = song.songName
            artist_mini.text = song.artist

            Glide.with(this)
                .load(song.thumbnail)
                .placeholder(R.drawable.music_default)
                .error("image error")
                .centerCrop()
                .into(profile_image_main)
        }
    }

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
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

        tracks.text = listSong.size.toString() + " " + resources.getString(R.string.tracks)

        if (listSong.size != 0) {
            tvZeroSong.visibility = View.GONE
        } else {
            tvZeroSong.visibility = View.VISIBLE
        }
        playlistSongAdapter.notifyDataSetChanged()
    }
}