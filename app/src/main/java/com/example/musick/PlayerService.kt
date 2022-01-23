package com.example.musick

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.util.Base64
import android.util.Log
import androidx.core.app.NotificationCompat
import com.commit451.nativestackblur.NativeStackBlur
import com.example.musick.NotificationChannelClass.Companion.CHANNEL_ID
import com.example.musick.activity.PlayerActivity
import database.MusicDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import saveData.AppConfig
import saveData.DataPlayer
import saveData.DataPlayer.Companion.getInstance
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import androidx.media.app.NotificationCompat as NotificationCompat1

class PlayerService: Service(), MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnBufferingUpdateListener{

    companion object {
        const val EXTRA_CURRENT_PROGRESS = "com.example.musick.current_progress"
        const val EXTRA_DURATION = "com.example.musick.duration"
        const val EXTRA_STATE_PLAY = "com.example.musick.state_play"
        const val EXTRA_PROGRESS = "com.example.musick.progress"
        const val ACTION_NEW_PLAY = "com.example.musick.NEW_PLAY"
        const val ACTION_PLAY_PAUSE_MUSIC = "com.example.musick.PLAY, Pause"
        const val ACTION_SEEK = "com.example.musick.SEEK"
        const val ACTION_NEXT_SONG = "com.example.musick.NEXT"
        const val ACTION_PREVIOUS_SONG = "com.example.musick.PREVIOUS"
        const val ACTION_CLOSE_PLAYER = "com.example.musick.CLOSE"
        const val ACTION_UPDATE_SONG_INFO = "com.example.musick.UPDATE_SONG_INFO"
        const val ACTION_UPDATE_STATE_PLAY = "com.example.musick.UPDATE_STATE_PLAY"
        const val ACTION_UPDATE_PROGRESS_SONG = "com.example.musick.UPDATE_PROGRESS"
        const val ACTION_START_SERVICE = "com.example.musick.ACTION_START_SERVICE"
    }

    private var isPlaying = false

    private var durationCurSong = 0
    private var mediaPlayer: MediaPlayer? = null
    private var pendingIntent: PendingIntent? = null
    private val playIcon = R.drawable.ic_baseline_play_arrow_24_black;
    private val pauseIcon = R.drawable.ic_baseline_pause_24_black;

    private val updateProgressHandler = Handler()

    private val updateProgress: Runnable = object : Runnable {
        override fun run() {
//            Log.wtf("Service", "updateProgress")
            val cur = mediaPlayer!!.currentPosition
            sendBroadcastUpdateProgress(cur, durationCurSong)
            updateProgressHandler.postDelayed(this, 1000L)
        }
    }


    override fun onCreate() {
        super.onCreate()
        initPlayer()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        val action = intent.action

        if (action == ACTION_NEW_PLAY) {
            newPlay()
        } else if (action == ACTION_PLAY_PAUSE_MUSIC) {
            play()
        } else if (action == ACTION_SEEK) {
            val progress = intent.getIntExtra(EXTRA_PROGRESS, 0)
            seekTo(progress)
        } else if (action == ACTION_NEXT_SONG) {
            next()
        } else if (action == ACTION_PREVIOUS_SONG) {
            previous()
        } else if (action == ACTION_CLOSE_PLAYER) {
            sendBroadcastClosePlayer()
            stopSelf()
        } else if (action == ACTION_START_SERVICE) {
            startServiceNow()
        }
        return START_STICKY
    }

    private fun sendBroadcastNewPlay() {
        val intent = Intent(ACTION_NEW_PLAY)
        sendBroadcast(intent)
    }

    private fun sendBroadcastUpdateProgress(cur: Int, duration: Int) {
        val intent = Intent(PlayerService.ACTION_UPDATE_PROGRESS_SONG)
        intent.putExtra(PlayerService.EXTRA_DURATION, duration)
        intent.putExtra(PlayerService.EXTRA_CURRENT_PROGRESS, cur)
        sendBroadcast(intent)
    }

    private fun sendBroadcastUpdateStatePlay() {
        val intent = Intent(ACTION_UPDATE_STATE_PLAY)
        intent.putExtra(EXTRA_STATE_PLAY, mediaPlayer!!.isPlaying)
        sendBroadcast(intent)
    }

    private fun sendBroadcastClosePlayer() {
        val intent = Intent(ACTION_CLOSE_PLAYER)
        sendBroadcast(intent)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        mediaPlayer!!.release()
        updateProgressHandler.removeCallbacks(updateProgress)
        super.onDestroy()
    }

