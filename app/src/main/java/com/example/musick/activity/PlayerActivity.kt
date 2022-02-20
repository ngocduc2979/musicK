package com.example.musick.activity

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.commit451.nativestackblur.NativeStackBlur
import com.example.musick.*
import database.MusicDatabase
import kotlinx.android.synthetic.main.player_activity.*
import saveData.AppConfig
import saveData.DataPlayer
import java.lang.StringBuilder

class PlayerActivity: AppCompatActivity() {

    companion object {
        private val KEY_NO_REPEAT = "no repeat"
        private const val KEY_REPEAT_ONE = "repeat one"

        fun launch(context: Context) {
            val intent = Intent(context, PlayerActivity::class.java)
            context.startActivity(intent)
        }
    }

    private var checkShuffle = false
    private var checkRepeat = "no repeat"
    private var isPlaying = false

    private val playerBroadcast: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == PlayerService.ACTION_UPDATE_PROGRESS_SONG) {
                val cur = intent.getIntExtra(PlayerService.EXTRA_CURRENT_PROGRESS, 0)
                val duration = intent.getIntExtra(PlayerService.EXTRA_DURATION, 0)
                if (duration > 0) {
                    seekbar.progress = cur * 100 / duration
                    currentTime.text = getTimeLabel(cur)
                    durationTime.text = getTimeLabel(duration)
                }
            } else if (action == PlayerService.ACTION_UPDATE_STATE_PLAY) {
                isPlaying = intent.getBooleanExtra(PlayerService.EXTRA_STATE_PLAY, false)

                if (isPlaying) {
                    play_pause.setBackgroundResource(R.drawable.ic_baseline_pause_circle_filled_24_white)
                } else {
                    play_pause.setBackgroundResource(R.drawable.ic_baseline_play_circle_filled_24_white)
                }
                AppConfig.getInstance(context).setStatePlay(isPlaying)
            } else if (action == PlayerService.ACTION_UPDATE_SONG_INFO) {
                showSongInfo(DataPlayer.getInstance()!!.getCurrentSong())
                setFavoriteColor()
            }

            /*val characterMapVn = listOf<String>("Đ", "Â", "Ă")
            val characterMapEn = listOf<String>("D", "A", "A")

            val songName = "DdsjfksjdfkÂ"
            val stringBuilder = StringBuilder()

            songName.toCharArray().forEach {
                val indexVn = characterMapVn.indexOf(it.toString())

                if (indexVn >= 0) {
                    stringBuilder.append(characterMapEn[indexVn])
                } else {
                    stringBuilder.append(it.toString())
                }
            }*/
        }
    }

    private fun getTimeLabel(milliseconds: Int): String {
        val minute = milliseconds / 1000 / 60
        val second = milliseconds / 1000 % 60
        return if (minute < 10) {
            if (second < 10) {
                "0$minute:0$second"
            } else {
                "0$minute:$second"
            }
        } else {
            if (second >= 10) {
                "$minute:$second"
            } else {
                "$minute:0$second"
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.player_activity)

//        isPlaying = intent.getBooleanExtra(PlayerService.EXTRA_STATE_PLAY, false)
        setClick()
        setStatePlay()
        play()
    }

    override fun onResume() {
        super.onResume()

        updateStatePlay()
        updateCurrentSong()
    }

    override fun onStart() {
        super.onStart()
        val intentFilter = IntentFilter()
        intentFilter.addAction(PlayerService.ACTION_UPDATE_PROGRESS_SONG)
        intentFilter.addAction(PlayerService.ACTION_UPDATE_STATE_PLAY)
        intentFilter.addAction(PlayerService.ACTION_UPDATE_SONG_INFO)

        registerReceiver(playerBroadcast, intentFilter)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(playerBroadcast)
    }

    private fun setClick() {
        play_pause.setOnClickListener(View.OnClickListener {
            val intent = Intent(this@PlayerActivity, PlayerService::class.java)
            intent.action = PlayerService.ACTION_PLAY_PAUSE_MUSIC
            startService(intent)
        })

        toolbar_player.setOnClickListener(View.OnClickListener { onBackPressed() })

        previous_player.setOnClickListener(View.OnClickListener { previous() })

        forward_player.setOnClickListener(View.OnClickListener { next() })

        shuffle.setOnClickListener(View.OnClickListener { shuffle() })

        repeat.setOnClickListener(View.OnClickListener { repeat() })

        seekbar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    private fun updateStatePlay() {
        isPlaying = AppConfig.getInstance(this).getStatePlay()
        if (isPlaying) {
            play_pause.setBackgroundResource(R.drawable.ic_baseline_pause_circle_filled_24_white)
        } else {
            play_pause.setBackgroundResource(R.drawable.ic_baseline_play_circle_filled_24_white)
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun updateCurrentSong() {
        val song = AppConfig.getInstance(this).getCurrentSong()

        val drawable = this.resources.getDrawable(R.drawable.dark_background_default)
        val bitmap = (drawable as BitmapDrawable).bitmap
        val blurBitmap: Bitmap = NativeStackBlur.process(bitmap, 50);

        tvSongName_player.text = song.songName
        tvArtist_player.text = song.artist
//        durationTime.text = getTimeLabel(Integer.parseInt(song.duration))

        Glide.with(this)
            .load(song.thumbnail)
            .placeholder(R.drawable.music_default)
            .error("image error")
            .centerCrop()
            .into(image_cover)

        Glide.with(this)
            .load(blurBitmap)
            .error("image error")
            .centerCrop()
            .into(iamge_blur)
    }

    private fun setStatePlay() {
        setRepeatIcon()
        setShuffleIcon()
        setFavoriteColor()
    }

    fun setFavoriteColor() {
        val playlistDatabase = MusicDatabase(this, "playlist.db", null, 1)
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
            favorite_player.setBackgroundResource(R.drawable.ic_baseline_favorite_24_orange)
        } else {
            favorite_player.setBackgroundResource(R.drawable.ic_baseline_favorite_24_white)
        }

        favorite_player.setOnClickListener {
            val song = DataPlayer.getInstance()!!.getCurrentSong()
            if (checkExist) {
                val removeSong = "DELETE FROM favorite WHERE path = " +  "'" +  song.path + "'"
                playlistDatabase.querryData(removeSong)
                checkExist = false
                favorite_player.setBackgroundResource(R.drawable.ic_baseline_favorite_24_white)
            } else {
                val insertSong = "INSERT INTO favorite VALUES (" + "'" + song.songName + "', " +
                        "'" + song.artist + "', " +
                        "'" + song.path + "', " +
                        "'" + song.album + "', " +
                        "'" + song.duration + "', " +
                        "'" + song.thumbnail + "')"
                playlistDatabase.querryData(insertSong)
                checkExist = true
                favorite_player.setBackgroundResource(R.drawable.ic_baseline_favorite_24_orange)
            }
        }
    }

    private fun setShuffleIcon() {
        checkShuffle = if (AppConfig.getInstance(this).isShuffle()) {
            shuffle.setBackgroundResource(R.drawable.ic_baseline_shuffle_24_purple)
            true
        } else {
            shuffle.setBackgroundResource(R.drawable.ic_baseline_shuffle_24_white)
            false
        }
    }

    private fun setRepeatIcon() {
        if (AppConfig.getInstance(this).getRepeat().equals(KEY_NO_REPEAT)) {
            repeat.setBackgroundResource(R.drawable.ic_baseline_repeat_24_white)
            checkRepeat = KEY_NO_REPEAT
        } else if (AppConfig.getInstance(this).getRepeat().equals(KEY_REPEAT_ONE)) {
            checkRepeat = KEY_REPEAT_ONE
            repeat.setBackgroundResource(R.drawable.ic_baseline_repeat_one_24_purple)
        }
    }

    private fun repeat() {
        if (checkRepeat == KEY_NO_REPEAT) {
            checkRepeat = KEY_REPEAT_ONE
            repeat.setBackgroundResource(R.drawable.ic_baseline_repeat_one_24_purple)
        } else if (checkRepeat == KEY_REPEAT_ONE) {
            checkRepeat = KEY_NO_REPEAT
            repeat.setBackgroundResource(R.drawable.ic_baseline_repeat_24_white)
        }
        AppConfig.getInstance(this).setRepeat(checkRepeat)
    }

    private fun shuffle() {
        if (!checkShuffle) {
            checkShuffle = true
            shuffle.setBackgroundResource(R.drawable.ic_baseline_shuffle_24_purple)
        } else {
            checkShuffle = false
            shuffle.setBackgroundResource(R.drawable.ic_baseline_shuffle_24_white)
        }
        AppConfig.getInstance(this).setShuffle(checkShuffle)
    }

    private fun showSongInfo(song: Song) {

        tvSongName_player.text = song.songName
        tvArtist_player.text = song.artist

        Glide.with(this)
            .load(song.thumbnail)
            .placeholder(R.drawable.music_default)
            .error("image error")
            .centerCrop()
            .into(image_cover)
    }

    private fun seekTo(progress: Int) {
        val intent = Intent(this, PlayerService::class.java)
        intent.action = PlayerService.ACTION_SEEK
        intent.putExtra(PlayerService.EXTRA_PROGRESS, progress)
        startService(intent)
    }

    private fun play() {
        val checkNewPlay = AppConfig.getInstance(this@PlayerActivity).getIsNewPlay()
        if (checkNewPlay) {
            val intent = Intent(this@PlayerActivity, PlayerService::class.java)
            intent.action = PlayerService.ACTION_NEW_PLAY
            startService(intent)
        }
    }

    private operator fun next() {
        val intent = Intent(this, PlayerService::class.java)
        intent.action = PlayerService.ACTION_NEXT_SONG
        startService(intent)
    }

    private fun previous() {
        val intent = Intent(this, PlayerService::class.java)
        intent.action = PlayerService.ACTION_PREVIOUS_SONG
        startService(intent)
    }
}