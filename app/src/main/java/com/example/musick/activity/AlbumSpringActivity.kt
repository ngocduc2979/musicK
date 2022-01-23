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
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.album_spring_activity.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import kotlin.collections.ArrayList

class AlbumSpringActivity: AppCompatActivity() {

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
        override fun onReceive(p0: Context, intent: Intent) {

            val action = intent.action

            if (action.equals(PlayerService.ACTION_UPDATE_STATE_PLAY)){
                albumSongAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.album_spring_activity)

        getListAlbum()

    }

    override fun onStart() {
        super.onStart()
        val intentFilter = IntentFilter()
        intentFilter.addAction(PlayerService.ACTION_UPDATE_STATE_PLAY)
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
                listAlbumSong.addAll(it)
                if (it.size != 0) {
                    tvZeroSong.visibility = View.GONE
                    Glide.with(this)
                        .load(coverImage)
                        .placeholder(R.drawable.music_default)
                        .centerCrop()
                        .into(cover)
                } else {
                    tvZeroSong.visibility = View.VISIBLE
                }

                playlist_name.text = playlistName
                tracks.text = it.size.toString() + " " + resources.getString(R.string.tracks)
                toolbar.setOnClickListener {
                    onBackPressed()
                }

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

}