    private fun initPlayer() {
        mediaPlayer = MediaPlayer()
        mediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
        mediaPlayer!!.setOnPreparedListener(this)
        mediaPlayer!!.setOnBufferingUpdateListener(this)
        mediaPlayer!!.setOnCompletionListener(this)
    }

    override fun onPrepared(mp: MediaPlayer) {
        mediaPlayer!!.start()
        isPlaying = true
        durationCurSong = mp.duration
        updateProgressHandler.post(updateProgress)
        sendBroadcastUpdateStatePlay()
    }

    override fun onBufferingUpdate(mp: MediaPlayer?, percent: Int) {}

    override fun onCompletion(mp: MediaPlayer?) {
        if (AppConfig.getInstance(this).getRepeat().equals("no repeat")) {
            if (AppConfig.getInstance(this).isShuffle()) {
                shuffeNext()
            } else {
                next()
            }
        } else if (AppConfig.getInstance(this).getRepeat().equals("repeat one")) {
            repeatOne()
        }

        sendNotification()
    }

    private fun play() {
        if (isPlaying) {
            if (mediaPlayer!!.isPlaying) {
                pause()
            } else {
                mediaPlayer!!.start()
                AppConfig.getInstance(this).setIsPlaying(true)
            }
            sendNotification()
            sendBroadcastUpdateStatePlay()
        } else {
            newPlay()
        }
    }

