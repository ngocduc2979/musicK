package saveData

import android.content.Context
import android.content.SharedPreferences
import com.example.musick.Album
import com.example.musick.Song
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.util.ArrayList

class AppConfig(context: Context) {

    private val KEY_LIST_SONG = "list song default"
    private val KEY_IS_PLAYING = "is playing"
    private val KEY_NO_REPEAT = "no repeat"
    private val KEY_SHUFFLE = "shuffle"
    private val KEY_REPEAT = "repeat"
    private val KEY_STATE_PLAY = "state_play"
    private val KEY_SONG_NAME = "song_name"
    private val KEY_ARTIST_NAME = "song_artist"
    private val KEY_SONG_PATH = "song_path"
    private val KEY_CURPOSITION = "curposition"
    private val KEY_IS_NEWPLAY = "isNewplay"
    private val KEY_CACHE_SONG = "cache_song"
    private val KEY_SORT = "sort"
    private val KEY_CACHE_SONG_API = "cache_song_api"
    private val KEY_CACHE_ALBUM = "cache_album"
    private val KEY_CACHE_CHARTS = "cache_charts"


    private var sharedPreferences: SharedPreferences? = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private var instance: AppConfig? = null
        fun getInstance(context: Context): AppConfig {
            if (instance == null) {
                instance = AppConfig(context)
            }

            return instance!!
        }
    }

    fun setShuffle(isShuffle: Boolean) {
        sharedPreferences!!.edit().putBoolean(KEY_SHUFFLE, isShuffle).apply()
    }

    fun isShuffle(): Boolean {
        return sharedPreferences!!.getBoolean(KEY_SHUFFLE, false)
    }

    fun setRepeat(repeat: String?) {
        sharedPreferences!!.edit().putString(KEY_REPEAT, repeat).apply()
    }

    fun getRepeat(): String? {
        return sharedPreferences!!.getString(KEY_REPEAT, KEY_NO_REPEAT)
    }

    fun setStatePlay(isPlaying: Boolean) {
        sharedPreferences!!.edit().putBoolean(KEY_STATE_PLAY, isPlaying).apply()
    }

    fun getStatePlay(): Boolean {
        return sharedPreferences!!.getBoolean(KEY_STATE_PLAY, false)
    }

    fun setCurrentSongName(song: Song) {
        sharedPreferences!!.edit().putString(KEY_SONG_NAME, song.songName).apply()
    }

    fun getSongName(): String? {
        return sharedPreferences!!.getString(KEY_SONG_NAME, null)
    }

    fun setCurrentSongArtist(song: Song) {
        sharedPreferences!!.edit().putString(KEY_ARTIST_NAME, song.artist).apply()
    }

    fun setCurrentSongPath(song: Song) {
        sharedPreferences!!.edit().putString(KEY_SONG_PATH, song.path).apply()
    }

    fun setCurPosition(position: Int) {
        sharedPreferences!!.edit().putInt(KEY_CURPOSITION, position).apply()
    }

    fun getCurposition(): Int {
        return sharedPreferences!!.getInt(KEY_CURPOSITION, 0)
    }

    fun setIsNewPlay(isIsNewPlay: Boolean) {
        sharedPreferences!!.edit().putBoolean(KEY_IS_NEWPLAY, isIsNewPlay).apply()
    }

    fun getIsNewPlay(): Boolean {
        return sharedPreferences!!.getBoolean(KEY_IS_NEWPLAY, false)
    }

    fun setIsPlaying(isIsPlaying: Boolean) {
        sharedPreferences!!.edit().putBoolean(KEY_IS_PLAYING, isIsPlaying).apply()
    }


    fun getCurrentSong(): Song {
        return getPlayList()!!.get(getCurposition())!!
    }

    fun getIsPlaying(): Boolean {
        return sharedPreferences!!.getBoolean(KEY_IS_PLAYING, false)
    }

    fun setSort(sortBy: String) {
        sharedPreferences!!.edit().putString(KEY_SORT, sortBy).apply()
    }

    fun getSort(): String? {
        return sharedPreferences!!.getString(KEY_SORT, "a-z")
    }

    fun setPlaylist(list: List<Song?>?) {
        val json: String = gson.toJson(list)
        sharedPreferences!!.edit().putString(KEY_LIST_SONG, json).apply()
    }

    fun getPlayList(): List<Song?>? {
        val json = sharedPreferences!!.getString(KEY_LIST_SONG, null)
        val type: Type = object : TypeToken<List<Song?>?>() {}.getType()
        return gson.fromJson(json, type)
    }

    fun setCacheSong(list: ArrayList<Song>) {
        val json: String = gson.toJson(list)
        sharedPreferences!!.edit().putString(KEY_CACHE_SONG, json).apply()
    }

    fun getCacheSong(): List<Song> {
        val json = sharedPreferences!!.getString(KEY_CACHE_SONG, null)
        val type: Type = object : TypeToken<List<Song?>?>() {}.type
        return gson.fromJson(json, type)
    }

    fun setCacheListSongAPI(list: ArrayList<Song>) {
        val json: String = gson.toJson(list)
        sharedPreferences!!.edit().putString(KEY_CACHE_SONG_API, json).apply()
    }

    fun getCacheListSongAPI(): List<Song> {
        val emptyList = Gson().toJson(ArrayList<Song>())
        return Gson().fromJson(
            sharedPreferences!!.getString(KEY_CACHE_SONG_API, emptyList),
            object : TypeToken<ArrayList<Song>>() {
            }.type
        )
    }

    fun setCacheListAlbumSpring(list: ArrayList<Album>) {
        val json: String = gson.toJson(list)
        sharedPreferences!!.edit().putString(KEY_CACHE_ALBUM, json).apply()
    }

    fun getCacheListAlbumSpring(): List<Album> {
        val emptyList = Gson().toJson(ArrayList<Album>())
        return Gson().fromJson(
            sharedPreferences!!.getString(KEY_CACHE_ALBUM, emptyList),
            object : TypeToken<ArrayList<Album>>() {
            }.type
        )
    }

    fun setCacheListCharts(list: ArrayList<Album>) {
        val json: String = gson.toJson(list)
        sharedPreferences!!.edit().putString(KEY_CACHE_CHARTS, json).apply()
    }

    fun getCacheListCharts(): List<Album> {
        val emptyList = Gson().toJson(ArrayList<Album>())
        return Gson().fromJson(
            sharedPreferences!!.getString(KEY_CACHE_CHARTS, emptyList),
            object : TypeToken<ArrayList<Album>>() {
            }.type
        )
    }


}