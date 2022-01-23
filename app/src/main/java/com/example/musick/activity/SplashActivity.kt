package com.example.musick.activity

import android.Manifest
import android.annotation.SuppressLint
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.commit451.nativestackblur.NativeStackBlur
import com.example.musick.Album
import com.example.musick.R
import com.example.musick.RequestAPI
import com.example.musick.Song
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.splash_activity.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import saveData.AppConfig
import wseemann.media.FFmpegMediaMetadataRetriever
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor


class SplashActivity: AppCompatActivity() {

    private var disposables: CompositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_activity)

        Glide.with(this)
            .load(R.drawable.music_background_resize)
            .centerCrop()
            .into(splash_cover)

        text.text = resources.getString(R.string.nnd)

        requestPermission()
        cacheListSongAPI()
        cacheListAlbum()
    }

    private fun requestPermission() {
        val permissionlistener: PermissionListener = object : PermissionListener {
            override fun onPermissionGranted() {
                loadSong()
            }

            override fun onPermissionDenied(deniedPermissions: List<String?>) {
                Toast.makeText(this@SplashActivity,
                    "Permission Denied\n$deniedPermissions",
                    Toast.LENGTH_SHORT).show()
            }
        }
        TedPermission.with(this)
            .setPermissionListener(permissionlistener)
            .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
            .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .check()
    }

    fun cacheListAlbum() {

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

        val disposableAlbum = requestAPI.register_album_list("/server/SongJson/albums.json.txt")
            .observeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread())
            .subscribeOn(io.reactivex.schedulers.Schedulers.io())
            .subscribe({
                AppConfig.getInstance(this).setCacheListAlbumSpring(it as ArrayList<Album>)
            }, {
                it.printStackTrace()
            })

        disposables.add(disposableAlbum)

        val disposableCharts = requestAPI.register_album_list("/server/SongJson/top100.json.txt")
            .observeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread())
            .subscribeOn(io.reactivex.schedulers.Schedulers.io())
            .subscribe({
                AppConfig.getInstance(this).setCacheListCharts(it as ArrayList<Album>)
            }, {
                it.printStackTrace()
            })

        disposables.add(disposableCharts)
    }

    fun cacheListSongAPI() {
//        val interceptor = HttpLoggingInterceptor()
//        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
//        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()

        val requestAPI: RequestAPI = Retrofit.Builder()
            .baseUrl("https://ngocduc2979.000webhostapp.com")
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
//            .client(client)
            .build()
            .create(RequestAPI::class.java)

        val disposable = requestAPI.register_spring_list("/server/allsong.json.txt")
            .observeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread())
            .subscribeOn(io.reactivex.schedulers.Schedulers.io())
            .subscribe({
                AppConfig.getInstance(this).setCacheListSongAPI(it as ArrayList<Song>)
            }, {
                it.printStackTrace()
            })
        disposables.add(disposable)
    }

    @SuppressLint("Range", "Recycle")
    fun loadSong() {
        Observable.create<List<Song>> {
                val listAllTracks = ArrayList<Song>()
                val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                val selection = MediaStore.Audio.Media.IS_MUSIC
                val cursor: Cursor? = contentResolver.query(uri, null, selection, null, null)

                if (cursor != null) {

                    while (cursor.moveToNext()) {
                        try {
                            val path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                            val artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                            val song = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                            val album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))
                            val duration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))

                            if (!path.endsWith(".amr")) {
                                listAllTracks.add(Song(song, artist, path, album, duration, cacheThumbnailPath(path)))
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    it.onNext(listAllTracks)
                    it.onComplete()
                }
        }.subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                AppConfig.getInstance(this).setCacheSong(it as java.util.ArrayList<Song>)
//                AppConfig.getInstance(this).setPlaylist(it)
                MainActivity.launch(this)
                finish()
            }, {
                it.printStackTrace()
            })
    }

    private fun cacheThumbnailPath(pathAudio: String): String {
        try {
            val thumbnailData = FFmpegMediaMetadataRetriever().apply {
                setDataSource(pathAudio)
            }.embeddedPicture

            val file = File(cacheDir, pathAudio.hashCode().toString())
            file.createNewFile()
            val fileOutputStream = FileOutputStream(file)
            val dataOutputStream = DataOutputStream(fileOutputStream)
            dataOutputStream.write(thumbnailData)
            dataOutputStream.flush()
            fileOutputStream.close()
            dataOutputStream.close()

            return file.path
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ""
    }

}