package com.example.musick.fragment

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musick.PlayerService
import com.example.musick.activity.AllTrackActivity
import com.example.musick.R
import com.example.musick.Song
import com.example.musick.activity.ArtistActivity
import com.example.musick.activity.PlaylistActivity
import com.example.musick.adapter.RecentlyPlayAdapter
import database.MusicDatabase
import kotlinx.android.synthetic.main.fragment_library.*
import saveData.AppConfig
import java.util.*
import kotlin.collections.ArrayList

class LibraryFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance() = LibraryFragment().apply {
            arguments = Bundle().apply {

            }
        }
    }

    private val libraryBroadCast: BroadcastReceiver = object : BroadcastReceiver(){
        @SuppressLint("NotifyDataSetChanged")
        override fun onReceive(p0: Context, intent: Intent) {

            val action = intent.action

            if (action.equals(PlayerService.ACTION_UPDATE_STATE_PLAY)){
                Log.wtf("HomeFragment", "ACTION_UPDATE_STATE_PLAY libraryBroadCast")
                setAdapterRecently()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this com.example.musick.fragment
        return inflater.inflate(R.layout.fragment_library, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        layout_alltrack.setOnClickListener {
           requireContext().startActivity(Intent(requireContext(), AllTrackActivity::class.java))
        }

        layout_laylist.setOnClickListener {
            requireContext().startActivity(Intent(requireContext(), PlaylistActivity::class.java))
        }

        layout_artist.setOnClickListener {
            ArtistActivity.launch(requireContext())
        }
    }

    override fun onResume() {
        super.onResume()
        setAdapterRecently()
    }

    override fun onStart() {
        super.onStart()
        val intentFilter = IntentFilter()
        intentFilter.addAction(PlayerService.ACTION_UPDATE_STATE_PLAY)
        activity?.registerReceiver(libraryBroadCast, intentFilter)
    }

    override fun onStop() {
        super.onStop()
        activity?.unregisterReceiver(libraryBroadCast)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setAdapterRecently() {
        val playlistDatabase = MusicDatabase(requireContext(), "playlist.db", null, 1)
        val cursor = playlistDatabase.getData("SELECT * FROM history")

        val listHistory = ArrayList<Song>()
        listHistory.clear()

        val listAllSong = ArrayList<Song>()
        listAllSong.clear()
        listAllSong.addAll(AppConfig.getInstance(requireContext()).getCacheSong())
        listAllSong.addAll(AppConfig.getInstance(requireContext()).getCacheListSongAPI())

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
            var checkExist = false
            for (i in listAllSong.indices){
                if (it.path == listAllSong[i].path) {
                    checkExist = true
                }
            }

            if (!checkExist) {
                val deleteSong = "DELETE FROM history WHERE path = '" +  it.path + "'"
                playlistDatabase.querryData(deleteSong)
                listHistory.remove(it)
            }
        }

        listHistory.reverse()

        recentlyView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        val recentlyPlayAdapter = RecentlyPlayAdapter(requireContext(), listHistory)
        recentlyView.adapter = recentlyPlayAdapter
        recentlyPlayAdapter.notifyDataSetChanged()
    }


}