    private fun startServiceNow() {
        try {
            if (AppConfig.getInstance(this).getPlayList() != null) {
                val song = AppConfig.getInstance(this).getCurrentSong()
                isPlaying = false
                mediaPlayer!!.reset()
                mediaPlayer!!.setDataSource(this, Uri.parse(song.path))
//            mediaPlayer!!.prepareAsync()
                sendNotification()
                requestUpdateUISongInfo()
                sendBroadcastUpdateStatePlay()
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun newPlay() {
        isPlaying = true
        val curSong = DataPlayer.getInstance()!!.getCurrentSong()
        AppConfig.getInstance(this).setCurrentSongName(curSong)
        AppConfig.getInstance(this).setCurrentSongArtist(curSong)
        AppConfig.getInstance(this).setCurrentSongPath(curSong)
        AppConfig.getInstance(this).setIsPlaying(true)
        sendBroadcastNewPlay()
        sendBroadcastUpdateStatePlay()

        sendNotification()
        startNewSong(curSong)
    }

    private fun requestUpdateUISongInfo() {
        val intent = Intent(ACTION_UPDATE_SONG_INFO)
        sendBroadcast(intent)
    }

    private fun startNewSong(song: Song) {
        try {
            isPlaying = false
            mediaPlayer!!.reset()
            mediaPlayer!!.setDataSource(this, Uri.parse(song.path))
            mediaPlayer!!.prepareAsync()
            requestUpdateUISongInfo()
            sendBroadcastUpdateStatePlay()
            addToHistory(song)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun repeatOne() {
        val curSong = getInstance()!!.getCurrentSong()
        startNewSong(curSong)
        AppConfig.getInstance(this).setCurrentSongName(curSong)
        AppConfig.getInstance(this).setCurrentSongArtist(curSong)
        AppConfig.getInstance(this).setCurrentSongPath(curSong)
    }

    private fun pause() {
        mediaPlayer!!.pause()
        AppConfig.getInstance(this).setIsPlaying(false)
    }

    private fun shuffeNext() {
        val shufflePosition = getInstance()!!.getShuffeNextPosition()
        AppConfig.getInstance(this).setCurPosition(shufflePosition)
        AppConfig.getInstance(this)
            .setCurrentSongName(getInstance()!!.getPlaylist()[shufflePosition])
        AppConfig.getInstance(this)
            .setCurrentSongArtist(getInstance()!!.getPlaylist()[shufflePosition])
        AppConfig.getInstance(this)
            .setCurrentSongPath(getInstance()!!.getPlaylist()[shufflePosition])

        sendNotification()

        requestUpdateUISongInfo()
        startNewSong(getInstance()!!.getPlaylist()[shufflePosition])
    }

    private operator fun next() {
        AppConfig.getInstance(this).setIsPlaying(true)

        if (AppConfig.getInstance(this).getRepeat().equals("no repeat")) {
            if (AppConfig.getInstance(this).isShuffle()) {
                shuffeNext()
            } else {
                val nextPosition = getInstance()!!.getNextPosition()
                AppConfig.getInstance(this).setCurPosition(nextPosition)
                AppConfig.getInstance(this).setPlaylist(getInstance()!!.getPlaylist())

                AppConfig.getInstance(this)
                    .setCurrentSongName(getInstance()!!.getPlaylist()[nextPosition])
                AppConfig.getInstance(this)
                    .setCurrentSongArtist(getInstance()!!.getPlaylist()[nextPosition])
                AppConfig.getInstance(this)
                    .setCurrentSongPath(getInstance()!!.getPlaylist()[nextPosition])

                sendNotification()
                requestUpdateUISongInfo()
                startNewSong(getInstance()!!.getPlaylist()[nextPosition])
            }
        } else if (AppConfig.getInstance(this).getRepeat().equals("repeat one")) {
            val curPosition = getInstance()!!.getPlayPosition()

            sendNotification()

            requestUpdateUISongInfo()
            startNewSong(getInstance()!!.getPlaylist()[curPosition])
        }
    }

    private fun previous() {
        AppConfig.getInstance(this).setIsPlaying(true)
        if (AppConfig.getInstance(this).getRepeat().equals("no repeat")) {
            if (AppConfig.getInstance(this).isShuffle()) {
                shuffeNext()
            } else {
                val previousPosition = getInstance()!!.getPreviewPosition()
                DataPlayer.getInstance()!!.setPlayPosition(previousPosition)

                AppConfig.getInstance(this).setCurPosition(previousPosition)
                AppConfig.getInstance(this).setPlaylist(DataPlayer.getInstance()!!.getPlaylist())
                AppConfig.getInstance(this).setCurrentSongName(getInstance()!!.getPlaylist()[previousPosition])
                AppConfig.getInstance(this).setCurrentSongArtist(getInstance()!!.getPlaylist()[previousPosition])
                AppConfig.getInstance(this).setCurrentSongPath(getInstance()!!.getPlaylist()[previousPosition])

                sendNotification()
                requestUpdateUISongInfo()
                startNewSong(DataPlayer.getInstance()!!.getPlaylist()[previousPosition])
            }
        } else if (AppConfig.getInstance(this).getRepeat().equals("repeat one")) {
            val curPosition = getInstance()!!.getPlayPosition()

            sendNotification()
            requestUpdateUISongInfo()
            startNewSong(DataPlayer.getInstance()!!.getPlaylist()[curPosition])
        }
    }

    private fun seekTo(progress: Int) {
        if (progress > 0) {
            val seekTo = progress * durationCurSong / 100
            mediaPlayer!!.seekTo(seekTo)
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun sendNotification() {
        val song = getInstance()!!.getCurrentSong()

        val albumart = getInstance()!!.getCurrentSong().thumbnail

        if (albumart != "") {
             if (albumart.contains("ngocduc2979")) {
                 Observable.create<Bitmap> {
                     val url = URL(albumart)
                     val bitmapApi = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                         it.onNext(bitmapApi)
                         it.onComplete()
                 }.subscribeOn(Schedulers.newThread())
                     .observeOn(AndroidSchedulers.mainThread())
                     .subscribe({
                         val resizeBitmap = Bitmap.createScaledBitmap(it, 250, 250, false)
                         setNotification(song, NativeStackBlur.process(resizeBitmap, 0))
                     }, {
                         it.printStackTrace()
                     })
             } else {
                 val bitmap = BitmapFactory.decodeFile(albumart)
                 val resizeBitmap = Bitmap.createScaledBitmap(bitmap, 250, 250, false)
                 setNotification(song, NativeStackBlur.process(resizeBitmap, 0))
             }
        } else {
            val drawable = this.resources.getDrawable(R.drawable.music_default)
            val bitmap = (drawable as BitmapDrawable).bitmap
            val resizeBitmap = Bitmap.createScaledBitmap(bitmap, 250, 250, false)
            setNotification(song, NativeStackBlur.process(resizeBitmap, 0))
        }

//        val mediaSessionCompat = MediaSessionCompat(this, "tag")
//        val notification: Notification =
//            NotificationCompat.Builder(this, CHANNEL_ID)
//                .setSmallIcon(R.drawable.music_icon)
//                .setContentTitle(song.songName)
//                .setContentText(song.artist)
//                .setLargeIcon(bm)
//                .setContentIntent(setClickNotifiMediaStye())
//                .addAction(R.drawable.ic_baseline_skip_previous_24_black, "privious",
//                    getPendingIntent(this, ACTION_PREVIOUS_SONG))
//                .addAction(if(AppConfig.getInstance(this).getIsPlaying()) pauseIcon else playIcon,
//                    "pause",getPendingIntent(this, ACTION_PLAY_PAUSE_MUSIC))
//                .addAction(R.drawable.ic_baseline_skip_next_24_black, "next",
//                    getPendingIntent(this, ACTION_NEXT_SONG))
//                .addAction(R.drawable.ic_baseline_close_24_black, "close",
//                    getPendingIntent(this, ACTION_CLOSE_PLAYER))
//                .setStyle(NotificationCompat1.MediaStyle()
//                    .setShowActionsInCompactView(0, 1, 2)
//                    .setMediaSession(if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) mediaSessionCompat.sessionToken else null))
//                .build()
//        startForeground(1, notification)
    }

    fun setNotification(song: Song, bm: Bitmap) {
        val mediaSessionCompat = MediaSessionCompat(this, "tag")
        val notification: Notification =
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.music_icon)
                .setContentTitle(song.songName)
                .setContentText(song.artist)
                .setLargeIcon(bm)
                .setContentIntent(setClickNotifiMediaStye())
                .addAction(R.drawable.ic_baseline_skip_previous_24_black, "privious",
                    getPendingIntent(this, ACTION_PREVIOUS_SONG))
                .addAction(if(AppConfig.getInstance(this).getIsPlaying()) pauseIcon else playIcon,
                    "pause",getPendingIntent(this, ACTION_PLAY_PAUSE_MUSIC))
                .addAction(R.drawable.ic_baseline_skip_next_24_black, "next",
                    getPendingIntent(this, ACTION_NEXT_SONG))
                .addAction(R.drawable.ic_baseline_close_24_black, "close",
                    getPendingIntent(this, ACTION_CLOSE_PLAYER))
                .setStyle(NotificationCompat1.MediaStyle()
                    .setShowActionsInCompactView(0, 1, 2)
                    .setMediaSession(if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) mediaSessionCompat.sessionToken else null))
                .build()
        startForeground(1, notification)
    }


    fun getBitmapFromURL(src: String?): Bitmap? {
        return try {
            val url = URL(src)
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.setDoInput(true)
            connection.connect()
            val input: InputStream = connection.getInputStream()
            BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun setClickNotifiMediaStye(): PendingIntent? {
        val intent = Intent(this, PlayerActivity::class.java)
        AppConfig.getInstance(this).setIsNewPlay(false)
        val taskStackBuilder = TaskStackBuilder.create(this)
        taskStackBuilder.addNextIntentWithParentStack(intent)
        return taskStackBuilder.getPendingIntent(1, PendingIntent.FLAG_UPDATE_CURRENT).also {
            pendingIntent = it
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun getPendingIntent(context: Context, action: String): PendingIntent {
        val intent = Intent(this, MyBroadCastReceiver::class.java)
//        val intent = Intent(action)
        intent.action = action
//        sendBroadcast(intent)
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    fun addToHistory(song: Song) {

        var playlistDatabase = MusicDatabase(this, "playlist.db", null, 1)
        val listHistory = ArrayList<Song>()
        var checkExist = false

        listHistory.clear()
        val cursor = playlistDatabase.getData("SELECT * FROM history")
        while (cursor.moveToNext()) {
            val name = cursor.getString(0)
            val artist = cursor.getString(1)
            val path = cursor.getString(2)
            val album = cursor.getString(3)
            val duration = cursor.getString(4)
            val thumbnail = cursor.getString(5)
            listHistory.add(Song(name, artist, path, album, duration, thumbnail))
        }

        listHistory.forEach {
            if (it.path == song.path) {
                checkExist = true
            }
        }

        if (!checkExist) {

            val add = "INSERT INTO history VALUES (" + "'" + song.songName + "', '" +
                    song.artist + "', '" +
                    song.path + "', '" +
                    song.album + "', '" +
                    song.duration + "', '" +
                    song.thumbnail + "', " +
                    1 + ")"
            playlistDatabase.querryData(add)
        } else {
            val selectPlays = "SELECT * from history WHERE path = '" + song.path + "'"
            val cursor = playlistDatabase.getData(selectPlays)
            var plays = 1
            while (cursor.moveToNext()) {
                plays = cursor.getInt(6)
            }

            val totalPlays = plays + 1

            val delete = "DELETE FROM history WHERE path = '" +  song.path + "'"
            playlistDatabase.querryData(delete)

            val add = "INSERT INTO history VALUES (" + "'" + song.songName + "', '" +
                    song.artist + "', '" +
                    song.path + "', '" +
                    song.album + "', '" +
                    song.duration + "', '" +
                    song.thumbnail + "', " +
                    totalPlays + ")"
            playlistDatabase.querryData(add)
        }
    }
}