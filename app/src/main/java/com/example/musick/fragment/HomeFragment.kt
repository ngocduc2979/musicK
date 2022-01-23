package com.example.musick.fragment

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musick.*
import com.example.musick.adapter.AlbumHomeAdapter
import com.example.musick.adapter.HomeAdapter
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_home.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import saveData.AppConfig

class HomeFragment : Fragment() {

    var listSong = ArrayList<Song>()
    var listAlbum = ArrayList<Album>()

    companion object {
        fun newInstance() =
            HomeFragment().apply {
            arguments = Bundle().apply {
            }
        }
    }

    private val homeBroadcastReceiver = object : BroadcastReceiver(){
        @SuppressLint("NotifyDataSetChanged")
        override fun onReceive(p0: Context, intent: Intent) {
            val action = intent.action
            if (action.equals(PlayerService.ACTION_UPDATE_STATE_PLAY)){
                setAdapter()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = Intent(requireContext(), PlayerService::class.java)
        intent.action = PlayerService.ACTION_START_SERVICE
        activity?.startService(intent)

    }

    override fun onStart() {
        super.onStart()
        val intentFilter = IntentFilter()
        intentFilter.addAction(PlayerService.ACTION_UPDATE_STATE_PLAY)
        activity?.registerReceiver(homeBroadcastReceiver, intentFilter)
    }

    override fun onStop() {
        super.onStop()
        activity?.unregisterReceiver(homeBroadcastReceiver)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAlbumsAdapter()
        setChartsAdapter()
        setAdapter()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        setAdapter()
    }

    private fun setChartsAdapter() {
        val listCharts = AppConfig.getInstance(requireContext()).getCacheListCharts() as ArrayList<Album>
        chartsView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        chartsView.adapter = AlbumHomeAdapter(listCharts, requireContext())
    }

    fun setAlbumsAdapter() {
        val listAlbum = AppConfig.getInstance(requireContext()).getCacheListAlbumSpring() as ArrayList<Album>
        albums.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        albums.adapter = AlbumHomeAdapter(listAlbum, requireContext())
    }

    fun setAdapter() {
        listSong = AppConfig.getInstance(requireContext()).getCacheListSongAPI() as ArrayList<Song>
        val homeAdapter = HomeAdapter(listSong, requireContext())
        playlistView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        playlistView.adapter = homeAdapter
    }
}