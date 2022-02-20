package com.example.musick.fragment

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
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
import androidx.recyclerview.widget.RecyclerView




class HomeFragment : Fragment() {

    var listSong = ArrayList<Song>()
    lateinit var homeAdapter: HomeAdapter
    var listLoadMore = ArrayList<Song>()
    var isLoading = false

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
                homeAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        getListLoadMore()
        setAdapter()
        initScrollListener()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        homeAdapter.notifyDataSetChanged()
    }

    private fun getListLoadMore() {
        listSong = AppConfig.getInstance(requireContext()).getCacheListSongAPI() as ArrayList<Song>
        if (listSong.size > 10) {
            for (i in 0 until 9) {
                listLoadMore.add(listSong[i])
            }
        }
    }


    private fun initScrollListener() {
        playlistView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager?
                if (!isLoading) {
                    if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == listLoadMore.size - 1) {
                        //bottom of list!
                        loadMore()
                        isLoading = true
                    }
                }
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadMore() {

        listLoadMore.add(Song("loading", null.toString(), null.toString(), null.toString(), null.toString(), null.toString() ))
        homeAdapter.notifyItemInserted(listLoadMore.size - 1)

        val handler = Handler()
        handler.postDelayed(Runnable {
            listLoadMore.removeAt(listLoadMore.size - 1)
            val scrollPosition = listLoadMore.size
            homeAdapter.notifyItemRemoved(scrollPosition)
            var currentSize = scrollPosition
            var nextLimit = 1
            if (listSong.size >= scrollPosition + 10) {
                nextLimit = currentSize + 10
            } else {
                nextLimit = listSong.size - 1
            }
            while (currentSize - 1 < nextLimit) {
                listLoadMore.add(listSong[currentSize])
                currentSize++
            }
            homeAdapter.notifyDataSetChanged()
            isLoading = false
        }, 2000)
    }


    private fun setChartsAdapter() {
        val listCharts = AppConfig.getInstance(requireContext()).getCacheListCharts() as ArrayList<Album>
        chartsView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        chartsView.adapter = AlbumHomeAdapter(listCharts, requireContext())
    }

    private fun setAlbumsAdapter() {
        val listAlbum = AppConfig.getInstance(requireContext()).getCacheListAlbumSpring() as ArrayList<Album>
        albums.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        albums.adapter = AlbumHomeAdapter(listAlbum, requireContext())
    }

    private fun setAdapter() {
        homeAdapter = HomeAdapter(listLoadMore, requireContext())
        playlistView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        playlistView.adapter = homeAdapter
    }
}