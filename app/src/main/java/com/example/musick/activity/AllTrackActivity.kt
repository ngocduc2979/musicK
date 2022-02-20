package com.example.musick.activity

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.musick.PlayerService
import com.example.musick.R
import com.example.musick.Song
import com.example.musick.adapter.AllTracksAdapter
import com.example.musick.fragment.FavoriteFragment
import database.MusicDatabase
import kotlinx.android.synthetic.main.all_track_activity.*
import saveData.AppConfig
import saveData.DataPlayer
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class AllTrackActivity: AppCompatActivity() {

    private var isPlaying = false
    private val playlistDatabase = MusicDatabase(this, "playlist.db", null, 1)
    private lateinit var allTracksAdapter: AllTracksAdapter
    private var listAllSong = ArrayList<Song>()

    private val allTracksActivity: BroadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("NotifyDataSetChanged")
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == PlayerService.ACTION_NEW_PLAY) {
                layout_mini_player.visibility = View.VISIBLE
            } else if (action == PlayerService.ACTION_UPDATE_STATE_PLAY) {
                isPlaying = intent.getBooleanExtra(PlayerService.EXTRA_STATE_PLAY, false)
                if (isPlaying) {
                    play_mini_player.setBackgroundResource(R.drawable.ic_baseline_pause_24_white)
                } else {
                    play_mini_player.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24_white)
                }
                AppConfig.getInstance(context).setStatePlay(isPlaying)
                allTracksAdapter.notifyDataSetChanged()

            } else if (action == PlayerService.ACTION_UPDATE_SONG_INFO) {
                setInfoSong(DataPlayer.getInstance()!!.getCurrentSong())
            } else if (action == PlayerService.ACTION_CLOSE_PLAYER) {
                layout_mini_player.visibility = View.GONE
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.all_track_activity)

        listAllSong = AppConfig.getInstance(this).getCacheSong() as ArrayList<Song>
        allTracksAdapter = AllTracksAdapter(listAllSong, this)

        initListWithTracks(listAllSong)

        toolbar.setOnClickListener {
            onBackPressed()
        }

        sort.setOnClickListener {
            showPopupMenu(it, this)
        }

        checkExistInDevice()
    }

    override fun onStart() {
        super.onStart()
        val intentFilter = IntentFilter()
        intentFilter.addAction(PlayerService.ACTION_NEW_PLAY)
        intentFilter.addAction(PlayerService.ACTION_UPDATE_STATE_PLAY)
        intentFilter.addAction(PlayerService.ACTION_UPDATE_SONG_INFO)
        intentFilter.addAction(PlayerService.ACTION_CLOSE_PLAYER)
        intentFilter.addAction(FavoriteFragment.ACTION_CLICK_FAVORITE)
        registerReceiver(allTracksActivity, intentFilter)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(allTracksActivity)
    }

    override fun onResume() {
        super.onResume()

        initTracks()
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
            .load(File(song.thumbnail))
            .centerCrop()
            .placeholder(R.drawable.music_default)
            .into(profile_image_alltracks)
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
        }
    }

    private fun upDateCurrentSong() {
        if (AppConfig.getInstance(this).getPlayList() != null) {
            val song = AppConfig.getInstance(this).getCurrentSong()
            songName_mini.text = song.songName
            artist_mini.text = song.artist

            Glide.with(this)
                .load(song.thumbnail)
                .centerCrop()
                .placeholder(R.drawable.music_default)
                .into(profile_image_alltracks)
        }
    }

    @SuppressLint("RestrictedApi")
    fun showPopupMenu(view: View, context: Context) {
        val menuBuilder = MenuBuilder(this)
        val inflater = MenuInflater(this)
        inflater.inflate(R.menu.sort_menu, menuBuilder)
        val optionsMenu = MenuPopupHelper(this, menuBuilder, view)
        optionsMenu.setForceShowIcon(true)

        menuBuilder.setCallback(object : MenuBuilder.Callback{
            override fun onMenuItemSelected(menu: MenuBuilder, item: MenuItem): Boolean {
                return when (item.itemId) {
                    R.id.sort_by_a_z -> {
                        AppConfig.getInstance(context).setSort("a-z")
                        initTracks()
                        true
                    }
                    R.id.sort_by_z_a -> {
                        AppConfig.getInstance(context).setSort("z-a")
                        initTracks()
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

    @SuppressLint("NotifyDataSetChanged")
    private fun initTracks() {

        if (AppConfig.getInstance(this).getSort().equals("a-z")) {
            Collections.sort(listAllSong, object : Comparator<Song> {
                override fun compare(p0: Song, p1: Song): Int {
                    return p0.songName.compareTo(p1.songName)
                }
            })
        } else if (AppConfig.getInstance(this).getSort().equals("z-a")) {
            Collections.sort(listAllSong, object : Comparator<Song> {
                override fun compare(p0: Song, p1: Song): Int {
                    return p1.songName.compareTo(p0.songName)
                }
            })
        }

        allTracksAdapter.notifyDataSetChanged()
    }

    private fun initListWithTracks(list: List<Song>) {
        tracksView.layoutManager = LinearLayoutManager(this)
        tracksView.adapter = allTracksAdapter
    }

    private fun checkExistInDevice() {
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
}