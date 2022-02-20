package com.example.musick.activity

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.musick.*
import com.example.musick.adapter.AlbumSongAdapter
import com.example.musick.fragment.FavoriteFragment
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import database.MusicDatabase
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.album_spring_activity.*
import kotlinx.android.synthetic.main.album_spring_activity.artist_mini
import kotlinx.android.synthetic.main.album_spring_activity.favorite_mini_player
import kotlinx.android.synthetic.main.album_spring_activity.layout_mini_player
import kotlinx.android.synthetic.main.album_spring_activity.play_mini_player
import kotlinx.android.synthetic.main.album_spring_activity.profile_image_main
import kotlinx.android.synthetic.main.album_spring_activity.songName_mini
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import saveData.AppConfig
import saveData.DataPlayer

class AlbumSpringActivity: AppCompatActivity() {

    private var isPlaying = false
    private val playlistDatabase = MusicDatabase(this, "playlist.db", null, 1)

    private var disposables: CompositeDisposable = CompositeDisposable()
    var listAlbumSong = ArrayList<Song>()
    lateinit var albumSongAdapter: AlbumSongAdapter

    companion object {
        private val KEY_ALBUM = "album"
        fun launch(context: Context, album: Album) {
            val intent = Intent(context, AlbumSpringActivity::class.java)
            val bundle = Bundle()
            bundle.putSerializable(KEY_ALBUM, album)
            intent.putExtras(bundle)
            context.startActivity(intent)
        }
    }

    private val albumSongBroadCast: BroadcastReceiver = object : BroadcastReceiver(){
        @SuppressLint("NotifyDataSetChanged")
        override fun onReceive(context: Context, intent: Intent) {

            val action = intent.action

            if (action.equals(PlayerService.ACTION_UPDATE_STATE_PLAY)){
                albumSongAdapter.notifyDataSetChanged()
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
        setContentView(R.layout.album_spring_activity)

        loading.visibility = View.GONE
        tvZeroSong.visibility = View.GONE

        val album: Album = intent.getSerializableExtra(KEY_ALBUM) as Album
        val playlistName = album.name
        val coverImage = album.cover

        Glide.with(this)
            .load(coverImage)
            .placeholder(R.drawable.music_default)
            .centerCrop()
            .into(cover)
        playlist_name.text = playlistName


        toolbar.setOnClickListener {
            onBackPressed()
        }
        getListAlbum()

    }

    override fun onStart() {
        super.onStart()
        val intentFilter = IntentFilter()
        intentFilter.addAction(PlayerService.ACTION_NEW_PLAY)
        intentFilter.addAction(PlayerService.ACTION_UPDATE_STATE_PLAY)
        intentFilter.addAction(PlayerService.ACTION_UPDATE_SONG_INFO)
        intentFilter.addAction(PlayerService.ACTION_CLOSE_PLAYER)
        intentFilter.addAction(FavoriteFragment.ACTION_CLICK_FAVORITE)
        registerReceiver(albumSongBroadCast, intentFilter)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(albumSongBroadCast)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        setAdapter(listAlbumSong)

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

    @SuppressLint("SetTextI18n")
    private fun getListAlbum() {
        val album: Album = intent.getSerializableExtra(KEY_ALBUM) as Album
        val id = album.id
        val playlistName = album.name
        val coverImage = album.cover

        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()

        val requestAPI: RequestAPI = Retrofit.Builder()
            .baseUrl("https://ngocduc2979.000webhostapp.com")
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(RequestAPI::class.java)

        val disposable = requestAPI.register_spring_list("/server/SongJson/" + id + ".json.txt")
            .observeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread())
            .subscribeOn(io.reactivex.schedulers.Schedulers.io())
            .subscribe({

//                listAlbumSong.addAll(it)
//                if (it.size != 0) {
//                    tvZeroSong.visibility = View.GONE
//
//                } else {
//                    tvZeroSong.visibility = View.VISIBLE
//                }

                tracks.text = it.size.toString() + " " + resources.getString(R.string.tracks)
                setAdapter(it as ArrayList<Song>)

                loading.visibility = View.GONE
            }, {
                it.printStackTrace()
            })
        disposables.add(disposable)
    }

    fun setAdapter(listSong: ArrayList<Song>) {
        listView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        albumSongAdapter = AlbumSongAdapter(listSong, this)
        listView.adapter = albumSongAdapter
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
}