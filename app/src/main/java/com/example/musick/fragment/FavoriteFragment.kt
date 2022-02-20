package com.example.musick.fragment

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musick.OnClickListerner
import com.example.musick.PlayerService
import com.example.musick.R
import com.example.musick.Song
import com.example.musick.adapter.FavoriteAdapter
import database.MusicDatabase
import kotlinx.android.synthetic.main.fragment_favorite.*
import saveData.AppConfig
import java.util.*
import kotlin.collections.ArrayList

class FavoriteFragment : Fragment(), OnClickListerner {

    var listFavorite = ArrayList<Song>()
    lateinit var favoriteAdapter: FavoriteAdapter

    companion object {
        const val ACTION_CLICK_FAVORITE = "click_favorite"

        @JvmStatic
        fun newInstance() =
            FavoriteFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }


    private val favoriteBroadCast: BroadcastReceiver = object :BroadcastReceiver(){
        @SuppressLint("NotifyDataSetChanged")
        override fun onReceive(p0: Context, intent: Intent) {

            val action = intent.action

            if (action.equals(ACTION_CLICK_FAVORITE)){
                val playlistDatabase = MusicDatabase(requireContext(), "playlist.db", null, 1)
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
                favoriteAdapter.notifyDataSetChanged()
            } else if (action.equals(PlayerService.ACTION_UPDATE_STATE_PLAY)) {
                favoriteAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onStart() {
        super.onStart()
        val intentFilter = IntentFilter()
        intentFilter.addAction(ACTION_CLICK_FAVORITE)
//        intentFilter.addAction(ACTION_CLICK_PAUSE_PLAY)
        intentFilter.addAction(PlayerService.ACTION_UPDATE_STATE_PLAY)
        activity?.registerReceiver(favoriteBroadCast, intentFilter)
    }

    override fun onStop() {
        super.onStop()
        activity?.unregisterReceiver(favoriteBroadCast)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this com.example.musick.fragment
        return inflater.inflate(R.layout.fragment_favorite, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onResume() {
        super.onResume()

        sort.setOnClickListener {
            showPopupMenu(it)
        }

        setAdapter()
    }

    @SuppressLint("RestrictedApi")
    fun showPopupMenu(view: View) {
        val menuBuilder = MenuBuilder(requireContext())
        val inflater = MenuInflater(requireContext())
        inflater.inflate(R.menu.sort_menu, menuBuilder)
        val optionsMenu = MenuPopupHelper(requireContext(), menuBuilder, view)
        optionsMenu.setForceShowIcon(true)

        menuBuilder.setCallback(object : MenuBuilder.Callback{
            override fun onMenuItemSelected(menu: MenuBuilder, item: MenuItem): Boolean {
                return when (item.itemId) {
                    R.id.sort_by_a_z -> {
                        AppConfig.getInstance(requireContext()).setSort("a-z")
                        setAdapter()
                        true
                    }
                    R.id.sort_by_z_a -> {
                        AppConfig.getInstance(requireContext()).setSort("z-a")
                        setAdapter()
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
    private fun setAdapter() {

//        val delete = "DELETE FROM table_list WHERE name = '1'"
//        playlistDatabase.querryData(delete)
        val playlistDatabase = MusicDatabase(requireContext(), "playlist.db", null, 1)

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

//        checkDevice(playlistDatabase)
        sortList()

        if (listFavorite.size > 0) {
            tvFavoriteZero.visibility = View.GONE
        }

        favoritelistView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        favoriteAdapter = FavoriteAdapter(requireContext(), listFavorite, this)
        favoritelistView.adapter = favoriteAdapter
        favoriteAdapter.notifyDataSetChanged()
    }

    private fun checkDevice(playlistDatabase: MusicDatabase) {

        val listAllSong = ArrayList<Song>()
        listAllSong.clear()
        listAllSong.addAll(AppConfig.getInstance(requireContext()).getCacheSong())
        listAllSong.addAll(AppConfig.getInstance(requireContext()).getCacheListSongAPI())

        listFavorite.forEach {
            var checkExist = false
            for (i in listAllSong.indices){
                if (it.path == listAllSong[i].path) {
                    checkExist = true
                }
            }
            if (!checkExist) {
                val deleteSong = "DELETE FROM favorite WHERE path = '" +  it.path + "'"
                playlistDatabase.querryData(deleteSong)
                listFavorite.remove(it)
            }
        }
    }

    private fun sortList() {
        if (AppConfig.getInstance(requireContext()).getSort().equals("a-z")) {
            Collections.sort(listFavorite, object : Comparator<Song> {
                override fun compare(p0: Song, p1: Song): Int {
                    return p0.songName.compareTo(p1.songName)
                }
            })
        } else if (AppConfig.getInstance(requireContext()).getSort().equals("z-a")) {
            Collections.sort(listFavorite, object : Comparator<Song> {
                override fun compare(p0: Song, p1: Song): Int {
                    return p1.songName.compareTo(p0.songName)
                }
            })
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun setOnclick() {
        val playlistDatabase = MusicDatabase(requireContext(), "playlist.db", null, 1)
        val cursor = playlistDatabase.getData("SELECT * FROM favorite")
        listFavorite.clear()
        while (cursor.moveToNext()) {
            val name = cursor.getString(0)
            val artist = cursor.getString(1)
            val path = cursor.getString(2)
            val album = cursor.getString(3)
            val duration = cursor.getString(4)
            val thumbnail = cursor.getString(5)
            listFavorite.add(Song(name, artist, path, album, duration, thumbnail))
        }
        favoriteAdapter.notifyDataSetChanged()
    }
}