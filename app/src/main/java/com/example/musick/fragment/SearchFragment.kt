package com.example.musick.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.musick.R
import android.view.inputmethod.EditorInfo

import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musick.Song
import com.example.musick.adapter.SearchAdapter
import kotlinx.android.synthetic.main.fragment_search.*
import saveData.AppConfig


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class SearchFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null
    private val listALlSong = ArrayList<Song>()
    val listSearch = ArrayList<Song>()

    companion object {
        @JvmStatic
        fun newInstance() =
            SearchFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this com.example.musick.fragment
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getListSong()

        edtSearch.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                var search = edtSearch.text.toString()
                performSearch(search)
                return@OnEditorActionListener true
            }
            false
        })
    }

    private fun performSearch(search: String) {
        listSearch.clear()
        listALlSong.forEach {
            if (it.songName.contains(search, ignoreCase = true) || it.artist.contains(search, ignoreCase = true)) {
                listSearch.add(it)
            }
        }

        if (listSearch.size == 0) {
            noResult.visibility = View.VISIBLE
        } else {
            noResult.visibility = View.GONE
        }

        setAdapter()
    }

    private fun setAdapter() {
        searchList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        searchList.adapter = SearchAdapter(listSearch, requireContext())
    }

    private fun getListSong() {
        val listSongOff = AppConfig.getInstance(requireContext()).getCacheSong()
        val listSongAPI = AppConfig.getInstance(requireContext()).getCacheListSongAPI()
        listALlSong.clear()
        listALlSong.addAll(listSongOff)
        listALlSong.addAll(listSongAPI)
    }
}