package saveData

import com.example.musick.Song
import java.util.*
import kotlin.collections.ArrayList

class DataPlayer {

    private var playlist: List<Song> = ArrayList()
    private var playPosition = 0

    companion object {
        private var instance: DataPlayer? = null
        fun getInstance(): DataPlayer? {
            if (instance == null) {
                instance = DataPlayer()
            }
            return instance
        }
    }

    fun setPlaylist(playlist: List<Song>) {
        this.playlist = playlist
    }

    fun getPlaylist(): List<Song> {
        return DataPlayer.getInstance()!!.playlist
    }

    fun setPlayPosition(position: Int) {
        playPosition = position
    }

    fun getPlayPosition(): Int {
        return playPosition
    }

    fun getCurrentSong(): Song {
        return playlist[playPosition]
    }

    fun getNextPosition(): Int {
        return ((playPosition + 1) % playlist.size).also { playPosition = it }
    }

    fun getShuffeNextPosition(): Int {
        val random = Random()
        return random.nextInt(playlist.size).also {
            playPosition = it }
    }

    fun getPreviewPosition(): Int {
        return if (playPosition == 0) {
            playlist.size - 1.also { playPosition = it }
        } else {
            playPosition - 1.also { playPosition = it }
        }
    }

}